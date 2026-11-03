package com.sosuisha.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WavConverterTest {
    @Test
    @DisplayName("m4aを、同じ周波数とチャンネル数の16bitのPCMのWAVに変換する")
    void converts_an_m4a_to_a_16_bit_pcm_wav_with_the_same_rate_and_channels()
        throws Exception {
        // tagged.m4a is 1 second of AAC-LC, 44100Hz, stereo.
        var wav = new WavConverter().toWav(Path.of("src/test/resources/id3/tagged.m4a"));

        var file = AudioSystem.getAudioFileFormat(wav.toFile());
        var format = file.getFormat();
        assertAll(
            () -> assertEquals(AudioFileFormat.Type.WAVE, file.getType()),
            () -> assertEquals(AudioFormat.Encoding.PCM_SIGNED, format.getEncoding()),
            () -> assertEquals(16, format.getSampleSizeInBits()),
            () -> assertEquals(44100f, format.getSampleRate()),
            () -> assertEquals(2, format.getChannels()),
            // The AAC encoder adds a little silence, so the length is not
            // exactly 44100 frames.
            () -> assertTrue(file.getFrameLength() >= 44100, "frames: " + file.getFrameLength())
        );
    }

    @Test
    @DisplayName("一時WAVを、指定した専用フォルダに作る")
    void creates_the_temporary_wav_in_the_given_folder() throws Exception {
        var folder = Files.createTempDirectory(
            Files.createDirectories(Path.of("target/wav-test")), "folder"
        ).resolve("sss-music-player");

        var wav = new WavConverter(folder).toWav(Path.of("src/test/resources/id3/tagged.m4a"));

        assertEquals(folder, wav.getParent());
    }

    @Test
    @DisplayName("専用フォルダに残った一時WAVをすべて消す")
    void deletes_all_temporary_wavs_left_in_the_folder() throws Exception {
        var folder = Files.createTempDirectory(
            Files.createDirectories(Path.of("target/wav-test")), "leftovers"
        );
        Files.write(folder.resolve("1.wav"), new byte[16]);
        Files.write(folder.resolve("2.wav"), new byte[16]);

        new WavConverter(folder).deleteLeftovers();

        try (var left = Files.list(folder)) {
            assertEquals(List.of(), left.toList());
        }
    }
}
