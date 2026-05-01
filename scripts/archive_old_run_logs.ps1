[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$LogsDir = "",
    [string]$ArchiveDirName = "back"
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrWhiteSpace($LogsDir)) {
    $LogsDir = Join-Path $scriptDir "..\\run\\logs"
}

$resolvedLogsDir = (Resolve-Path -LiteralPath $LogsDir).Path
$archiveDir = Join-Path $resolvedLogsDir $ArchiveDirName

if (-not (Test-Path -LiteralPath $archiveDir)) {
    New-Item -ItemType Directory -Path $archiveDir -Force | Out-Null
}

$today = (Get-Date).Date

function Get-DateFromLogFileName {
    param([string]$FileName)

    # 例如: 2026-05-01-12.log.gz / 2026-05-01-1.log
    if ($FileName -match '^(?<d>\d{4}-\d{2}-\d{2})-\d+\.log(?:\.gz)?$') {
        return [datetime]::ParseExact($Matches['d'], 'yyyy-MM-dd', $null).Date
    }
    return $null
}

$files = Get-ChildItem -LiteralPath $resolvedLogsDir -File -Force | Where-Object {
    if ($_.DirectoryName -eq $archiveDir) {
        return $false
    }

    $dateInName = Get-DateFromLogFileName -FileName $_.Name
    if ($null -ne $dateInName) {
        return $dateInName -lt $today
    }

    # 文件名不带日期时，退回到最后修改时间判断（如 latest.log / debug.log）
    return $_.LastWriteTime.Date -lt $today
}

foreach ($file in $files) {
    $targetPath = Join-Path $archiveDir $file.Name
    if ($PSCmdlet.ShouldProcess($file.FullName, "Move to $targetPath")) {
        Move-Item -LiteralPath $file.FullName -Destination $targetPath -Force
    }
}

Write-Host ("Moved {0} file(s) to {1}" -f $files.Count, $archiveDir)
