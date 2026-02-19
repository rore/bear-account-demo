param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$ArgsList
)

$ErrorActionPreference = 'Stop'
if ($ArgsList.Count -ne 1) {
    [Console]::Error.WriteLine('usage: pr-gate.ps1 <base-ref> (expected remote-tracking ref, e.g. origin/main)')
    exit 64
}
$baseRef = $ArgsList[0]
if (-not $baseRef.StartsWith('origin/')) {
    [Console]::Error.WriteLine('usage: pr-gate.ps1 <base-ref> (expected remote-tracking ref, e.g. origin/main)')
    exit 64
}

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = Split-Path -Parent $scriptDir
$blocksFile = Join-Path $repoRoot 'bear.blocks.yaml'
$bearWrapper = Join-Path $scriptDir 'bear.ps1'

if (Test-Path $blocksFile) {
    & $bearWrapper pr-check --all --project $repoRoot --base $baseRef
    exit $LASTEXITCODE
}

$specDir = Join-Path $repoRoot 'spec'
$irFiles = @()
if (Test-Path $specDir) {
    $irFiles = @(Get-ChildItem -Path $specDir -Filter '*.bear.yaml' -File | Sort-Object Name)
}
if ($irFiles.Count -eq 0) {
    [Console]::Error.WriteLine('pr-gate: No BEAR block index or IR files found')
    [Console]::Error.WriteLine('pr-gate: Create initial IR file(s), create bear.blocks.yaml, compile, then rerun pr-gate.')
    exit 64
}

foreach ($irFile in $irFiles) {
    $rel = "spec/$($irFile.Name)"
    [Console]::WriteLine("pr-gate: checking $rel against $baseRef")
    & $bearWrapper pr-check $rel --project $repoRoot --base $baseRef
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

exit 0
