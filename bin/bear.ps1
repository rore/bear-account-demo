param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$BearArgs
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$localBearBat = Join-Path $repoRoot '.bear/tools/bear-cli/bin/bear.bat'
$localBearSh = Join-Path $repoRoot '.bear/tools/bear-cli/bin/bear'

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

[Console]::Error.WriteLine('bear wrapper: missing BEAR CLI. Expected .bear/tools/bear-cli/bin/bear(.bat) or bear on PATH.')
exit 127