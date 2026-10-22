# Copy the current GT6CE addon SDK, and the full mod jars when they exist, into libs/.
# libs/ is preferred over the sibling checkout. Jars in libs/ stay out of git.
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$gt6 = if ($env:GT6CE_DIR) { $env:GT6CE_DIR } else { Join-Path (Split-Path -Parent $repo) 'GregTech6CommunityEdition\GregTech6' }
$dest = Join-Path $repo 'libs'
New-Item -ItemType Directory -Force -Path $dest | Out-Null

$files = @(
    'multiversion\versions\1.20.1-forge\build\addon-sdk\gt6ce-addon-sdk-1.20.1-forge-0.0.0.jar',
    'multiversion\versions\1.21.1-neoforge\build\addon-sdk\gt6ce-addon-sdk-1.21.1-neoforge-0.0.0.jar',
    'multiversion\versions\1.20.1-forge\build\libs\gt6ce-1.20.1-forge-0.0.0.jar',
    'multiversion\versions\1.21.1-neoforge\build\libs\gt6ce-neoforge-1.21.1-0.0.0.jar'
)
$copied = 0
foreach ($relative in $files) {
    $source = Join-Path $gt6 $relative
    if (-not (Test-Path -LiteralPath $source)) {
        Write-Output "skip  $relative"
        continue
    }
    Copy-Item -LiteralPath $source -Destination (Join-Path $dest (Split-Path $relative -Leaf)) -Force
    Write-Output "copy  $(Split-Path $relative -Leaf)"
    $copied++
}
if ($copied -lt 2) {
    throw "Need both addon SDK jars from $gt6. Build them with the GT6CE addonSdkJar tasks."
}
