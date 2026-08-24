package com.jstyle.test2025.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
 *
 * Analysis of the resulting CSVs: hardware/jstyle-spike/analyze_ppg.py in the tumtum repo.
 */
public class PPGActivity extends BaseActivity {
    private static final String TAG = "TumtumSpike";
    // Same magic value the vendor demo uses; the unit (s vs ms) is undocumented,
    // so long sessions rely on the auto re-arm below rather than on this number.
    private static final long MEASUREMENT_TIME = 50 * 1000;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppg);
        ButterKnife.bind(this);
        ppg_ChartsView.setBlankCount(20);
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
        }
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
        BleManager.getInstance().offerValue(
                BleSDK.SetDeviceMeasurementWithType(AutoTestMode.AutoHRV, MEASUREMENT_TIME, true));
        BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(true));
        BleManager.getInstance().writeValue();
    }

    @OnClick({R.id.start, R.id.end})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.start:
                recording = true;
                packetCount = 0;
                sampleCount = 0;
                openLogFiles();
                sendStartCommands();
                Start();
                break;
            case R.id.end:
                recording = false;
                BleManager.getInstance().offerValue(
                        BleSDK.SetDeviceMeasurementWithType(AutoTestMode.AutoHRV, MEASUREMENT_TIME, false));
                BleManager.getInstance().offerValue(BleSDK.setECGRealtimeDuringHRVEnabled(false));
                BleManager.getInstance().writeValue();
                closeLogFiles();
                break;
        }
    }

    @Override
    public void dataCallback(Map<String, Object> maps) {
        super.dataCallback(maps);
        String dataType = getDataType(maps);
        switch (dataType) {
            case BleConst.Getppg: {
                long now = System.currentTimeMillis();
                Map<String, String> map = getData(maps);
                String ppg = map.get(DeviceKey.arrayPpgRawData);
                String packetId = map.get(DeviceKey.packetID);
                if (ppg == null) break;
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
        }
    }
}
