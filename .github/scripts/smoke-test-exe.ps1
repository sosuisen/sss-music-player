# Starts the native exe, waits until its first window is open, and closes it.
# The app has no route without a window, so this is the one check that a
# missing registration in the native-image metadata does not stop the start.
# The settings file and the database are put under a temporary folder, so the
# home folder of the runner is not touched. Without a settings file the app
# opens the settings window first; closing it leaves the main window.
param(
  [Parameter(Mandatory = $true)][string]$Exe,
  [int]$StartTimeoutSeconds = 60
)

$ErrorActionPreference = 'Stop'
$work = Join-Path ([System.IO.Path]::GetTempPath()) "sss-music-player-smoke-$PID"
New-Item -ItemType Directory -Force $work | Out-Null

$p = Start-Process -FilePath $Exe -PassThru -ArgumentList @(
  "-Dsss.settings.file=$work/settings.properties",
  "-Dsss.library.db=$work/library.db"
)

$deadline = (Get-Date).AddSeconds($StartTimeoutSeconds)
while ((Get-Date) -lt $deadline) {
  if ($p.HasExited) {
    Write-Output "::error::The exe exited with code $($p.ExitCode) before it opened a window."
    exit 1
  }
  $p.Refresh()
  if ($p.MainWindowHandle -ne 0) { break }
  Start-Sleep -Seconds 1
}
if ($p.MainWindowHandle -eq 0) {
  $p.Kill()
  Write-Output "::error::The exe opened no window within $StartTimeoutSeconds seconds."
  exit 1
}
Write-Output 'The exe opened a window.'

# The settings window is modal and comes first, then the main window.
for ($i = 0; $i -lt 5 -and -not $p.HasExited; $i++) {
  $p.Refresh()
  if ($p.MainWindowHandle -ne 0) { $null = $p.CloseMainWindow() }
  Start-Sleep -Seconds 3
}
if (-not $p.HasExited) {
  $p.Kill()
  Write-Output '::error::The exe did not exit after its windows were closed.'
  exit 1
}
if ($p.ExitCode -ne 0) {
  Write-Output "::error::The exe exited with code $($p.ExitCode)."
  exit 1
}
Write-Output 'The exe exited with code 0.'
