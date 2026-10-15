$ErrorActionPreference = 'Stop'
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$zipPath = Join-Path $projectRoot 'lab5-101199099-submission.zip'
$include = @('.github', '.mvn', 'src', 'scripts', '.gitattributes', '.gitignore',
    'pom.xml', 'mvnw', 'mvnw.cmd', 'mvn.ps1', 'README.md', 'DEPLOYMENT.md',
    'TEST-RESULTS.md', 'HTTP-requests.txt')
Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stream = [System.IO.File]::Open($zipPath, [System.IO.FileMode]::Create)
$archive = [System.IO.Compression.ZipArchive]::new($stream, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($item in $include) {
        $path = Join-Path $projectRoot $item
        if (-not (Test-Path -LiteralPath $path)) { throw "Missing submission file: $item" }
        $files = if (Test-Path -LiteralPath $path -PathType Container) {
            Get-ChildItem -LiteralPath $path -File -Recurse -Force | Where-Object { $_.FullName -notmatch '[\\/]__pycache__[\\/]' }
        } else { Get-Item -LiteralPath $path -Force }
        foreach ($file in $files) {
            $entry = $file.FullName.Substring($projectRoot.Length + 1).Replace('\', '/')
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive, $file.FullName, $entry) | Out-Null
        }
    }
} finally {
    $archive.Dispose()
    $stream.Dispose()
}
Write-Output "Created $zipPath"
