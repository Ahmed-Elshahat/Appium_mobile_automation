$ErrorActionPreference = 'Continue'
$repo = 'D:\Mobile\WM\mobile-automation-WMV_Dev\appium\Appium_mobile_automation'
Set-Location $repo
$mvn = 'C:\Tools\apache-maven-3.9.8\bin\mvn.cmd'
$logDir = Join-Path $repo 'logs-wmv'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$suites = @(
  'auto-topup',
  'change-passcode-default-tier', 'change-passcode-default-tier-negative',
  'change-passcode-family-tier', 'change-passcode-family-tier-negative',
  'change-passcode-full-tier', 'change-passcode-full-tier-negative',
  'change-passcode-visitor-tier', 'change-passcode-visitor-tier-negative',
  'change-phone-number-full-tier', 'account-details', 'account-statement',
  'family-mission-suite', 'privacy-and-terms', 'rating', 'change-app-language',
  'registration-invitation-code', 'send-money-request-approve', 'validate-notifications',
  'edit-group-qatta-details', 'pay-group-qatta-suite', 'pay-quick-qatta-suite',
  'reject-group-qatta-suite', 'reject-quick-qatta-suite'
)
$max = 20

Get-Job | Where-Object Name -in $suites | Remove-Job -Force -ErrorAction SilentlyContinue

foreach ($s in $suites) {
  while (@(Get-Job -State Running).Count -ge $max) { Wait-Job -Any -Timeout 120 | Out-Null }
  Start-Job -Name $s -ArgumentList $repo, $s, $mvn -ScriptBlock {
    param($repo, $s, $mvn)
    Set-Location $repo
    $log = Join-Path $repo "logs-wmv\$s.log"
    & cmd /c "`"$mvn`" test -Dsuite=suites/$s.xml -Dprofile=sit-wmv -Dproject.build.directory=target-$s -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"
  } | Out-Null
  Start-Sleep -Milliseconds 1500   # stagger session creation so LambdaTest device allocation doesn't burst
}

Get-Job | Where-Object Name -in $suites | Wait-Job | Out-Null

"=== WMV PARALLEL RESULTS ($(Get-Date -Format 'HH:mm:ss')) ===" | Tee-Object -FilePath "$logDir\_summary.txt"
foreach ($s in $suites) {
  $log = Join-Path $logDir "$s.log"
  if (Test-Path $log) {
    $res  = (Select-String -Path $log -Pattern 'Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+' | Select-Object -Last 1).Line
    $build = if (Select-String -Path $log -Pattern 'BUILD SUCCESS' -Quiet) { 'SUCCESS' }
             elseif (Select-String -Path $log -Pattern 'BUILD FAILURE' -Quiet) { 'FAILURE' }
             else { 'UNKNOWN' }
    $summary = if ($res) { $res.Trim() } else { '(no test summary)' }
    ("{0,-40} {1,-8} {2}" -f $s, $build, $summary) | Tee-Object -FilePath "$logDir\_summary.txt" -Append
  }
  else { ("{0,-40} NO-LOG" -f $s) | Tee-Object -FilePath "$logDir\_summary.txt" -Append }
}
Get-Job | Where-Object Name -in $suites | Remove-Job -Force
"=== DONE ===" | Tee-Object -FilePath "$logDir\_summary.txt" -Append
