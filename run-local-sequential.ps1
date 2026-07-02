param(
  [string]$Udid = 'R5CX73LLSPE',
  [string]$Pv   = '16',
  [string]$Profile = 'sit-wmv'
)
# Runs ALL wallet + settings suites SEQUENTIALLY on the local device.
# Before each suite (and before the first), clears the app data so every suite starts fresh.
$ErrorActionPreference = 'Continue'
$repo = 'D:\Mobile\WM\mobile-automation-WMV_Dev\appium\Appium_mobile_automation'
Set-Location $repo
$mvn = 'C:\Tools\apache-maven-3.9.8\bin\mvn.cmd'
$pkg = 'com.urpay.consumer.sit'
$logDir = Join-Path $repo 'logs-local-seq'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# Keep the screen awake for the whole run. The device has a SECURE lock: if the screen times out
# it engages the keyguard and every suite then fails at login with a 75s landing timeout (Appium
# only sees the lock screen). stayon + a long screen-off timeout prevents auto-lock while on USB.
& adb -s $Udid shell svc power stayon true | Out-Null
& adb -s $Udid shell settings put system screen_off_timeout 1800000 | Out-Null

$suites = @(
  # ── Wallet ──
  'auto-topup','account-details','account-statement','family-mission-suite',
  'registration-invitation-code','send-money-request-approve','validate-notifications',
  'edit-group-qatta-details','pay-group-qatta-suite','pay-quick-qatta-suite',
  'reject-group-qatta-suite','reject-quick-qatta-suite',
  # ── Settings ──
  'change-passcode-default-tier','change-passcode-default-tier-negative',
  'change-passcode-family-tier','change-passcode-family-tier-negative',
  'change-passcode-full-tier','change-passcode-full-tier-negative',
  'change-passcode-visitor-tier','change-passcode-visitor-tier-negative',
  'change-phone-number-full-tier','privacy-and-terms','rating','change-app-language'
)

$summary = Join-Path $logDir '_summary.txt'
"=== LOCAL SEQUENTIAL RUN $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') (device $Udid) ===" | Set-Content $summary

$first = $true
foreach ($s in $suites) {
  Write-Host "[$(Get-Date -Format HH:mm:ss)] Clearing app data + running: $s"
  # Wake the screen and dismiss a (non-secure) keyguard before each suite as a safety net.
  & adb -s $Udid shell input keyevent KEYCODE_WAKEUP | Out-Null
  & adb -s $Udid shell wm dismiss-keyguard | Out-Null
  # Clear app data so each suite starts from a clean state (fresh onboarding/login).
  & adb -s $Udid shell pm clear $pkg | Out-Null
  Start-Sleep -Seconds 2

  # 'clean' only on the first suite: wipes any stale bytecode (e.g. classes compiled by a
  # different JDK) once; subsequent suites reuse the compiled classes for speed.
  $goal = if ($first) { 'clean test' } else { 'test' }
  $first = $false

  $log = Join-Path $logDir "$s.log"
  & cmd /c "`"$mvn`" $goal -Dsuite=suites/$s.xml -Dprofile=$Profile -Dremote=false -Dudid=$Udid -DplatformVersion=$Pv -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"

  $res = (Select-String -Path $log -Pattern 'Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+' | Select-Object -Last 1).Line
  $build = if (Select-String -Path $log -Pattern 'BUILD SUCCESS' -Quiet) { 'SUCCESS' }
           elseif (Select-String -Path $log -Pattern 'BUILD FAILURE' -Quiet) { 'FAILURE' }
           else { 'UNKNOWN' }
  ("{0,-40} {1,-8} {2}" -f $s, $build, ($(if ($res) { $res.Trim() } else { '(no summary)' }))) | Add-Content $summary
}
"=== DONE $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') ===" | Add-Content $summary
Write-Host "All suites finished. Summary: $summary"
