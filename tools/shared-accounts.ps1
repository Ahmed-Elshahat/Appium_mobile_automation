# Cross-reference: which login account (mobileNumber) each test class uses, then
# group by account to find any account shared by 2+ DISTINCT test classes.
$cfgFiles = @(
    'src\test\resources\config\default.properties',
    'src\test\resources\config\sit-wmv.properties',
    'src\test\resources\config\sit-cards.properties',
    'src\test\resources\config\sit-remittance.properties'
)
# Build merged key -> value map
$cfg = @{}
foreach ($f in $cfgFiles) {
    if (Test-Path $f) {
        Get-Content $f | ForEach-Object {
            if ($_ -match '^\s*([A-Za-z0-9_.]+)\s*=\s*(.*?)\s*$' -and $_ -notmatch '^\s*#') {
                $cfg[$Matches[1]] = $Matches[2]
            }
        }
    }
}
function Resolve-Num([string]$raw) {
    if ([string]::IsNullOrWhiteSpace($raw)) { return '(empty)' }
    ($raw -replace '^\+?966', '0') -replace '^0?5', '05'
}

# For each test class, collect the mobileNumber KEYS it references literally
$map = @{}   # account -> set of test classes
$testFiles = Get-ChildItem -Recurse 'src\test\java\com\urpay\tests' -Filter *Test.java
foreach ($tf in $testFiles) {
    $cls = $tf.BaseName
    $text = Get-Content $tf.FullName -Raw
    $keys = [regex]::Matches($text, '"([A-Za-z0-9_.]+\.mobileNumber)"') | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique
    foreach ($k in $keys) {
        if ($cfg.ContainsKey($k)) {
            $num = Resolve-Num $cfg[$k]
            if ($num -eq '(empty)') { continue }
            if (-not $map.ContainsKey($num)) { $map[$num] = New-Object System.Collections.Generic.HashSet[string] }
            [void]$map[$num].Add("$cls ($k)")
        }
    }
}

$out = Join-Path $PSScriptRoot 'shared-accounts.txt'
Set-Content $out "Accounts shared by 2+ DISTINCT test classes (literal .mobileNumber refs):"
Add-Content $out ""
$found = $false
foreach ($kv in ($map.GetEnumerator() | Sort-Object { $_.Value.Count } -Descending)) {
    $classes = @($kv.Value | ForEach-Object { ($_ -replace ' \(.*$','') } | Sort-Object -Unique)
    if ($classes.Count -gt 1) {
        $found = $true
        Add-Content $out ("`n{0}  -> {1} test classes:" -f $kv.Key, $classes.Count)
        $kv.Value | Sort-Object | ForEach-Object { Add-Content $out "    $_" }
    }
}
if (-not $found) { Add-Content $out '(none via literal keys)' }
Add-Content $out "`nNOTE: prefix-indirection classes NOT captured here (check manually):"
Add-Content $out "  MoneyGramTransferTest (loginAndTransfer prefix), TelecomRechargeTest, AbstractChangePasscode*/AbstractForgotPasscode* (keyPrefix())."
