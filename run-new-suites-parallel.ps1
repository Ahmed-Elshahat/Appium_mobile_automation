param(
  [string]$Env = 'SIT',        # target environment: SIT | UAT  (passed as -Denv)
  [string]$Profile = 'sit-wmv' # config profile / data set (passed as -Dprofile)
)
# ---------------------------------------------------------------------------
# Runs the newly-migrated WMV suites on LambdaTest (default remote=true).
# Each wave = ONE mvn process (single target/ dir → no resources file-lock), and
# real parallelism comes from TestNG (parallel="tests" in the suite XML) +
# DriverFactory's ThreadLocal<AppiumDriver> (one LambdaTest session per <test>).
#
# WAVE 1 (parallel): suites/new-suites-parallel.xml — 5 non-family suites whose
#   accounts are pairwise-distinct, run concurrently (thread-count=5):
#     reject-money-request                     (0506913889 / 0520154446)
#     wallet-balance                           (0529193979)
#     invite-friends                           (0577977177)
#     forgot-passcode-default-tier-negative    (0520110197)
#     forgot-passcode-default-tier-negative-02 (0591181431)
#
# WAVE 2 (after wave 1): suites/family-wallet.xml — parallel="none" (Send Gift →
#   Kid Receive → Family Request → Direct Top-up +/- → Permission → Delink). These
#   journeys share scarce parent/kid accounts (0520154446 / 0506913889 / 0567982178)
#   — the same accounts wave 1's reject job uses — so it runs sequentially, AFTER
#   wave 1's process exits (the two never contend for target/).
#
# Run: powershell -File run-new-suites-parallel.ps1
#      powershell -File run-new-suites-parallel.ps1 -Env UAT -Profile uat-wmv
# ---------------------------------------------------------------------------
$ErrorActionPreference = 'Continue'
$repo = 'D:\Mobile\WM\mobile-automation-WMV_Dev\appium\Appium_mobile_automation'
Set-Location $repo
$mvn = 'C:\Tools\apache-maven-3.9.8\bin\mvn.cmd'
$logDir = Join-Path $repo 'logs-wmv'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# Each wave is ONE mvn process (single target/ dir, no file-lock contention).
# Wave 1 gets real parallelism from TestNG (parallel="tests" in the suite XML +
# DriverFactory ThreadLocal → one LambdaTest session per <test>).
$wave1Suite = 'new-suites-parallel'   # 5 non-family tests, parallel="tests"
$wave2Suite = 'family-wallet'         # family tests, parallel="none" (shared accounts)

function Invoke-Suite($suite) {
  $log = Join-Path $logDir "$suite.log"
  & cmd /c "`"$mvn`" test -Dsuite=suites/$suite.xml -Dprofile=$Profile -Denv=$Env -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"
}

Invoke-Suite $wave1Suite
Invoke-Suite $wave2Suite

$all = @($wave1Suite, $wave2Suite)
$summaryFile = Join-Path $logDir '_new_suites_summary.txt'
"=== NEW SUITES PARALLEL RESULTS ($(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')) ===" | Tee-Object -FilePath $summaryFile
foreach ($s in $all) {
  $log = Join-Path $logDir "$s.log"
  if (Test-Path $log) {
    $res  = (Select-String -Path $log -Pattern 'Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+' | Select-Object -Last 1).Line
    $build = if (Select-String -Path $log -Pattern 'BUILD SUCCESS' -Quiet) { 'SUCCESS' }
             elseif (Select-String -Path $log -Pattern 'BUILD FAILURE' -Quiet) { 'FAILURE' }
             else { 'UNKNOWN' }
    $summary = if ($res) { $res.Trim() } else { '(no test summary)' }
    ("{0,-42} {1,-8} {2}" -f $s, $build, $summary) | Tee-Object -FilePath $summaryFile -Append
  }
  else { ("{0,-42} NO-LOG" -f $s) | Tee-Object -FilePath $summaryFile -Append }
}
"=== DONE ===" | Tee-Object -FilePath $summaryFile -Append
