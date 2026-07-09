param(
  [string]$Env = 'SIT',        # target environment: SIT | UAT  (passed as -Denv)
  [string]$Profile = 'sit-wmv' # config profile / data set (passed as -Dprofile)
)
# ---------------------------------------------------------------------------
# Runs the AUTH + SETTINGS + WALLET test folders on LambdaTest.
# DEVICE CAP: never more than 15 concurrent LambdaTest sessions.
#   - Each POOL suite is single-session (parallel="none"/default => 1 session while it runs),
#     so capping the job pool at 15 => at most 15 devices in use at once.
#   - Multi-session aggregate suites (parallel="tests") are EXCLUDED from the pool because one
#     such job alone would open many sessions (e.g. all-wallet-settings = thread-count 15).
#   - Each suite = its own mvn job with -Dproject.build.directory=target-<suite> so the
#     concurrent builds never contend for a shared target/ (test-classes) dir.
# POST phase (sequential, after the pool drains): the family-account suites, which share the
#   scarce parent/kid accounts (0520154446 / 0506913889 / 0567982178) and would otherwise cause
#   account-collision false failures if run concurrently.
# ---------------------------------------------------------------------------
$ErrorActionPreference = 'Continue'
$repo = 'D:\Mobile\WM\mobile-automation-WMV_Dev\appium\Appium_mobile_automation'
Set-Location $repo
$mvn = 'C:\Tools\apache-maven-3.9.8\bin\mvn.cmd'
$logDir = Join-Path $repo 'logs-wmv'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# --- Parallel pool (max 15 concurrent devices; every entry is a single-session suite) ---
$pool = @(
  # AUTH folder (forgot-passcode all tiers + smoke)
  'smoke',
  'forgot-passcode-default-tier', 'forgot-passcode-default-tier-negative', 'forgot-passcode-default-tier-negative-02',
  'forgot-passcode-family-tier', 'forgot-passcode-family-tier-negative', 'forgot-passcode-family-tier-negative-02',
  'forgot-passcode-full-tier', 'forgot-passcode-full-tier-negative', 'forgot-passcode-full-tier-negative-02',
  'forgot-passcode-visitor-tier', 'forgot-passcode-visitor-tier-negative', 'forgot-passcode-visitor-tier-negative-02',
  # SETTINGS folder (change-passcode all tiers + language / phone / privacy / rating)
  'change-app-language', 'change-phone-number-full-tier', 'privacy-and-terms', 'rating',
  'change-passcode-default-tier', 'change-passcode-default-tier-negative',
  'change-passcode-family-tier', 'change-passcode-family-tier-negative',
  'change-passcode-full-tier', 'change-passcode-full-tier-negative',
  'change-passcode-visitor-tier', 'change-passcode-visitor-tier-negative',
  # WALLET folder - non-family (single-session; account-distinct)
  'account-details', 'account-statement', 'invite-friends', 'wallet-balance',
  'validate-notifications', 'send-money-request-approve', 'reject-money-request',
  'registration-invitation-code', 'edit-group-qatta-details',
  'pay-group-qatta-suite', 'pay-quick-qatta-suite', 'reject-group-qatta-suite', 'reject-quick-qatta-suite'
)
# --- Run sequentially AFTER the pool (WALLET family-account suites; share parent/kid accounts) ---
#  family-wallet = SendGift, KidReceiveGift, FamilyRequest, DirectTopup +/-, Permission, Delink.
$post = @('auto-topup', 'family-mission-suite', 'family-wallet')

$all = $pool + $post
$max = 15

Get-Job | Where-Object Name -in $all | Remove-Job -Force -ErrorAction SilentlyContinue

foreach ($s in $pool) {
  # Throttle to $max concurrent jobs. NOTE: do NOT use `Wait-Job -Any` without piping the jobs in —
  # with no job specified it treats the mandatory job parameter as missing and PROMPTS for Id[0],
  # which blocks the loop forever after the first $max jobs. Poll the running-job count instead.
  while (@(Get-Job -State Running).Count -ge $max) { Start-Sleep -Seconds 5 }
  Start-Job -Name $s -ArgumentList $repo, $s, $mvn, $Env, $Profile -ScriptBlock {
    param($repo, $s, $mvn, $Env, $Profile)
    Set-Location $repo
    $log = Join-Path $repo "logs-wmv\$s.log"
    & cmd /c "`"$mvn`" test -Dsuite=suites/$s.xml -Dprofile=$Profile -Denv=$Env -Dproject.build.directory=target-$s -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"
  } | Out-Null
  Start-Sleep -Milliseconds 1500   # stagger session creation so LambdaTest device allocation doesn't burst
}

# Wait for the whole pool to finish before the shared-account family suite.
Get-Job | Where-Object Name -in $pool | Wait-Job | Out-Null

foreach ($s in $post) {
  $log = Join-Path $logDir "$s.log"
  & cmd /c "`"$mvn`" test -Dsuite=suites/$s.xml -Dprofile=$Profile -Denv=$Env -Dproject.build.directory=target-$s -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"
}

# --- Summary ---
$summaryFile = Join-Path $logDir '_all_selected_summary.txt'
"=== ALL SELECTED SUITES ($(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')) ===" | Tee-Object -FilePath $summaryFile
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
Get-Job | Where-Object Name -in $all | Remove-Job -Force -ErrorAction SilentlyContinue
"=== DONE ===" | Tee-Object -FilePath $summaryFile -Append
