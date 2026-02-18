param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$BearArgs
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$vendoredBearBat = Join-Path $repoRoot 'tools/bear-cli/bin/bear.bat'
$vendoredBearSh = Join-Path $repoRoot 'tools/bear-cli/bin/bear'
$localBearBat = Join-Path $repoRoot '.bear/tools/bear-cli/bin/bear.bat'
$localBearSh = Join-Path $repoRoot '.bear/tools/bear-cli/bin/bear'

if (Test-Path $vendoredBearBat) {
    & $vendoredBearBat @BearArgs
    exit $LASTEXITCODE
}

if (Test-Path $vendoredBearSh) {
    & $vendoredBearSh @BearArgs
    exit $LASTEXITCODE
}

if (Test-Path $localBearBat) {
    & $localBearBat @BearArgs
    exit $LASTEXITCODE
}

if (Test-Path $localBearSh) {
    & $localBearSh @BearArgs
    exit $LASTEXITCODE
}

$globalBear = Get-Command bear -ErrorAction SilentlyContinue
if ($null -ne $globalBear) {
    & bear @BearArgs
    exit $LASTEXITCODE
}

[Console]::Error.WriteLine('bear wrapper: missing BEAR CLI. Expected tools/bear-cli/bin/bear(.bat), .bear/tools/bear-cli/bin/bear(.bat), or bear on PATH.')
exit 127
