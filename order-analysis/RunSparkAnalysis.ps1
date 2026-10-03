param(
    [switch]$NoPause
)

$ErrorActionPreference = 'Stop'
$project = $PSScriptRoot
$classpathFile = Join-Path $project 'target\spark-classpath.txt'

$maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($maven) {
    $mavenPath = $maven.Source
} else {
    $mavenPath = Get-ChildItem "$env:USERPROFILE\.m2\wrapper\dists" -Filter mvn.cmd -Recurse -ErrorAction SilentlyContinue |
        Select-Object -First 1 -ExpandProperty FullName
}

if (-not $mavenPath) {
    throw 'Maven was not found.'
}

$javaOptions = @(
    '--add-exports=java.base/sun.nio.ch=ALL-UNNAMED',
    '--add-exports=java.base/sun.util.calendar=ALL-UNNAMED',
    '--add-opens=java.base/java.nio=ALL-UNNAMED'
)
$env:HADOOP_HOME = 'C:\hadoop'

Push-Location $project
try {
    $mainClass = Join-Path $project 'target\classes\com\xiong\analysis\SparkSalesAnalysis.class'
    $pom = Join-Path $project 'pom.xml'
    $needPrepare = !(Test-Path $classpathFile) -or !(Test-Path $mainClass) -or ((Get-Item $pom).LastWriteTime -gt (Get-Item $classpathFile).LastWriteTime)

    if ($needPrepare) {
        Write-Host 'Preparing Spark runtime for the first run...' -ForegroundColor Cyan
        & $mavenPath -q -DskipTests compile dependency:build-classpath "-Dmdep.outputFile=$classpathFile"
        if ($LASTEXITCODE -ne 0) { throw "Maven preparation failed with code $LASTEXITCODE" }
    }

    $dependencies = (Get-Content $classpathFile -Raw).Trim()
    $runtimeClasspath = "$(Join-Path $project 'target\classes');$dependencies"
    Write-Host 'Running Spark SQL analysis...' -ForegroundColor Cyan
    & java @javaOptions -cp $runtimeClasspath com.xiong.analysis.SparkSalesAnalysis
    if ($LASTEXITCODE -ne 0) { throw "Spark analysis exited with code $LASTEXITCODE" }
    Write-Host 'Spark analysis completed. Refresh the merchant dashboard.' -ForegroundColor Green
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    if (-not $NoPause) { Read-Host 'Press Enter to close' }
    exit 1
} finally {
    Pop-Location
}
if (-not $NoPause) { Read-Host 'Press Enter to close' }
