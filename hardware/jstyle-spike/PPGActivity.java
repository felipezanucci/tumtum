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
 * 10. Raw-PPG probe (24/08 run 7): the run that captured PPG never sent the
 *     ECG-realtime flag false; the two runs that toggled it captured nothing.
 *     Start now walks three configurations (see the phase constants) so one
 *     recording settles whether raw PPG can be sustained at all.
 *
 * Analysis of the resulting CSVs: hardware/jstyle-spike/analyze_ppg.py in the tumtum repo.
 */
public class PPGActivity extends BaseActivity {
    private static final String TAG = "TumtumSpike";
    /**
     * Raw-PPG probe (run 7). Only one configuration ever produced raw PPG: v4's,
     * which re-armed with start ONLY and never cleared the ECG-realtime flag.
     * v5/v6 switched to a stop -> start toggle (which sends the flag false) and
     * captured zero packets across five sessions. This run A/B/Cs it:
     *
     *   Phase A  0-75s   exact v4 recipe: 300s session, start-only re-arm every
     *                    5s, flag never set false. Runs first, on a virgin state.
     *   Phase B  75-150s vendor demo's own literal duration (50*1000), same
     *                    start-only re-arm, in case the unit differs for AutoHRV.
     *   Phase C  150-225s clean stop, then a SINGLE start left completely
     *                    undisturbed, to test whether the re-arms themselves
     *                    were interrupting a stream that wanted to keep going.
     *   Then     falls back to AutoHeartRate so the run still yields a series.
     */
    private static final long PHASE_A_END_MS = 75_000;
    private static final long PHASE_B_END_MS = 150_000;
    private static final long PHASE_C_END_MS = 225_000;
    private static final long PROBE_REARM_MS = 5_000;

    private static final long HRV_SESSION_A = 300;
    private static final long HRV_SESSION_B = 50_000;

    /**
     * Raw PPG was settled on 24/08 (run 7): zero packets across all three probe
     * configurations and 32 measurement commands. The probe is kept for the
     * record but switched off, so a recording goes straight to heart rate —
     * which is what the Polar-referenced effort protocol needs.
     */
    private static final boolean PPG_PROBE_ENABLED = false;

    /** Try one long session first; 300s is the length proven to work in run 6. */
    private static final long HR_SESSION_TRY = 900;
    private static final long HR_SESSION_SAFE = 300;
    /** Re-arm this long before the session would expire, to avoid a gap mid-protocol. */
    private static final long HR_REARM_MARGIN_MS = 20_000;

