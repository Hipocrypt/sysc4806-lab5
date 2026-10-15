# Windows convenience helper. The standard Maven wrappers work on other machines.
$ErrorActionPreference = 'Stop'
$javaCandidates = @($env:JAVA_HOME, 'C:\Program Files\Microsoft\jdk-21.0.10.7-hotspot')
foreach ($jdkDirectory in @((Join-Path $env:USERPROFILE '.jdks'), 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Microsoft')) {
    if (Test-Path -LiteralPath $jdkDirectory) {
        $javaCandidates += Get-ChildItem -LiteralPath $jdkDirectory -Directory | Select-Object -ExpandProperty FullName
    }
}
$java21 = $javaCandidates | Where-Object {
    $_ -and (Test-Path -LiteralPath (Join-Path $_ 'release')) -and
    ((Get-Content -LiteralPath (Join-Path $_ 'release') -Raw) -match 'JAVA_VERSION="21[.\"]')
} | Select-Object -First 1
if (-not $java21) { throw 'Install JDK 21 and set JAVA_HOME to its installation directory. See README.md.' }
$env:JAVA_HOME = $java21
$toolsPath = Join-Path $PSScriptRoot '.tools'
New-Item -ItemType Directory -Force -Path (Join-Path $toolsPath 'tmp') | Out-Null
$env:TEMP = Join-Path $toolsPath 'tmp'
$env:TMP = $env:TEMP
$env:MAVEN_USER_HOME = $toolsPath
$mavenExecutable = Join-Path $toolsPath 'apache-maven-3.9.11\bin\mvn.cmd'
if (-not (Test-Path -LiteralPath $mavenExecutable)) { $mavenExecutable = Join-Path $PSScriptRoot 'mvnw.cmd' }
& $mavenExecutable "-Dmaven.repo.local=$(Join-Path $toolsPath 'repository')" -f (Join-Path $PSScriptRoot 'pom.xml') @args
exit $LASTEXITCODE
