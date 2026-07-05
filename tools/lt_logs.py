#!/usr/bin/env python3
"""Pull LambdaTest mobile session logs to THIS machine, regardless of where the test ran.

LambdaTest stores every session's artifacts in the cloud, so this script only needs your
creds + a session id — run it from any machine.

Usage (load creds first: LT_USERNAME / LT_ACCESS_KEY):
  python lt_logs.py list [N]            # list N recent sessions
  python lt_logs.py report [N]          # scan N recent sessions, report FAILED ones -> lt-logs/report.html
  python lt_logs.py latest              # download the newest session's logs+video
  python lt_logs.py logs <sessionId>    # download a specific session's logs+video
  python lt_logs.py detail <sessionId>  # dump session JSON

Artifacts are saved to tools/lt-logs/<sessionId>/ and text logs are analyzed for app issues.
`report` also writes a self-contained HTML report to tools/lt-logs/report.html.
NOTE: the API device log is capped ~1MB (session start). The FULL crash logcat comes from the
in-session capture (urpay-mobile-tests/logcat/ on the execution machine).
"""
import base64
import datetime
import html
import json
import os
import re
import sys
import urllib.error
import urllib.request

USER = os.environ.get("LT_USERNAME")
KEY = os.environ.get("LT_ACCESS_KEY")
BASE = "https://mobile-api.lambdatest.com/mobile-automation/api/v1"
BASEDIR = os.path.dirname(os.path.abspath(__file__))

# The app under test. Matches com.urpay.consumer / com.urpay.consumer.sit.
APP_PKG = "com.urpay"

# Crashes from these processes are device/OS noise (launcher, GMS, system UI), NOT our app.
NOISE_PKGS = (
    "com.miui", "com.google", "com.android.systemui", "com.sec.android",
    "android.process", "system_server", "com.samsung", "com.qualcomm",
    "gms", "com.xiaomi", "com.oppo", "com.oneplus", "com.heytap",
)

# Sessions in these states are treated as failures to report.
FAILED_STATUSES = {"failed", "error", "timeout", "aborted", "lambda error"}

# Back-compat broad matcher used by cmd_logs' quick grep.
CRASH_PATS = re.compile(
    r"FATAL EXCEPTION|FATAL SIGNAL|AndroidRuntime|ANR in|signal 11|signal 6|"
    r"SIGSEGV|SIGABRT|beginning of crash|com\.urpay|Process: |Caused by:|has died",
    re.IGNORECASE,
)

# Precise, process-aware matchers used by analyze_text().
_FATAL_JAVA = re.compile(r"FATAL EXCEPTION", re.I)
_PROC_LINE = re.compile(r"Process:\s*([\w.:]+)", re.I)
_FATAL_SIGNAL = re.compile(r"Fatal signal", re.I)
_NATIVE_NAME = re.compile(r"\(([\w.:]+)\)\s*$")
_TOMBSTONE = re.compile(r">>>\s*([\w.:]+)\s*<<<")
_JS_ERROR = re.compile(
    r"(ReactNativeJS.*(?:Error|Exception|redbox))|JavascriptException|"
    r"TypeError|ReferenceError|undefined is not a function|is not a function|"
    r"Unhandled (?:JS |Promise )",
    re.I,
)
_ANR = re.compile(r"ANR in\s+([\w.:]+)", re.I)
_BACKEND = re.compile(
    r"Network request failed|Unable to resolve host|ETIMEDOUT|ECONNREFUSED|"
    r"ENOTFOUND|Service ?Unavailable|Bad Gateway|Gateway Timeout|"
    r"HTTP/?\d?\.?\d?\s*5\d\d|status(?:Code)?[\"':=\s]+5\d\d",
    re.I,
)


def _hdr():
    token = base64.b64encode(f"{USER}:{KEY}".encode()).decode()
    return {"Authorization": f"Basic {token}"}


def _get(url, auth=True):
    req = urllib.request.Request(url, headers=_hdr() if auth else {})
    with urllib.request.urlopen(req, timeout=120) as resp:
        return resp.read()


def _get_json(url):
    return json.loads(_get(url))


# --------------------------------------------------------------------------- #
# Log analysis (process-aware): flag only real app issues, ignore OS noise.
# --------------------------------------------------------------------------- #
def _is_app(pkg):
    return bool(pkg) and APP_PKG in pkg


def _issue(category, severity, process, line_no, evidence):
    return dict(category=category, severity=severity, process=process,
                line_no=line_no, evidence=[e for e in evidence if e][:6])


def _dedup(issues):
    seen, out = set(), []
    for it in sorted(issues, key=lambda x: (x["severity"], x["line_no"])):
        first = it["evidence"][0] if it["evidence"] else str(it["line_no"])
        key = (it["category"], first)
        if key in seen:
            continue
        seen.add(key)
        out.append(it)
    return out


