[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs
)

$ErrorActionPreference = 'Stop'
$repo = Split-Path -Parent $PSScriptRoot
if (-not $GradleArgs -or $GradleArgs.Count -eq 0) {
    $GradleArgs = @('compileBoth')
}

function Find-Jdk([int]$Major) {
    $candidates = @([Environment]::GetEnvironmentVariable("JAVA${Major}_HOME"))
    if ($Major -eq 17) { $candidates += $env:JAVA_HOME }
    foreach ($parent in @(
            "$env:ProgramFiles\Java",
            "$env:ProgramFiles\Eclipse Adoptium",
            "$env:ProgramFiles\Microsoft",
            "$env:USERPROFILE\.jdks")) {
        if (Test-Path -LiteralPath $parent) {
            $candidates += Get-ChildItem -LiteralPath $parent -Directory | ForEach-Object FullName
        }
    }
    foreach ($candidate in $candidates | Select-Object -Unique) {
        if (-not $candidate) { continue }
        $java = Join-Path $candidate 'bin\java.exe'
        $javac = Join-Path $candidate 'bin\javac.exe'
        if (-not (Test-Path -LiteralPath $java) -or -not (Test-Path -LiteralPath $javac)) { continue }
        # java -version writes to stderr. Windows PowerShell turns that into a terminating error
        # when ErrorActionPreference is Stop, so read it through cmd.
        $version = cmd /c "`"$java`" -version 2>&1"
        if ($version -match "version `"$Major\.") { return $candidate }
    }
    throw "Java $Major JDK not found. Set JAVA${Major}_HOME to its installation directory, not the bin folder."
}

$jdk17 = Find-Jdk 17
$jdk21 = Find-Jdk 21
$env:JAVA_HOME = $jdk17
$env:JAVA17_HOME = $jdk17
$env:JAVA21_HOME = $jdk21
Write-Output "Java 17: $jdk17"
Write-Output "Java 21: $jdk21"

Push-Location $repo
try {
    & (Join-Path $repo 'gradlew.bat') --console=plain @GradleArgs
    exit $LASTEXITCODE
} finally {
    Pop-Location
}
