$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
$blocksFile = Join-Path $repoRoot 'bear.blocks.yaml'
$specDir = Join-Path $repoRoot 'spec'
$bearWrapper = Join-Path $PSScriptRoot 'bear.ps1'

if (Test-Path $blocksFile) {
    & $bearWrapper check --all --project $repoRoot
    exit $LASTEXITCODE
}

$irFiles = @()
if (Test-Path $specDir) {
    $irFiles = @(Get-ChildItem -Path $specDir -Filter '*.bear.yaml' -File | Sort-Object Name)
}

if ($irFiles.Count -eq 0) {
    [Console]::Error.WriteLine('bear-all: No BEAR block index or IR files found')
    [Console]::Error.WriteLine('bear-all: Create initial IR file(s), create bear.blocks.yaml, compile, then rerun bear-all.')
    exit 64
}

if ($irFiles.Count -ge 2) {
    [Console]::Error.WriteLine('bear-all: Multiple IR files found but bear.blocks.yaml is missing')
    [Console]::Error.WriteLine('bear-all: Create bear.blocks.yaml and run bear check --all --project ., then rerun bear-all.')
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