def analyze_text(text, source=""):
    """Return (issues, noise).

    issues: app-attributed problems, each dict(category, severity, process, line_no, evidence).
            severity 1=crash, 2=JS/ANR, 3=backend.
    noise:  {process: count} for FATALs from non-app processes (launcher/GMS/etc.).
    """
    lines = text.splitlines()
    n = len(lines)
    issues, noise = [], {}
    skip_until = -1
    for i, ln in enumerate(lines):
        # 1) Java / RN fatal exception -> attribute to the crashing Process:.
        if _FATAL_JAVA.search(ln):
            pkg = None
            for j in range(i, min(i + 6, n)):
                m = _PROC_LINE.search(lines[j])
                if m:
                    pkg = m.group(1)
                    break
            skip_until = i + 15  # suppress JS/backend hits inside this stack trace
            evidence = [l.rstrip() for l in lines[i:i + 6]]
            if _is_app(pkg):
                issues.append(_issue("APP CRASH (fatal exception)", 1, pkg, i + 1, evidence))
            else:
                noise[pkg or "unknown"] = noise.get(pkg or "unknown", 0) + 1
            continue
        # 2) Native fatal signal (SIGSEGV/SIGABRT) -> attribute via (name) or tombstone header.
        if _FATAL_SIGNAL.search(ln):
            pkg = None
            m = _NATIVE_NAME.search(ln.strip())
            if m:
                pkg = m.group(1)
            if not _is_app(pkg):
                for j in range(i, min(i + 10, n)):
                    t = _TOMBSTONE.search(lines[j])
                    if t:
                        pkg = t.group(1)
                        break
            skip_until = i + 10
            evidence = [l.rstrip() for l in lines[i:i + 4]]
            if _is_app(pkg):
                issues.append(_issue("APP CRASH (native signal)", 1, pkg, i + 1, evidence))
            elif pkg:
                noise[pkg] = noise.get(pkg, 0) + 1
            continue
        # 3) ANR.
        m = _ANR.search(ln)
        if m:
            pkg = m.group(1)
            evidence = [l.rstrip() for l in lines[i:i + 3]]
            if _is_app(pkg):
                issues.append(_issue("ANR (app not responding)", 2, pkg, i + 1, evidence))
            else:
                noise[pkg] = noise.get(pkg, 0) + 1
            continue
        if i <= skip_until:
            continue  # part of a fatal stack already reported above
        # 4) Non-fatal JS error / redbox (app is alive but a feature broke).
        if _JS_ERROR.search(ln):
            evidence = [l.rstrip() for l in lines[max(0, i - 1):i + 3]]
            issues.append(_issue("JS error / redbox (non-fatal)", 2, APP_PKG, i + 1, evidence))
            continue
        # 5) Backend / service error surfaced in the app logs.
        if _BACKEND.search(ln):
            evidence = [l.rstrip() for l in lines[max(0, i - 1):i + 2]]
            issues.append(_issue("Backend / service error", 3, "(network)", i + 1, evidence))
            continue
    return _dedup(issues), noise


def scan_crashes(text):
    """Back-compat helper: return (app_crashes, noise) with marker/pkg/cause dicts."""
    issues, noise = analyze_text(text)
    crashes = []
    for it in issues:
        if it["severity"] != 1:
            continue
        cause = ""
        for e in it["evidence"]:
            if "Process:" not in e and "FATAL" not in e:
                cause = e
                break
        crashes.append(dict(marker=it["category"], pkg=it["process"],
                            cause=cause or (it["evidence"][-1] if it["evidence"] else "")))
    return crashes, noise


def cmd_list(limit=10):
    data = _get_json(f"{BASE}/sessions?limit={limit}")
    rows = data.get("data") or data.get("sessions") or []
    for s in rows:
        print(f"{s.get('session_id','?')} | {s.get('status_ind','?'):<10} | "
              f"{s.get('platform','?')} {s.get('device','')} | {s.get('name','')}")


def cmd_detail(session_id):
    print(json.dumps(_get_json(f"{BASE}/sessions/{session_id}"), indent=2)[:8000])


def _all_url_fields(obj, found=None):
    """All downloadable artifact URLs on a session: logs, crash, video, screenshots."""
    if found is None:
        found = {}
    if isinstance(obj, dict):
        for k, v in obj.items():
            if isinstance(v, str) and v.startswith("http") and (
                "log" in k.lower() or "crash" in k.lower()
                or "video" in k.lower() or "screenshot" in k.lower()):
                found[k] = v
            elif isinstance(v, (dict, list)):
                _all_url_fields(v, found)
    elif isinstance(obj, list):
        for item in obj:
            _all_url_fields(item, found)
    return found


