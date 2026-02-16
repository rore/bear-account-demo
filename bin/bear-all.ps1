$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$specDir = Join-Path $repoRoot 'spec'
$bearWrapper = Join-Path $PSScriptRoot 'bear.ps1'

$irFiles = @()
if (Test-Path $specDir) {
    $irFiles = @(Get-ChildItem -Path $specDir -Filter '*.bear.yaml' -File | Sort-Object Name)
}

if ($irFiles.Count -eq 0) {
    [Console]::Error.WriteLine('bear-all: No IR files found under spec/*.bear.yaml')
    [Console]::Error.WriteLine('bear-all: Create initial block IR, run compile, then rerun bear-all.')
    exit 64
}

foreach ($irFile in $irFiles) {
    $rel = Join-Path 'spec' $irFile.Name
    [Console]::WriteLine("bear-all: checking $rel")
    & $bearWrapper check $irFile.FullName --project $repoRoot
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
}

exit 0