# Generates minimal mp3 files with ID3v2.3 tags for test resources.
$enc = [System.Text.Encoding]::GetEncoding('ISO-8859-1')

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

    # Minimal MPEG-1 Layer III frame: 128kbps, 44.1kHz, no padding -> 417 bytes
    $mpegFrame = New-Object byte[] 417
    $mpegFrame[0] = 0xFF; $mpegFrame[1] = 0xFB; $mpegFrame[2] = 0x90; $mpegFrame[3] = 0x00

    $out = [System.Collections.Generic.List[byte]]::new()
    $out.AddRange($header)
    $out.AddRange($body)
    1..4 | ForEach-Object { $out.AddRange($mpegFrame) }

    [System.IO.File]::WriteAllBytes($Path, $out.ToArray())
    Write-Host "Wrote $Path ($($out.Count) bytes)"
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
