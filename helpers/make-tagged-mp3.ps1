# Generates minimal mp3 files for test resources:
# - tagged.mp3 / tagged2.mp3: with an ID3v2.3 tag
# - untagged.mp3: valid MPEG frames only, without any tag
$enc = [System.Text.Encoding]::GetEncoding('ISO-8859-1')

# Minimal MPEG-1 Layer III frames: 128kbps, 44.1kHz, no padding -> 417 bytes each.
# The leading comma keeps PowerShell from unrolling the byte array into Object[].
function Get-MpegFrames {
    $frame = New-Object byte[] 417
    $frame[0] = 0xFF; $frame[1] = 0xFB; $frame[2] = 0x90; $frame[3] = 0x00
    $bytes = New-Object byte[] (417 * 4)
    for ($i = 0; $i -lt 4; $i++) {
        [Array]::Copy($frame, 0, $bytes, $i * 417, 417)
    }
    return , $bytes
}

function New-TaggedMp3 {
    param(
        [string]$Path,
        [System.Collections.Specialized.OrderedDictionary]$Tags
    )

    $frames = [System.Collections.Generic.List[byte]]::new()
    foreach ($key in $Tags.Keys) {
        $text = $enc.GetBytes($Tags[$key])
        $size = $text.Length + 1  # +1 for the encoding byte
        $frames.AddRange($enc.GetBytes($key))
        $frames.AddRange([byte[]]@(
            (($size -shr 24) -band 0xFF),
            (($size -shr 16) -band 0xFF),
            (($size -shr 8) -band 0xFF),
            ($size -band 0xFF)))
        $frames.AddRange([byte[]]@(0, 0))  # frame flags
        $frames.Add([byte]0)               # encoding: ISO-8859-1
        $frames.AddRange($text)
    }

    $body = $frames.ToArray()
    $len = $body.Length
    # ID3v2 header: "ID3", version 2.3, flags 0, syncsafe size
    $header = [byte[]]@(
        0x49, 0x44, 0x33, 3, 0, 0,
        (($len -shr 21) -band 0x7F),
        (($len -shr 14) -band 0x7F),
        (($len -shr 7) -band 0x7F),
        ($len -band 0x7F))

    $out = [System.Collections.Generic.List[byte]]::new()
    $out.AddRange($header)
    $out.AddRange($body)
    $out.AddRange((Get-MpegFrames))

    [System.IO.File]::WriteAllBytes($Path, $out.ToArray())
    Write-Host "Wrote $Path ($($out.Count) bytes)"
}

function New-UntaggedMp3 {
    param([string]$Path)

    $bytes = Get-MpegFrames
    [System.IO.File]::WriteAllBytes($Path, $bytes)
    Write-Host "Wrote $Path ($($bytes.Length) bytes)"
}

$dir = Join-Path $PSScriptRoot '..\src\test\resources\id3'
New-Item -ItemType Directory -Force $dir | Out-Null

New-TaggedMp3 -Path (Join-Path $dir 'tagged.mp3') -Tags ([ordered]@{
    TIT2 = 'Test Song'
    TPE1 = 'Test Artist'
    TALB = 'Test Album'
    TPE2 = 'Test Album Artist'
    TRCK = '3'
    TYER = '2020'
})

New-TaggedMp3 -Path (Join-Path $dir 'tagged2.mp3') -Tags ([ordered]@{
    TIT2 = 'Another Song'
    TPE1 = 'Another Artist'
    TALB = 'Another Album'
    TPE2 = 'Another Album Artist'
    TRCK = '7'
    TYER = '1999'
})

New-UntaggedMp3 -Path (Join-Path $dir 'untagged.mp3')
