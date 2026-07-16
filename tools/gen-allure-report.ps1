# ─────────────────────────────────────────────────────────────────────────────
#  Generate the Allure report WITH cross-run history (trend + flaky detection).
#
#  Why this script: `mvn clean` wipes target/ and allure-results/ every run, so
#  Allure's history (trend graph, flaky classification, retries collapse) is lost.
#  This script persists history OUTSIDE those folders in a git-ignored
#  `.allure-history/` at the repo root, feeding it back before each generate.
#
#  Flow:
#    1. Seed allure-results/history/ from the persisted .allure-history/ (if any).
#    2. Generate the normal report  -> target/allure-report        (produces history/trend).
#    3. Persist target/allure-report/history/ back to .allure-history/ for next run.
#    4. Generate the single-file report -> target/allure-single-report (shareable index.html).
#
#  Usage (from repo root, after a test run):
#    powershell -ExecutionPolicy Bypass -File tools/gen-allure-report.ps1
# ─────────────────────────────────────────────────────────────────────────────
param(
    [string]$Results = "allure-results",
    [string]$Report  = "target/allure-report",
    [string]$Single  = "target/allure-single-report",
    [string]$HistoryStore = ".allure-history"
)

$ErrorActionPreference = "Stop"

function Resolve-Allure {
    $cmd = Get-Command allure -ErrorAction SilentlyContinue
    if ($cmd) { return "allure" }
    $local = Join-Path $PSScriptRoot "..\.allure\bin\allure.bat"
    if (Test-Path $local) { return (Resolve-Path $local).Path }
    throw "Allure CLI not found. Install it or place it under .allure/bin/allure.bat"
}

if (-not (Test-Path $Results)) {
    throw "Results folder '$Results' not found. Run the tests first."
}

$allure = Resolve-Allure

# 1. Seed history from the persisted store so the trend accumulates across runs.
if (Test-Path (Join-Path $HistoryStore "*")) {
    $dest = Join-Path $Results "history"
    New-Item -ItemType Directory -Force -Path $dest | Out-Null
    Copy-Item -Path (Join-Path $HistoryStore "*") -Destination $dest -Recurse -Force
    Write-Host "Seeded history from $HistoryStore -> $dest"
} else {
    Write-Host "No prior history in $HistoryStore (first run) — trend starts now."
}

# 2. Normal report (this is the one that emits an updated history/ folder).
& $allure generate $Results -o $Report --clean
Write-Host "Report generated -> $Report"

# 3. Persist the freshly-computed history back to the store for the next run.
$newHistory = Join-Path $Report "history"
if (Test-Path $newHistory) {
    New-Item -ItemType Directory -Force -Path $HistoryStore | Out-Null
    Copy-Item -Path (Join-Path $newHistory "*") -Destination $HistoryStore -Recurse -Force
    Write-Host "Persisted history -> $HistoryStore"
}

# 4. Single-file report for easy sharing (history/trend embedded).
& $allure generate $Results --single-file -o $Single --clean
Write-Host "Single-file report -> $Single/index.html"
