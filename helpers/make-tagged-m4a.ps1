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

# One second of silent AAC audio with iTunes-style metadata atoms.
function New-TaggedM4a {
    param([string]$Path, [hashtable]$Meta)

    ffmpeg -y -loglevel error `
        -f lavfi -i anullsrc=r=44100:cl=stereo -t 1 `
        -c:a aac -b:a 128k `
        -metadata title=$($Meta.title) `
        -metadata artist=$($Meta.artist) `
        -metadata album=$($Meta.album) `
        -metadata album_artist=$($Meta.album_artist) `
        -metadata track=$($Meta.track) `
        -metadata date=$($Meta.date) `
        $Path

    if ($LASTEXITCODE -ne 0) {
        Write-Error 'ffmpeg failed to generate the m4a file.'
        exit 1
    }
    Write-Host "Wrote $Path"
}

New-TaggedM4a -Path (Join-Path $dir 'tagged.m4a') -Meta @{
    title = 'M4A Song'
    artist = 'M4A Artist'
    album = 'M4A Album'
    album_artist = 'M4A Album Artist'
    track = '5'
    date = '2015'
}

New-TaggedM4a -Path (Join-Path $dir 'tagged-japanese.m4a') -Meta @{
    title = '春の歌'
    artist = '春のアーティスト'
    album = '春のアルバム'
    album_artist = '春のアルバムアーティスト'
    track = '6'
    date = '2018'
}
