#!/usr/bin/env python3
"""Tumtum raw-PPG spike analysis.

Takes the CSVs recorded by the patched vendor demo (PPGActivity.java in this
folder) plus a Polar H10 reference export, derives beats-per-minute from the
raw PPG signal, and reports the metrics that decide the JStyle purchase:
peak delay, peak amplitude ratio, overall accuracy and stream health.

Usage:
    python3 analyze_ppg.py --ppg ppg_raw_20260821_190000.csv \
        --events events_20260821_190000.csv \
        --polar polar_hr.csv [--offset-s 0] [--out report]

Polar reference format: either a two-column CSV `timestamp_ms,hr`, or a
Polar Sensor Logger HR export (semicolon-separated, "Phone timestamp;HR [bpm]").

Dependencies: pip install numpy scipy pandas matplotlib
"""
import argparse
import sys

import numpy as np
import pandas as pd
from scipy.signal import butter, filtfilt, find_peaks


def load_ppg(path):
    df = pd.read_csv(path)
    if not {"received_ms", "ppg"}.issubset(df.columns):
        sys.exit(f"{path}: expected columns received_ms,packet_id,sample_index,ppg")
    t0, t1 = df.received_ms.iloc[0], df.received_ms.iloc[-1]
    n = len(df)
    duration_s = (t1 - t0) / 1000.0
    fs = n / duration_s if duration_s > 0 else 0.0
    # Samples arrive in bursts (one BLE packet holds ~50-80 samples); spread
    # them uniformly across the recording for a usable time base.
    t = np.linspace(t0, t1, n) / 1000.0
    x = df.ppg.astype(float).to_numpy()
    return t, x, fs, df


def packet_loss(df):
    if "packet_id" not in df.columns or df.packet_id.isna().all():
        return None
    pids = df.groupby((df.packet_id != df.packet_id.shift()).cumsum()).packet_id.first()
    pids = pids.astype(float).to_numpy()
    d = np.diff(pids)
    # ignore counter wrap-arounds (large negative jumps)
    gaps = d[(d > 1)]
    lost = int(np.sum(gaps - 1))
    total = len(pids) + lost
    return lost, total, (100.0 * lost / total if total else 0.0)


def bandpass(x, fs, lo=0.5, hi=4.0, order=3):
    b, a = butter(order, [lo / (fs / 2), hi / (fs / 2)], btype="band")
    return filtfilt(b, a, x)


def ppg_to_hr(t, x, fs):
    """Raw PPG -> 1 Hz heart-rate series (t_1hz, hr_1hz)."""
    # resample to a uniform grid so the filter is well defined
    fs_u = 25.0
    tu = np.arange(t[0], t[-1], 1.0 / fs_u)
    xu = np.interp(tu, t, x)
    xf = bandpass(xu, fs_u)
    # adaptive prominence: rolling std over 10 s
    prom = 0.4 * pd.Series(xf).rolling(int(10 * fs_u), min_periods=1, center=True).std().to_numpy()
    peaks, _ = find_peaks(xf, distance=int(0.33 * fs_u), prominence=prom)
    if len(peaks) < 3:
        sys.exit("Too few beats detected - check the signal (was the band snug on the wrist?)")
    tp = tu[peaks]
    ibi = np.diff(tp)
    hr = 60.0 / ibi
    th = (tp[1:] + tp[:-1]) / 2
    ok = (hr > 30) & (hr < 220)
    th, hr = th[ok], hr[ok]
    # median filter against ectopic/missed beats, then 1 Hz grid + 5 s smoothing
    hr = pd.Series(hr).rolling(5, min_periods=1, center=True).median().to_numpy()
    t1 = np.arange(np.ceil(th[0]), np.floor(th[-1]))
    hr1 = np.interp(t1, th, hr)
    hr1 = pd.Series(hr1).rolling(5, min_periods=1, center=True).mean().to_numpy()
    return t1, hr1


def load_polar(path, offset_s):
    with open(path) as f:
        head = f.readline()
    if ";" in head:  # Polar Sensor Logger export
        df = pd.read_csv(path, sep=";")
        tcol = [c for c in df.columns if "timestamp" in c.lower()][0]
        hcol = [c for c in df.columns if "hr" in c.lower()][0]
        ts = pd.to_datetime(df[tcol]).astype("int64") / 1e9
        hr = df[hcol].astype(float).to_numpy()
    else:
        df = pd.read_csv(path)
        ts = df.iloc[:, 0].astype(float).to_numpy()
        if ts.max() > 1e11:  # milliseconds
            ts = ts / 1000.0
        hr = df.iloc[:, 1].astype(float).to_numpy()
    return np.asarray(ts) + offset_s, hr


