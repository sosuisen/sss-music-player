# Generates a small m4a file with tag metadata for test resources.
#
# REQUIRED: ffmpeg must be installed and on the PATH (https://ffmpeg.org/).
# The generated file is committed to the repository, so ffmpeg is only
# needed when regenerating it.

if (-not (Get-Command ffmpeg -ErrorAction SilentlyContinue)) {
    Write-Error 'ffmpeg is required but was not found on the PATH.'
    exit 1
}

$dir = Join-Path $PSScriptRoot '..\src\test\resources\id3'
New-Item -ItemType Directory -Force $dir | Out-Null
$path = Join-Path $dir 'tagged.m4a'

# One second of silent AAC audio with iTunes-style metadata atoms.
ffmpeg -y -loglevel error `
    -f lavfi -i anullsrc=r=44100:cl=stereo -t 1 `
    -c:a aac -b:a 128k `
    -metadata title='M4A Song' `
    -metadata artist='M4A Artist' `
    -metadata album='M4A Album' `
    -metadata album_artist='M4A Album Artist' `
    -metadata track='5' `
    -metadata date='2015' `
    $path

if ($LASTEXITCODE -ne 0) {
    Write-Error 'ffmpeg failed to generate the m4a file.'
    exit 1
}
Write-Host "Wrote $path"
