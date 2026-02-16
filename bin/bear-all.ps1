$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$irPath = Join-Path $repoRoot 'spec/withdraw.bear.yaml'
$bearWrapper = Join-Path $PSScriptRoot 'bear.ps1'

if (-not (Test-Path $irPath)) {
    [Console]::Error.WriteLine("bear-all: missing IR file: $irPath")
    exit 64
}

& $bearWrapper check $irPath --project $repoRoot
exit $LASTEXITCODE