def peak_metrics(t_ref, hr_ref, t_ppg, hr_ppg, label, out_lines):
    """Delay and amplitude ratio at the reference's highest peak."""
    lo = max(t_ref[0], t_ppg[0])
    hi = min(t_ref[-1], t_ppg[-1])
    mref = (t_ref >= lo) & (t_ref <= hi)
    if not mref.any():
        out_lines.append(f"[{label}] no time overlap between reference and PPG series")
        return
    i_ref = np.argmax(hr_ref * mref)
    t_peak_ref, v_peak_ref = t_ref[i_ref], hr_ref[i_ref]
    base_ref = np.median(hr_ref[mref][:60]) if mref.sum() > 60 else np.median(hr_ref[mref])

    win = (t_ppg >= t_peak_ref - 60) & (t_ppg <= t_peak_ref + 60)
    if not win.any():
        out_lines.append(f"[{label}] PPG series has no data within ±60 s of the reference peak")
        return
    i_ppg = np.argmax(np.where(win, hr_ppg, -np.inf))
    t_peak_ppg, v_peak_ppg = t_ppg[i_ppg], hr_ppg[i_ppg]
    base_ppg = np.median(hr_ppg[:60]) if len(hr_ppg) > 60 else np.median(hr_ppg)

    delay = t_peak_ppg - t_peak_ref
    amp = (v_peak_ppg - base_ppg) / (v_peak_ref - base_ref) if v_peak_ref != base_ref else float("nan")
    out_lines.append(
        f"[{label}] reference peak {v_peak_ref:.0f} bpm | measured peak {v_peak_ppg:.0f} bpm | "
        f"delay {delay:+.1f} s | amplitude ratio {amp:.2f}"
    )


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--ppg", required=True)
    ap.add_argument("--events", default=None)
    ap.add_argument("--polar", default=None)
    ap.add_argument("--offset-s", type=float, default=0.0,
                    help="add this many seconds to the Polar clock to align it with the phone clock")
    ap.add_argument("--out", default="report")
    args = ap.parse_args()

    lines = []
    t, x, fs, df = load_ppg(args.ppg)
    lines.append(f"PPG: {len(x)} samples over {t[-1]-t[0]:.0f} s -> estimated sample rate {fs:.1f} Hz")
    pl = packet_loss(df)
    if pl:
        lost, total, pct = pl
        lines.append(f"Packet loss: {lost}/{total} packets ({pct:.2f}%)")

    t1, hr1 = ppg_to_hr(t, x, fs)
    lines.append(f"Derived HR: {len(t1)} s of 1 Hz data, range {hr1.min():.0f}-{hr1.max():.0f} bpm")

    t_dev = hr_dev = None
    if args.events:
        ev = pd.read_csv(args.events)
        dev = ev[ev.event.astype(str).str.startswith("device_measurement_") & ev.device_heart_rate.notna()]
        dev = dev[pd.to_numeric(dev.device_heart_rate, errors="coerce").notna()]
        if len(dev):
            t_dev = dev.received_ms.to_numpy() / 1000.0
            hr_dev = dev.device_heart_rate.astype(float).to_numpy()
            hr_dev_valid = hr_dev > 0
            t_dev, hr_dev = t_dev[hr_dev_valid], hr_dev[hr_dev_valid]
            lines.append(f"Device-reported HR points: {len(hr_dev)}")
        rearms = int((ev.event == "auto_rearm").sum())
        lines.append(f"Auto re-arms during the session: {rearms}")

    if args.polar:
        t_ref, hr_ref = load_polar(args.polar, args.offset_s)
        lo, hi = max(t_ref[0], t1[0]), min(t_ref[-1], t1[-1])
        if hi <= lo:
            lines.append("WARNING: Polar and PPG recordings do not overlap in time - check --offset-s")
        else:
            grid = np.arange(lo, hi)
            a = np.interp(grid, t1, hr1)
            b = np.interp(grid, t_ref, hr_ref)
            lines.append(f"Overlap with Polar: {len(grid)} s | MAE {np.mean(np.abs(a-b)):.1f} bpm")
            peak_metrics(t_ref, hr_ref, t1, hr1, "PPG-derived HR vs Polar", lines)
            if t_dev is not None and len(t_dev) > 5:
                peak_metrics(t_ref, hr_ref, t_dev, hr_dev, "Firmware HR vs Polar", lines)

    print("\n".join(lines))
    with open(f"{args.out}.txt", "w") as f:
        f.write("\n".join(lines) + "\n")

    try:
        import matplotlib
        matplotlib.use("Agg")
        import matplotlib.pyplot as plt
        fig, ax = plt.subplots(figsize=(12, 5))
        ax.plot(t1 - t1[0], hr1, label="HR from raw PPG (ours)", lw=1.5)
        if args.polar:
            ax.plot(t_ref - t1[0], hr_ref, label="Polar H10 (reference)", lw=1.0, alpha=0.8)
        if t_dev is not None:
            ax.plot(t_dev - t1[0], hr_dev, ".", ms=3, label="Firmware HR (device)")
        ax.set_xlabel("time (s)")
        ax.set_ylabel("bpm")
        ax.legend()
        ax.set_title("JStyle V8 raw-PPG spike")
        fig.tight_layout()
        fig.savefig(f"{args.out}.png", dpi=150)
        print(f"Plot saved to {args.out}.png")
    except Exception as e:  # plotting is optional
        print(f"(plot skipped: {e})")


if __name__ == "__main__":
    main()