def _latest_session_id():
    data = _get_json(f"{BASE}/sessions?limit=1")
    rows = data.get("data") or data.get("sessions") or []
    if not rows:
        sys.exit("No sessions found for this account.")
    return rows[0].get("session_id")


def _ext_for(field):
    if "video" in field:
        return ".mp4"
    if "screenshot" in field:
        return ".zip"
    return ".log"


def cmd_logs(session_id):
    # Resolve "latest" (or no id) to the most recent session on the account.
    if session_id in (None, "latest"):
        session_id = _latest_session_id()
        print(f"Latest session: {session_id}")
    sess = _get_json(f"{BASE}/sessions/{session_id}").get("data", {})
    urls = _all_url_fields(sess)
    out_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "lt-logs", session_id)
    os.makedirs(out_dir, exist_ok=True)
    print(f"Saving artifacts to {out_dir}")
    for k, u in urls.items():
        for auth in (False, True):  # pre-signed URLs need no auth; API endpoints do
            try:
                raw = _get(u, auth=auth)
                path = os.path.join(out_dir, k + _ext_for(k))
                with open(path, "wb") as fh:
                    fh.write(raw)
                if _ext_for(k) == ".log":
                    text = raw.decode("utf-8", "ignore")
                    hits = [ln for ln in text.splitlines() if CRASH_PATS.search(ln)]
                    print(f"  {k}: {len(raw)} bytes, {len(hits)} crash-ish -> {path}")
                    for ln in hits[:40]:
                        print("    CRASH> " + ln.strip()[:200])
                else:
                    print(f"  {k}: {len(raw)} bytes -> {path}")
                break
            except urllib.error.HTTPError as e:
                if auth:
                    print(f"  {k}: HTTP {e.code}")
            except Exception as e:  # noqa: BLE001
                if auth:
                    print(f"  {k}: {e}")
    print("NOTE: the LT device log via API is capped at ~1MB (session start). For the FULL "
          "crash logcat, use the in-session capture written to urpay-mobile-tests/logcat/ on the "
          "execution machine (or the LT dashboard video).")


# --------------------------------------------------------------------------- #
# report: enumerate FAILED sessions, pull their data, analyze, write HTML.
# --------------------------------------------------------------------------- #
def _download(url):
    """Fetch a URL trying pre-signed (no-auth) first, then Basic auth. None on failure."""
    for auth in (False, True):
        try:
            return _get(url, auth=auth)
        except urllib.error.HTTPError:
            if auth:
                return None
        except Exception:  # noqa: BLE001
            if auth:
                return None
    return None


def _sessions(limit):
    data = _get_json(f"{BASE}/sessions?limit={limit}")
    return data.get("data") or data.get("sessions") or []


def cmd_report(limit=25, include_passed=False):
    sessions = _sessions(limit)
    targets = []
    for s in sessions:
        status = str(s.get("status_ind") or s.get("status") or "").lower()
        if include_passed or status in FAILED_STATUSES:
            targets.append(s)
    print(f"Scanned {len(sessions)} recent session(s); {len(targets)} to report.")
    rows = []
    for s in targets:
        sid = s.get("session_id")
        if not sid:
            continue
        detail = _get_json(f"{BASE}/sessions/{sid}").get("data", {})
        urls = _all_url_fields(detail)
        out_dir = os.path.join(BASEDIR, "lt-logs", sid)
        os.makedirs(out_dir, exist_ok=True)
        artifacts, all_issues, noise_total = {}, [], {}
        for k, u in urls.items():
            raw = _download(u)
            if raw is None:
                continue
            path = os.path.join(out_dir, k + _ext_for(k))
            with open(path, "wb") as fh:
                fh.write(raw)
            artifacts[k] = path
            if _ext_for(k) == ".log":
                issues, noise = analyze_text(raw.decode("utf-8", "ignore"), k)
                all_issues.extend(issues)
                for p, c in noise.items():
                    noise_total[p] = noise_total.get(p, 0) + c
        all_issues = _dedup(all_issues)
        rows.append(dict(session=s, detail=detail, issues=all_issues,
                         noise=noise_total, artifacts=artifacts))
        top = all_issues[0]["category"] if all_issues else "no app issue in logs"
        print(f"  {sid} | {s.get('status_ind','?'):<8} | {s.get('device','')} | "
              f"issues={len(all_issues)} | {top}")
    report_path = os.path.join(BASEDIR, "lt-logs", "report.html")
    os.makedirs(os.path.dirname(report_path), exist_ok=True)
    with open(report_path, "w", encoding="utf-8") as fh:
        fh.write(_build_html(rows))
    print(f"\nReport written: {report_path}")
    if not rows:
        print("No failed sessions in the scanned window (try a larger N, e.g. `report 100`).")


