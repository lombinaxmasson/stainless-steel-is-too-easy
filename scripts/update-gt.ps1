# Copy the current GT6CE common API jar, both platform SDKs, and the full mod jars when they exist, into libs/.
# libs/ is preferred over the sibling checkout. Jars in libs/ stay out of git.
$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
$gt6 = if ($env:GT6CE_DIR) { $env:GT6CE_DIR } else { Join-Path (Split-Path -Parent $repo) 'GregTech6CommunityEdition\GregTech6' }
$dest = Join-Path $repo 'libs'
New-Item -ItemType Directory -Force -Path $dest | Out-Null

# Versions come from the addon's gradle.properties so the two files cannot drift.
$props = @{}
Get-Content (Join-Path $repo 'gradle.properties') | ForEach-Object {
    if ($_ -match '^\s*([A-Za-z0-9_.]+)\s*=\s*(.+)$') { $props[$Matches[1]] = $Matches[2].Trim() }
}
$modVersion = $props['gt6ce_version']
$apiVersion = $props['gt6ce_api_version']
$mcVersion = $props['minecraft_version']

$required = @(
    "api\common\build\libs\gt6ce-api-$apiVersion.jar",
    "minecraft\versions\$mcVersion-forge\build\addon-sdk\gt6ce-addon-sdk-$mcVersion-forge-$apiVersion.jar",
    "minecraft\versions\1.21.1-neoforge\build\addon-sdk\gt6ce-addon-sdk-1.21.1-neoforge-$apiVersion.jar"
)
# Runtime jars are optional: their build is slow and only the dev game needs them.
$optional = @(
    "minecraft\versions\$mcVersion-forge\build\libs\gt6ce-$mcVersion-forge-$modVersion.jar",
    "minecraft\versions\1.21.1-neoforge\build\libs\gt6ce-neoforge-1.21.1-$modVersion.jar"
)

foreach ($relative in $required) {
    $source = Join-Path $gt6 $relative
    if (-not (Test-Path -LiteralPath $source)) {
        throw "Missing $relative in $gt6. Build it with the GT6CE API and addonSdkJar tasks."
    }
    Copy-Item -LiteralPath $source -Destination (Join-Path $dest (Split-Path $relative -Leaf)) -Force
    Write-Output "copy  $(Split-Path $relative -Leaf)"
}
foreach ($relative in $optional) {
    $source = Join-Path $gt6 $relative
    if (-not (Test-Path -LiteralPath $source)) {
        Write-Output "skip  $relative"
        continue
    }
    Copy-Item -LiteralPath $source -Destination (Join-Path $dest (Split-Path $relative -Leaf)) -Force
    Write-Output "copy  $(Split-Path $relative -Leaf)"
}