    /** Gap between a stop and the start that follows it. */
    private static final long TOGGLE_GAP_MS = 800;
    /** In HR mode, re-arm when no non-zero heart rate arrives for this long. */
    private static final long HR_SILENCE_MS = 25_000;

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
    private volatile long lastPacketMs = 0;
    private volatile long firstPacketMs = 0;
    private volatile long lastHrMs = 0;
    private volatile long recordingStartMs = 0;
    private volatile long hrCount = 0;
    private volatile long lastArmMs = 0;
    /** 0 = phase A, 1 = phase B, 2 = phase C, 3 = heart rate. */
    private volatile int probePhase = 0;
    private volatile long hrSession = HR_SESSION_TRY;
    private volatile boolean hrSessionProven = false;
    /** Which measurement is armed; drives the on-screen status only. */
    private volatile AutoTestMode mode = AutoTestMode.AutoHRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppg);
        ButterKnife.bind(this);
        ppg_ChartsView.setBlankCount(20);
        info.setText("spike v8 (protocolo HR + Polar) — pronto");
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
     * Drives the probe: advances through phases A, B and C on elapsed time, and
     * within a phase re-arms on stream silence (except phase C, which is left
     * deliberately untouched). After the probe it keeps the HR series alive.
     */
    private void watchdogTick() {
        if (!recording) return;
        long now = System.currentTimeMillis();

        if (PPG_PROBE_ENABLED) {
            long elapsed = now - recordingStartMs;
            int want = elapsed < PHASE_A_END_MS ? 0
                     : elapsed < PHASE_B_END_MS ? 1
                     : elapsed < PHASE_C_END_MS ? 2 : 3;
            if (want != probePhase) {
                enterPhase(want);
                return;
            }
            if (probePhase == 2) return;
            if (probePhase != 3) {
                if (now - Math.max(lastPacketMs, lastArmMs) >= PROBE_REARM_MS) armPpg();
                return;
            }
        }

        long silence = now - Math.max(lastHrMs, lastArmMs);
        if (silence >= HR_SILENCE_MS) {
            // A session length the firmware refuses looks exactly like silence,
            // so drop to the length proven in run 6 before blaming the sensor.
            if (!hrSessionProven && hrSession != HR_SESSION_SAFE) {
                hrSession = HR_SESSION_SAFE;
                logEvent("hr_session_fallback_" + HR_SESSION_SAFE, "");
            }
            logEvent("watchdog_hr_silence_ms_" + silence, "");
            armHeartRate();
            return;
        }
        // Re-arm just before the session would expire: an expiry mid-protocol
        // costs a 9s warm-up and restarts the firmware ramp at the worst moment.
        if (hrSessionProven && now - lastArmMs >= hrSession * 1000 - HR_REARM_MARGIN_MS) {
            logEvent("proactive_rearm_before_expiry", "");
            armHeartRate();
        }
    }

    private void enterPhase(int phase) {
        probePhase = phase;
        logEvent("phase_" + phaseName() + "_start_ppg_ms_" + streamedMs(), "");
        if (phase == 0 || phase == 1) {
            armPpg();
            return;
        }
        // Phases C and HR both begin from a clean slate.
        mode = phase == 3 ? AutoTestMode.AutoHeartRate : AutoTestMode.AutoHRV;
        cleanStop();
        if (timer != null && !timer.isShutdown()) {
            timer.schedule(() -> {
                if (!recording) return;
                if (probePhase == 3) armHeartRate();
                else armPpg();
            }, TOGGLE_GAP_MS, TimeUnit.MILLISECONDS);
        }
        final String label = phase == 3 ? "PPG bloqueado — medindo HR (1Hz)"
                                        : "fase C: start único, sem re-armes";
        runOnUiThread(() -> info.setText(label));
    }

    private String phaseName() {
        switch (probePhase) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            default: return "HR";
        }
    }

    /**
     * Arm the raw-PPG measurement. Critically, the ECG-realtime flag is only ever
     * set true here: the one run that produced raw PPG never sent it false.
     */
    private void armPpg() {
        lastArmMs = System.currentTimeMillis();
        long seconds = probePhase == 1 ? HRV_SESSION_B : HRV_SESSION_A;
        logEvent("arm_ppg_phase" + phaseName() + "_dur_" + seconds, "");
        BleManager.getInstance().offerValue(BleSDK.RealTimeStep(true, true));
        BleManager.getInstance().offerValue(
                BleSDK.SetDeviceMeasurementWithType(AutoTestMode.AutoHRV, seconds, true));
        BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(true));
        BleManager.getInstance().writeValue();
    }

    private void armHeartRate() {
        lastArmMs = System.currentTimeMillis();
        logEvent("arm_hr_dur_" + hrSession, "");
        BleManager.getInstance().offerValue(BleSDK.RealTimeStep(true, true));
        BleManager.getInstance().offerValue(
                BleSDK.SetDeviceMeasurementWithType(AutoTestMode.AutoHeartRate, hrSession, true));
        BleManager.getInstance().writeValue();
    }

    /** Stop every measurement mode and clear the raw-PPG flag. */
    private void cleanStop() {
        logEvent("clean_stop", "");
        BleManager.getInstance().offerValue(BleSDK.SetDeviceMeasurementWithType(
                AutoTestMode.AutoHRV, HRV_SESSION_A, false));
        BleManager.getInstance().offerValue(BleSDK.SetDeviceMeasurementWithType(
                AutoTestMode.AutoHeartRate, hrSession, false));
        BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(false));
        BleManager.getInstance().writeValue();
    }

    /** Milliseconds of raw PPG actually streamed so far this recording. */
    private long streamedMs() {
        return firstPacketMs == 0 ? 0 : lastPacketMs - firstPacketMs;
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
                lastArmMs = 0;
                hrSession = HR_SESSION_TRY;
                hrSessionProven = false;
                recordingStartMs = System.currentTimeMillis();
                openLogFiles();
                if (PPG_PROBE_ENABLED) {
                    probePhase = 0;
                    mode = AutoTestMode.AutoHRV;
                    logEvent("phase_A_start_ppg_ms_0", "");
                    armPpg();
                } else {
                    probePhase = 3;
                    mode = AutoTestMode.AutoHeartRate;
                    logEvent("hr_only_run_start", "");
                    armHeartRate();
                }
                Start();
                break;
            case R.id.end:
                recording = false;
                cleanStop();
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
                runOnUiThread(() -> info.setText(
                        "fase " + phaseName() + " | PPG " + pc + " pacotes / " + sc + " amostras"));
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
                // The device ended the session itself. Re-arm for every phase
                // except C, whose whole point is to be left undisturbed.
                if (recording && probePhase != 2) {
                    logEvent("auto_rearm_phase" + phaseName(), "");
                    if (probePhase == 3) armHeartRate();
                    else armPpg();
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
                    hrSessionProven = true;
                }
                logEvent("hr", hrValue > 0 ? String.valueOf(hrValue) : "0");
                final long mins = (System.currentTimeMillis() - recordingStartMs) / 60000;
                final long secs = ((System.currentTimeMillis() - recordingStartMs) / 1000) % 60;
                final String status = "HR: " + (hrValue > 0 ? hrValue : "--")
                        + "   |   " + mins + "min" + (secs < 10 ? "0" : "") + secs
                        + "   |   " + hrCount + " leituras";
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
