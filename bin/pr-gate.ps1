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
if (-not (Test-Path $repoRoot)) {
    $resolved = git -C $scriptDir rev-parse --show-toplevel 2>$null
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($resolved)) {
        [Console]::Error.WriteLine('pr-gate: unable to resolve repo root')
        exit 74
    }
    $repoRoot = $resolved.Trim()
}

$specDir = Join-Path $repoRoot 'spec'
$bearWrapper = Join-Path $scriptDir 'bear.ps1'
$irFiles = @()
if (Test-Path $specDir) {
    $irFiles = @(Get-ChildItem -Path $specDir -Filter '*.bear.yaml' -File | Sort-Object Name)
}
if ($irFiles.Count -eq 0) {
    [Console]::Error.WriteLine('pr-gate: No IR files found under spec/*.bear.yaml')
    [Console]::Error.WriteLine('pr-gate: Create initial block IR, run compile, then rerun pr-gate.')
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
