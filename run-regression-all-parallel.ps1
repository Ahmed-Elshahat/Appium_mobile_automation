param(
  [string]$Env      = 'SIT',          # target environment: SIT | UAT  (-Denv)
  [switch]$Remote   = $true,          # run on LambdaTest (-Dremote=true). Use -Remote:$false for local.
  [int]$TimeoutMin  = 180             # overall wait cap (minutes)
)
# ─────────────────────────────────────────────────────────────────────────────
# Runs the FOUR per-profile parallel regression suites AT THE SAME TIME.
# Each profile is a separate mvn JVM (one -Dprofile per run), its own log and its
# own target-* dir so they never collide.
#
#   WMV        -> regression-wmv-parallel.xml        (-Dprofile=sit-wmv)
#   CARDS      -> regression-cards-parallel.xml      (-Dprofile=sit-cards)
#   REMITTANCE -> regression-remittance-parallel.xml (-Dprofile=sit-remittance)
#   DMP        -> regression-dmp-parallel.xml        (-Dprofile=sit-dmp-all)
#
# DEVICE BUDGET: thread-counts are set to wmv=9 + cards=4 + remittance=2 + dmp=4 = 19,
#   matching the LambdaTest 19-device cap, so all four run together with no queuing.
#   To change the cap, edit thread-count in each XML so the four still sum to your max.
#
# Usage:
#   .\run-regression-all-parallel.ps1                 # SIT, LambdaTest
#   .\run-regression-all-parallel.ps1 -Env UAT
#   .\run-regression-all-parallel.ps1 -Remote:$false  # local devices
# ─────────────────────────────────────────────────────────────────────────────
$ErrorActionPreference = 'Continue'
# Repo = the folder this script lives in (portable across machines).
$repo = if ($PSScriptRoot) { $PSScriptRoot } else { (Get-Location).Path }
Set-Location $repo
# Resolve Maven: prefer it on PATH, else fall back to known install locations.
$mvn = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $mvn) {
  foreach ($p in @(
      'C:\Tools\apache-maven-3.9.8\bin\mvn.cmd',
      'D:\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin\mvn.cmd')) {
    if (Test-Path $p) { $mvn = $p; break }
  }
}
if (-not $mvn) { Write-Error "Maven (mvn) not found on PATH. Install Maven or set `$mvn in this script."; exit 1 }
Write-Host "Repo: $repo"
Write-Host "Maven: $mvn`n"
$logDir = Join-Path $repo 'logs-regression'
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# ── Preflight: compile once so a build error fails fast with a clear message
#    (instead of three silent "(no test summary)" FAILUREs).
Write-Host "Preflight: mvn clean test-compile ..."
$preLog = Join-Path $logDir '_preflight.log'
& cmd /c "`"$mvn`" -q clean test-compile > `"$preLog`" 2>&1"
if ($LASTEXITCODE -ne 0) {
  Write-Host "`nPREFLIGHT COMPILE FAILED - fix this before running. Last 30 lines:`n" -ForegroundColor Red
  Get-Content $preLog -Tail 30 | Write-Host
  Write-Host "`nFull preflight log: $preLog"
  exit 1
}
Write-Host "Preflight OK.`n"

$remoteFlag = if ($Remote) { 'true' } else { 'false' }

# name | suite file | profile
$runs = @(
  @{ Name = 'wmv';        Suite = 'regression-wmv-parallel.xml';        Profile = 'sit-wmv' },
  @{ Name = 'cards';      Suite = 'regression-cards-parallel.xml';      Profile = 'sit-cards' },
  @{ Name = 'remittance'; Suite = 'regression-remittance-parallel.xml'; Profile = 'sit-remittance' },
  @{ Name = 'dmp';        Suite = 'regression-dmp-parallel.xml';        Profile = 'sit-dmp-all' }
)

Get-Job | Where-Object Name -in ($runs.Name) | Remove-Job -Force -ErrorAction SilentlyContinue

foreach ($r in $runs) {
  Start-Job -Name $r.Name -ArgumentList $repo, $mvn, $r.Suite, $r.Profile, $r.Name, $Env, $remoteFlag -ScriptBlock {
    param($repo, $mvn, $suite, $profile, $name, $env, $remoteFlag)
    Set-Location $repo
    $log = Join-Path $repo "logs-regression\$name.log"
    & cmd /c "`"$mvn`" clean test -Dsuite=suites/$suite -Dprofile=$profile -Denv=$env -Dremote=$remoteFlag -Dproject.build.directory=target-$name -Dmaven.test.failure.ignore=true > `"$log`" 2>&1"
  } | Out-Null
  Write-Host "Launched $($r.Name)  ($($r.Suite), profile=$($r.Profile))"
  Start-Sleep -Seconds 3   # stagger so LambdaTest device allocation doesn't burst
}

Write-Host "`nAll four suites running in parallel. Waiting (timeout ${TimeoutMin}m)..."
Get-Job | Where-Object Name -in ($runs.Name) | Wait-Job -Timeout ($TimeoutMin * 60) | Out-Null

$summaryFile = Join-Path $logDir '_summary.txt'
"=== REGRESSION PARALLEL RESULTS ($(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')) ===" | Tee-Object -FilePath $summaryFile
foreach ($r in $runs) {
  $log = Join-Path $logDir "$($r.Name).log"
  if (Test-Path $log) {
    $res = (Select-String -Path $log -Pattern 'Tests run: \d+, Failures: \d+, Errors: \d+, Skipped: \d+' | Select-Object -Last 1).Line
    $build = if (Select-String -Path $log -Pattern 'BUILD SUCCESS' -Quiet) { 'SUCCESS' }
             elseif (Select-String -Path $log -Pattern 'BUILD FAILURE' -Quiet) { 'FAILURE' }
             else { 'UNKNOWN' }
    $summary = if ($res) { $res.Trim() } else { '(no test summary)' }
    ("{0,-12} {1,-8} {2}" -f $r.Name, $build, $summary) | Tee-Object -FilePath $summaryFile -Append
    if ($build -ne 'SUCCESS') {
      Write-Host "`n----- $($r.Name): last 25 lines of $log -----" -ForegroundColor Yellow
      Get-Content $log -Tail 25 | Write-Host
    }
  }
  else { ("{0,-12} NO-LOG" -f $r.Name) | Tee-Object -FilePath $summaryFile -Append }
}
Get-Job | Where-Object Name -in ($runs.Name) | Remove-Job -Force
"=== DONE ===" | Tee-Object -FilePath $summaryFile -Append
Write-Host "`nLogs: $logDir   Summary: $summaryFile"
