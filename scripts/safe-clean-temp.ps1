param(
    [switch]$Execute
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$allowed = @(
    '.gradle-user',
    '.bear-test-results',
    'build/generated/bear/.staging',
    'build/tmp/bear'
)

Write-Output 'safe-clean-temp: candidate targets:'
$allowed | ForEach-Object { Write-Output (" - " + $_) }

if (-not $Execute) {
    Write-Output 'safe-clean-temp: dry-run mode. Re-run with -Execute to remove existing targets.'
    exit 0
}

foreach ($rel in $allowed) {
    $target = Join-Path $repoRoot $rel
    if (-not (Test-Path $target)) {
        continue
    }

    $resolved = (Resolve-Path $target).Path
    if (-not $resolved.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to delete outside repo root: $resolved"
    }

    Remove-Item -Recurse -Force $resolved
    Write-Output ("removed: " + $rel)
}

Write-Output 'safe-clean-temp: done'
