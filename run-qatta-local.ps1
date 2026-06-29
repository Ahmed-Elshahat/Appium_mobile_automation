param([string[]]$Suites)

$ErrorActionPreference = 'Continue'
$repo = 'D:\Mobile\WM\mobile-automation-WMV_Dev\appium\Appium_mobile_automation'
Set-Location $repo
$mvn = 'C:\Tools\apache-maven-3.9.8\bin\mvn.cmd'
$udid = 'R5CX73LLSPE'
$pv = '16'
$pkg = 'com.urpay.consumer.sit'
$act = 'com.urpay.consumer.sit.MainActivity'
$logDir = Join-Path $repo 'logs-wmv'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# 5 Qatta suites. ONE physical device => run SEQUENTIALLY (cannot parallelize on a single device).
# Pass -Suites to run a subset, e.g. -Suites pay-group-qatta-suite,reject-quick-qatta-suite
$allSuites = @(
  'edit-group-qatta-details',
  'pay-group-qatta-suite',
  'pay-quick-qatta-suite',
  'reject-group-qatta-suite',
  'reject-quick-qatta-suite'
)
$suites = if ($Suites) { $allSuites | Where-Object { $Suites -contains $_ } } else { $allSuites }
if (-not $suites) { Write-Host "No matching suites for: $Suites"; exit 1 }

# pm clear revokes all runtime permissions; with noReset=true Appium does NOT re-grant them,
# so the app's post-login permission prompts (contacts/notifications/etc.) overlay the screen
# and block dashboard + Qatta navigation. Pre-grant them after every clear to avoid the dialogs.
$grantPerms = @(
  'android.permission.READ_CONTACTS',
  'android.permission.WRITE_CONTACTS',
  'android.permission.POST_NOTIFICATIONS',
  'android.permission.CAMERA',
  'android.permission.ACCESS_FINE_LOCATION',
  'android.permission.ACCESS_COARSE_LOCATION',
  'android.permission.READ_PHONE_STATE',
  'android.permission.READ_EXTERNAL_STORAGE',
  'android.permission.WRITE_EXTERNAL_STORAGE'
)

function Reset-App {
  param([string]$phase)
  Write-Host "[$phase] force-stop + clear data + grant perms + relaunch $pkg"
  & adb -s $udid shell am force-stop $pkg              2>&1 | Out-Null
  & adb -s $udid shell pm clear $pkg                   2>&1 | Out-Null
  foreach ($p in $grantPerms) {
    & adb -s $udid shell pm grant $pkg $p              2>&1 | Out-Null   # tolerate perms not grantable on this API level
  }
  & adb -s $udid shell am start -n "$pkg/$act"         2>&1 | Out-Null
}

# Clean state before the first suite.
Reset-App -phase 'pre-run'

foreach ($s in $suites) {
  $log = Join-Path $logDir "local-$s.log"
  Write-Host "=== RUN $s ($(Get-Date -Format 'HH:mm:ss')) ==="
  & cmd /c "`"$mvn`" test -Dsuite=suites/$s.xml -Dprofile=sit-wmv -Dremote=false -Dudid=$udid -DplatformVersion=$pv -Dproject.build.directory=target-$s -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"
  # After EVERY suite: clear app data, kill it, relaunch.
  Reset-App -phase "post-$s"
}

"=== QATTA LOCAL RESULTS ($(Get-Date -Format 'HH:mm:ss')) ===" | Tee-Object -FilePath "$logDir\_qatta_local_summary.txt"
foreach ($s in $suites) {
  $log = Join-Path $logDir "local-$s.log"
  if (Test-Path $log) {
    $res  = (Select-String -Path $log -Pattern 'Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+' | Select-Object -Last 1).Line
    $build = if (Select-String -Path $log -Pattern 'BUILD SUCCESS' -Quiet) { 'SUCCESS' }
             elseif (Select-String -Path $log -Pattern 'BUILD FAILURE' -Quiet) { 'FAILURE' }
             else { 'UNKNOWN' }
    $summary = if ($res) { $res.Trim() } else { '(no test summary)' }
    ("{0,-30} {1,-8} {2}" -f $s, $build, $summary) | Tee-Object -FilePath "$logDir\_qatta_local_summary.txt" -Append
  }
  else { ("{0,-30} NO-LOG" -f $s) | Tee-Object -FilePath "$logDir\_qatta_local_summary.txt" -Append }
}
"=== DONE ===" | Tee-Object -FilePath "$logDir\_qatta_local_summary.txt" -Append
