$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# Load only the real safety functions. Never run the signing script or read its keys.
$tokens = $null
$parseErrors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile(
    (Join-Path $PSScriptRoot 'create-signed-installer.ps1'), [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count -ne 0) { throw 'The signing helper has PowerShell syntax errors.' }
foreach ($name in @('Assert-InstallerDestinationAvailable', 'Move-InstallerWithoutOverwrite', 'Assert-ReleaseApkMetadata')) {
    $definition = $ast.Find({ param($node)
        $node -is [System.Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq $name
    }, $true)
    if ($null -eq $definition) { throw "Missing safety function: $name" }
    . ([scriptblock]::Create($definition.Extent.Text))
}

function Assert-Rejected([scriptblock]$Action, [string]$Label) {
    $rejected = $false
    try { & $Action } catch { $rejected = $true }
    if (-not $rejected) { throw "Expected rejection: $Label" }
}

$testDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ('rener-installer-safety-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $testDirectory | Out-Null
try {
    $existing = Join-Path $testDirectory 'existing.apk'
    $candidate = Join-Path $testDirectory 'candidate.apk'
    $fresh = Join-Path $testDirectory 'fresh.apk'
    [System.IO.File]::WriteAllText($existing, 'original verified installer')
    [System.IO.File]::WriteAllText($candidate, 'new candidate')
    $originalHash = (Get-FileHash -LiteralPath $existing -Algorithm SHA256).Hash
    Assert-Rejected { Assert-InstallerDestinationAvailable $existing } 'existing destination'
    Assert-InstallerDestinationAvailable $fresh
    Assert-Rejected { Move-InstallerWithoutOverwrite $candidate $existing } 'destination created before final move'
    if ((Get-FileHash -LiteralPath $existing -Algorithm SHA256).Hash -cne $originalHash -or
        -not (Test-Path -LiteralPath $candidate)) { throw 'An existing installer or rejected candidate changed.' }
    Move-InstallerWithoutOverwrite $candidate $fresh
    if ([System.IO.File]::ReadAllText($fresh) -cne 'new candidate' -or (Test-Path -LiteralPath $candidate)) {
        throw 'The verified candidate was not moved intact.'
    }

    $package = "package: name='com.idphoto.printing' versionCode='11' versionName='1.0.7' platformBuildVersionName='16'"
    Assert-ReleaseApkMetadata @($package) '1.0.7' 11
    Assert-Rejected { Assert-ReleaseApkMetadata @($package) '1.0.7' 12 } 'stale build code'
    Assert-Rejected { Assert-ReleaseApkMetadata @($package) '1.0.8' 11 } 'wrong version name'
    Assert-Rejected { Assert-ReleaseApkMetadata @($package, 'application-debuggable') '1.0.7' 11 } 'debuggable APK'
    Assert-Rejected { Assert-ReleaseApkMetadata @($package.Replace('com.idphoto.printing', 'other.app'), "application-label:'com.idphoto.printing'") '1.0.7' 11 } 'wrong package despite matching label'
    Assert-Rejected { Assert-ReleaseApkMetadata @($package.Replace(" versionCode='11'", '')) '1.0.7' 11 } 'missing build code'
    Write-Output 'PASS: destination preservation, final-move collision, intact publication, and exact APK metadata checks (10 scenarios). No signing keys or release APKs accessed.'
} finally {
    Get-ChildItem -LiteralPath $testDirectory -File | ForEach-Object { Remove-Item -LiteralPath $_.FullName -Force }
    [System.IO.Directory]::Delete($testDirectory, $false)
}