def _sev_class(sev):
    return {1: "crit", 2: "warn", 3: "info"}.get(sev, "info")


def _build_html(rows):
    ts = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    with_issues = sum(1 for r in rows if r["issues"])
    p = []
    p.append("<!doctype html><html><head><meta charset='utf-8'>")
    p.append("<title>URPay LambdaTest Failure Report</title><style>")
    p.append("body{font-family:Segoe UI,Arial,sans-serif;margin:24px;background:#0d1117;color:#e6edf3}")
    p.append("h1{font-size:22px}h2{font-size:16px;margin:0}")
    p.append(".sess{border:1px solid #30363d;border-radius:8px;margin:16px 0;padding:16px;background:#161b22}")
    p.append(".meta{color:#8b949e;font-size:13px;margin:4px 0 10px}")
    p.append(".issue{border-left:4px solid #444;padding:8px 12px;margin:8px 0;border-radius:4px;background:#0d1117}")
    p.append(".crit{border-color:#f85149}.warn{border-color:#d29922}.info{border-color:#58a6ff}")
    p.append(".cat{font-weight:600}.proc{color:#8b949e;font-size:12px}")
    p.append("pre{white-space:pre-wrap;word-break:break-word;background:#010409;padding:8px;"
             "border-radius:4px;font-size:12px;margin:6px 0 0;color:#c9d1d9}")
    p.append("a{color:#58a6ff}.ok{color:#3fb950}.noise{color:#6e7681;font-size:12px}")
    p.append(".badge{display:inline-block;padding:2px 8px;border-radius:10px;font-size:12px;background:#21262d}")
    p.append("</style></head><body>")
    p.append("<h1>URPay &mdash; LambdaTest Failure Report</h1>")
    p.append(f"<div class='meta'>Generated {html.escape(ts)} &middot; {len(rows)} failed session(s) "
             f"&middot; {with_issues} with app issues detected in logs</div>")
    for r in rows:
        s, d = r["session"], r["detail"]
        sid = s.get("session_id", "?")
        status = html.escape(str(s.get("status_ind") or s.get("status") or "?"))
        device = html.escape(" ".join(str(s.get(x, "")) for x in
                                       ("platform", "device", "os_version")).strip())
        name = html.escape(str(s.get("name") or d.get("name") or sid))
        video = d.get("video_url") or s.get("video_url") or ""
        p.append("<div class='sess'>")
        p.append(f"<h2>{name}</h2>")
        p.append(f"<div class='meta'><span class='badge'>{status}</span> &middot; {device} "
                 f"&middot; <code>{html.escape(sid)}</code></div>")
        links = []
        for k, path in r["artifacts"].items():
            rel = os.path.relpath(path, os.path.join(BASEDIR, "lt-logs"))
            links.append(f"<a href='{html.escape(rel)}'>{html.escape(k)}</a>")
        if video:
            links.append(f"<a href='{html.escape(video)}'>video</a>")
        if links:
            p.append("<div class='meta'>artifacts: " + " &middot; ".join(links) + "</div>")
        if r["issues"]:
            for it in r["issues"]:
                ev = html.escape("\n".join(it["evidence"]))
                p.append(f"<div class='issue {_sev_class(it['severity'])}'>")
                p.append(f"<span class='cat'>{html.escape(it['category'])}</span> "
                         f"<span class='proc'>{html.escape(str(it['process']))} "
                         f"&middot; log line {it['line_no']}</span>")
                p.append(f"<pre>{ev}</pre></div>")
        else:
            p.append("<div class='issue info'><span class='ok'>No app-attributed crash/error "
                     "found in the pulled logs.</span> Failure is likely an assertion or "
                     "element-not-found &mdash; check the video / Appium log.</div>")
        if r["noise"]:
            noise = ", ".join(f"{html.escape(pk)}\u00d7{c}"
                              for pk, c in sorted(r["noise"].items(), key=lambda x: -x[1]))
            p.append(f"<div class='noise'>ignored device/OS crash noise: {noise}</div>")
        p.append("</div>")
    p.append("</body></html>")
    return "".join(p)


if __name__ == "__main__":
    if not USER or not KEY:
        sys.exit("Missing LT_USERNAME / LT_ACCESS_KEY")
    mode = sys.argv[1] if len(sys.argv) > 1 else "list"
    arg = sys.argv[2] if len(sys.argv) > 2 else None
    if mode == "list":
        cmd_list(int(arg) if arg else 10)
    elif mode == "detail":
        cmd_detail(arg)
    elif mode == "logs":
        cmd_logs(arg)
    elif mode == "latest":
        cmd_logs("latest")
    elif mode == "report":
        cmd_report(int(arg) if arg and arg.isdigit() else 25)
    else:
        sys.exit(f"Unknown mode: {mode}")

