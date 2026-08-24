package com.jstyle.test2025.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jstyle.test2025.BuildConfig;

import com.jstyle.blesdkv8.Util.BleSDK;
import com.jstyle.blesdkv8.constant.BleConst;
import com.jstyle.blesdkv8.constant.DeviceKey;
import com.jstyle.blesdkv8.model.AutoTestMode;
import com.jstyle.test2025.R;
import com.jstyle.test2025.ble.BleManager;
import com.jstyle.test2025.views.PPGChartsView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Tumtum raw-PPG validation spike (drop-in replacement for the vendor demo's PPGActivity).
 *
 * Adds to the stock demo screen:
 *  1. CSV logging of every raw PPG sample with wall-clock packet timestamps and packetID
 *     (files land in Android/data/com.jstyle.test2025/files/).
 *  2. CSV logging of measurement events and the device's own processed HR, so the
 *     firmware BPM can be compared against our PPG-derived BPM on the same clock.
 *  3. Auto re-arm: when the device ends the HRV measurement session, the same start
 *     commands are sent again so the raw stream keeps flowing for multi-hour tests.
 *  4. Watchdog re-arm: the A17 bench run of 2026-08-24 showed the stream dying
 *     silently (2 packets then nothing, no stop callback), so a 1s watchdog
 *     re-sends the start commands whenever the stream stays mute for 5s.
 *  5. Catch-all callback logging: every BLE callback type the device sends is
 *     written to the events CSV, so a refusal or an unmapped response is visible.
 *  6. Share sheet on End: the finished CSVs are offered straight through the
 *     system share dialog (Drive, WhatsApp...), because the Downloads export
 *     proved hard to locate in the field; export errors now surface on screen.
 *  7. Two-phase run (24/08 field findings): measurement durations are in SECONDS
 *     (the old 50*1000 was likely rejected), and the 1 Hz RealTimeStep stream is
 *     rock-solid but only carries HR during an active measurement. Start now arms
 *     AutoHRV (raw PPG) for 60s; with zero PPG packets it falls back to
 *     AutoHeartRate and records the 1 Hz SDK heart rate as "hr" events — making
 *     the SDK-vs-app filter protocol runnable even while raw PPG stays blocked.
 *  8. Toggle re-arm (24/08 run 4): raw PPG flowed for exactly 2s starting 59ms
 *     after a re-arm, then 47 further "start" commands were ignored — the
 *     firmware no-ops a start while it believes a session is active. Re-arming
 *     now sends stop, waits, and starts again, and the HR fallback triggers on
 *     total PPG yield rather than on having never seen a packet.
 *  9. Warm-up aware (24/08 run 5): the sensor needs 8-9s after a measurement
 *     command before it emits anything, so v5's 5s watchdog killed every
 *     session before it could produce data (zero PPG packets all run). The
 *     PPG watchdog now waits 30s, and HR sessions are long (300s) because each
 *     re-arm costs a warm-up and restarts the firmware's 0.5 BPM/s ramp.
 *
 * Analysis of the resulting CSVs: hardware/jstyle-spike/analyze_ppg.py in the tumtum repo.
 */
public class PPGActivity extends BaseActivity {
    private static final String TAG = "TumtumSpike";
    // Session durations in SECONDS — confirmed empirically on 24/08: a 60s
    // request yielded 9s of warm-up plus exactly 51s of 1 Hz heart rate.
    // Raw PPG gets a medium session; HR gets a long one because every re-arm
    // costs a fresh 9s warm-up AND restarts the firmware's 0.5 BPM/s ramp.
    private static final long MEASUREMENT_TIME_HRV = 120;
    private static final long MEASUREMENT_TIME_HR = 300;

    @BindView(R.id.info)
    TextView info;
    @BindView(R.id.ppg_ChartsView)
    PPGChartsView ppg_ChartsView;
    private final Deque<Float> queues = new LinkedList<>();
    private ScheduledThreadPoolExecutor timer = null;

    private BufferedWriter ppgWriter = null;
    private BufferedWriter eventWriter = null;
    private File ppgFile = null;
    private File eventFile = null;
    private boolean recording = false;
    private long packetCount = 0;
    private long sampleCount = 0;
    /**
     * Re-arm after this much raw-PPG silence. Must comfortably exceed the
     * sensor warm-up (measured at 8-9s on 24/08): the 5s threshold of v5 kept
     * killing each session before the sensor was ready, which is why that run
     * captured zero PPG packets while v4's slower re-arm captured a burst.
     */
    private static final long WATCHDOG_SILENCE_MS = 30_000;
    /** Gap between the stop and the start of a re-arm toggle. */
    private static final long TOGGLE_GAP_MS = 800;
    /** Give the raw-PPG (AutoHRV) path this long before falling back to plain HR. */
    private static final long HRV_GIVE_UP_MS = 150_000;
    /** In HR mode, re-arm when no non-zero heart rate arrives for this long. */
    private static final long HR_SILENCE_MS = 25_000;
    private volatile long lastPacketMs = 0;
    private volatile long firstPacketMs = 0;
    private volatile long lastArmMs = 0;
    private volatile long lastHrMs = 0;
    private volatile long recordingStartMs = 0;
    private volatile long hrCount = 0;
    private volatile int lastHr = 0;
    /** Which measurement the watchdog keeps armed: raw-PPG first, HR fallback. */
    private volatile AutoTestMode mode = AutoTestMode.AutoHRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppg);
        ButterKnife.bind(this);
        ppg_ChartsView.setBlankCount(20);
        info.setText("spike v6 (warm-up aware) — pronto");
    }

    private void Start() {
        if (null == timer || timer.isShutdown()) {
            timer = new ScheduledThreadPoolExecutor(2);
            timer.scheduleWithFixedDelay(() -> {
                try {
                    PPGActivity.this.runOnUiThread(() -> {
                        Deque<Float> requeppg = queues;
                        Float ppgvalue = requeppg.pollLast();
                        if (null != ppgvalue && null != ppg_ChartsView) {
                            ppg_ChartsView.addShowDatasPPG(Collections.singletonList(ppgvalue));
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }, 0, 50L, TimeUnit.MILLISECONDS);
            timer.scheduleWithFixedDelay(this::watchdogTick, 1, 1, TimeUnit.SECONDS);
        }
    }

    /**
     * The device has been seen dropping the raw stream without sending any stop
     * callback, which starves the callback-driven re-arm. This ticks every second
     * while recording and re-sends the start commands after WATCHDOG_SILENCE_MS
     * of silence, surfacing the wait on screen so the operator sees it happening.
     */
    private void watchdogTick() {
        if (!recording) return;
        long now = System.currentTimeMillis();

        if (mode == AutoTestMode.AutoHRV) {
            // The 24/08 run delivered raw PPG for exactly 2s, 59ms after a re-arm,
            // and then ignored 47 further "start" commands: the device treats a
            // start as a no-op while it believes a session is running. So re-arm
            // by toggling stop -> start instead of repeating start.
            long silence = now - Math.max(lastPacketMs, lastArmMs);

            // Fall back on total PPG yield, not on "never saw a packet": a brief
            // burst must not strand the run in a mode that yields nothing more.
            if (now - recordingStartMs > HRV_GIVE_UP_MS && streamedMs() < 20_000) {
                logEvent("mode_switch_heart_ppg_ms_" + streamedMs(), "");
                switchToHeartRate();
                return;
            }
            if (silence < WATCHDOG_SILENCE_MS) return;
            logEvent("watchdog_toggle_after_ms_" + silence, "");
            toggleRearm();
            final long silenceS = silence / 1000;
            runOnUiThread(() -> info.setText("PPG mudo há " + silenceS + "s — toggle stop/start..."));
        } else {
            long silence = now - Math.max(lastHrMs, lastArmMs);
            if (silence < HR_SILENCE_MS) return;
            logEvent("watchdog_toggle_hr_after_ms_" + silence, "");
            toggleRearm();
        }
    }

    /** Session length to request for the mode currently armed. */
    private long measurementSeconds() {
        return mode == AutoTestMode.AutoHRV ? MEASUREMENT_TIME_HRV : MEASUREMENT_TIME_HR;
    }

    /** Milliseconds of raw PPG actually streamed so far this recording. */
    private long streamedMs() {
        return firstPacketMs == 0 ? 0 : lastPacketMs - firstPacketMs;
    }

    /**
     * Re-arm as a real toggle: stop the measurement, wait, then start it again.
     * Repeating start alone was proven to be a no-op on this firmware.
     */
    private void toggleRearm() {
        lastArmMs = System.currentTimeMillis();
        sendStopCommands();
        if (timer != null && !timer.isShutdown()) {
            timer.schedule(() -> {
                if (recording) sendStartCommands();
            }, TOGGLE_GAP_MS, TimeUnit.MILLISECONDS);
        }
    }

    /** Stop whichever measurement is armed, plus the raw-PPG flag. */
    private void sendStopCommands() {
        BleManager.getInstance().offerValue(
                BleSDK.SetDeviceMeasurementWithType(mode, measurementSeconds(), false));
        if (mode == AutoTestMode.AutoHRV) {
            BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(false));
        }
        BleManager.getInstance().writeValue();
    }

    /** Hand the run over to the plain heart-rate measurement. */
    private void switchToHeartRate() {
        sendStopCommands();
        mode = AutoTestMode.AutoHeartRate;
        lastArmMs = System.currentTimeMillis();
        if (timer != null && !timer.isShutdown()) {
            timer.schedule(() -> {
                if (recording) sendStartCommands();
            }, TOGGLE_GAP_MS, TimeUnit.MILLISECONDS);
        }
        runOnUiThread(() -> info.setText("PPG bruto não sustentou — medindo HR (1Hz)"));
    }

    private void openLogFiles() {
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File dir = getExternalFilesDir(null);
        try {
            ppgFile = new File(dir, "ppg_raw_" + stamp + ".csv");
            ppgWriter = new BufferedWriter(new FileWriter(ppgFile));
            ppgWriter.write("received_ms,packet_id,sample_index,ppg\n");

            eventFile = new File(dir, "events_" + stamp + ".csv");
            eventWriter = new BufferedWriter(new FileWriter(eventFile));
            eventWriter.write("received_ms,event,device_heart_rate\n");

            logEvent("recording_started", "");
            Log.i(TAG, "Logging to " + ppgFile.getAbsolutePath());
            info.setText("REC: " + ppgFile.getName());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open log files", e);
            info.setText("ERRO ao criar arquivos de log: " + e.getMessage());
        }
    }

    private void closeLogFiles() {
        try {
            if (eventWriter != null) {
                logEvent("recording_stopped", "");
                eventWriter.close();
            }
            if (ppgWriter != null) ppgWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Failed to close log files", e);
        }
        ppgWriter = null;
        eventWriter = null;
        exportToDownloads(ppgFile);
        exportToDownloads(eventFile);
        shareCsvs();
    }

    /**
     * Offers the finished CSVs through the system share sheet, using the same
     * FileProvider the vendor demo already ships for its log sharing. This is
     * the primary hand-off path; the Downloads copy is a fallback.
     */
    private void shareCsvs() {
        try {
            java.util.ArrayList<Uri> uris = new java.util.ArrayList<>();
            for (File f : new File[]{ppgFile, eventFile}) {
                if (f != null && f.exists()) {
                    uris.add(FileProvider.getUriForFile(
                            this, BuildConfig.APPLICATION_ID + ".provider", f));
                }
            }
            if (uris.isEmpty()) {
                runOnUiThread(() -> info.setText("nenhum CSV para compartilhar"));
                return;
            }
            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.setType("text/csv");
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Enviar CSVs do spike"));
        } catch (Exception e) {
            Log.e(TAG, "Share failed", e);
            runOnUiThread(() -> info.setText("ERRO ao compartilhar: " + e.getMessage()));
        }
    }

    /**
     * Copies a finished CSV into the public Downloads/tumtum_spike folder so it
     * can be shared straight from the phone (Android/data is not browsable on
     * modern Android). API 29+ only; on older devices the app-dir copy remains.
     */
    private void exportToDownloads(File src) {
        if (src == null || !src.exists() || android.os.Build.VERSION.SDK_INT < 29) return;
        try {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, src.getName());
            values.put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            values.put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_DOWNLOADS + "/tumtum_spike");
            android.net.Uri uri = getContentResolver()
                    .insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) return;
            try (java.io.OutputStream os = getContentResolver().openOutputStream(uri);
                 java.io.FileInputStream is = new java.io.FileInputStream(src)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) os.write(buf, 0, n);
            }
            Log.i(TAG, "Exported to Downloads/tumtum_spike: " + src.getName());
            runOnUiThread(() -> info.setText("CSVs salvos em Downloads/tumtum_spike"));
        } catch (Exception e) {
            Log.e(TAG, "Export to Downloads failed", e);
            runOnUiThread(() -> info.setText("ERRO no export p/ Downloads: " + e.getMessage()));
        }
    }

    private void logEvent(String event, String heartRate) {
        if (eventWriter == null) return;
        try {
            eventWriter.write(System.currentTimeMillis() + "," + event + "," + heartRate + "\n");
            eventWriter.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write event", e);
        }
    }

    private void sendStartCommands() {
        lastArmMs = System.currentTimeMillis();
        // Keep the 1 Hz real-time stream on: it carries the device HR while a
        // measurement session is active (field-validated on the A17, 24/08).
        BleManager.getInstance().offerValue(BleSDK.RealTimeStep(true, true));
        logEvent("arm_" + mode + "_" + measurementSeconds() + "s", "");
        BleManager.getInstance().offerValue(
                BleSDK.SetDeviceMeasurementWithType(mode, measurementSeconds(), true));
        if (mode == AutoTestMode.AutoHRV) {
            BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(true));
        }
        BleManager.getInstance().writeValue();
    }

    @OnClick({R.id.start, R.id.end})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start:
                if (ppgWriter != null) {
                    // Start pressed twice without End: flush and export the previous
                    // pair instead of silently leaking it.
                    recording = false;
                    closeLogFiles();
                }
                recording = true;
                packetCount = 0;
                sampleCount = 0;
                lastPacketMs = 0;
                firstPacketMs = 0;
                lastHrMs = 0;
                hrCount = 0;
                lastHr = 0;
                recordingStartMs = System.currentTimeMillis();
                mode = AutoTestMode.AutoHRV;
                openLogFiles();
                sendStartCommands();
                Start();
                break;
            case R.id.end:
                recording = false;
                BleManager.getInstance().offerValue(BleSDK.SetDeviceMeasurementWithType(
                        AutoTestMode.AutoHRV, MEASUREMENT_TIME_HRV, false));
                BleManager.getInstance().offerValue(BleSDK.SetDeviceMeasurementWithType(
                        AutoTestMode.AutoHeartRate, MEASUREMENT_TIME_HR, false));
                BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(false));
                BleManager.getInstance().offerValue(BleSDK.RealTimeStep(false, false));
                BleManager.getInstance().writeValue();
                closeLogFiles();
                break;
        }
    }

    @Override
    public void dataCallback(Map<String, Object> maps) {
        super.dataCallback(maps);
        String dataType = getDataType(maps);
        if (dataType == null) dataType = "";
        switch (dataType) {
            case BleConst.Getppg: {
                long now = System.currentTimeMillis();
                Map<String, String> map = getData(maps);
                String ppg = map.get(DeviceKey.arrayPpgRawData);
                String packetId = map.get(DeviceKey.packetID);
                if (ppg == null) break;
                if (firstPacketMs == 0) firstPacketMs = now;
                lastPacketMs = now;
                packetCount++;
                String[] samples = ppg.split(",");
                if (ppgWriter != null) {
                    try {
                        StringBuilder sb = new StringBuilder(samples.length * 24);
                        for (int i = 0; i < samples.length; i++) {
                            sb.append(now).append(',')
                              .append(packetId == null ? "" : packetId).append(',')
                              .append(i).append(',')
                              .append(samples[i].trim()).append('\n');
                        }
                        ppgWriter.write(sb.toString());
                        ppgWriter.flush();
                        sampleCount += samples.length;
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to write PPG samples", e);
                    }
                }
                for (String a : samples) {
                    try {
                        queues.addFirst(Float.valueOf(a.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
                final long pc = packetCount;
                final long sc = sampleCount;
                runOnUiThread(() -> info.setText("pacotes: " + pc + "  amostras: " + sc));
                break;
            }
            case BleConst.MeasurementHrvCallback:
            case BleConst.MeasurementHeartCallback: {
                Map<String, String> map = getData(maps);
                String hr = map == null ? null : map.get(DeviceKey.HeartRate);
                logEvent("device_measurement_" + dataType, hr == null ? "" : hr);
                break;
            }
            case BleConst.StopMeasurementHrvCallback:
            case BleConst.StopMeasurementHeartCallback: {
                logEvent("device_measurement_stopped_" + dataType, "");
                // The device ended the measurement session on its own: re-arm so the
                // raw stream keeps flowing during long soak tests.
                if (recording) {
                    logEvent("auto_rearm", "");
                    sendStartCommands();
                }
                break;
            }
            case BleConst.RealTimeStep: {
                // 1 Hz stream: heartRate is non-zero only while a measurement
                // session is active — this series IS the SDK-HR protocol data.
                Map<String, String> map = getData(maps);
                String hr = map == null ? null : map.get(DeviceKey.HeartRate);
                int hrValue = 0;
                try {
                    hrValue = hr == null ? 0 : (int) Float.parseFloat(hr.trim());
                } catch (NumberFormatException ignored) {
                }
                if (hrValue > 0) {
                    lastHrMs = System.currentTimeMillis();
                    hrCount++;
                    lastHr = hrValue;
                }
                logEvent("hr", hrValue > 0 ? String.valueOf(hrValue) : "0");
                final String status = "modo: " + mode + " | HR: " + (hrValue > 0 ? hrValue : "--")
                        + " | leituras HR: " + hrCount + " | pacotes PPG: " + packetCount
                        + " (" + (streamedMs() / 1000) + "s)";
                runOnUiThread(() -> info.setText(status));
                break;
            }
            default: {
                // Unknown territory is exactly what this spike needs to see: record
                // every other callback the device sends, payload included, so a
                // refusal or an unmapped response shows up in the events CSV.
                logEvent("cb_" + dataType, compactPayload(maps));
                break;
            }
        }
    }

    /** Flatten a callback payload to one CSV-safe cell (no commas or newlines). */
    private static String compactPayload(Map<String, Object> maps) {
        Object data = maps == null ? null : maps.get(DeviceKey.Data);
        if (data == null) return "";
        String text = String.valueOf(data).replace(',', ';').replace('\n', ' ');
        return text.length() > 200 ? text.substring(0, 200) : text;
    }
}
