package com.sosuisha.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Converts an audio file that Java Sound can read into a temporary WAV file
 * in a folder only for those files.
 * JAAD adds m4a (AAC) to the formats that Java Sound reads. See ADR 005.
 */
public class WavConverter {
    private final Path folder;

    /**
     * Creates a converter that writes into the folder {@code sss-music-player}
     * in the temporary folder of the OS.
     */
    public WavConverter() {
        this(Path.of(System.getProperty("java.io.tmpdir"), "sss-music-player"));
    }

    /**
     * Creates a converter that writes into the given folder. The folder is
     * created when it does not exist.
     *
     * @param folder folder for the temporary WAV files
     * @throws NullPointerException if folder is null
     */
    public WavConverter(Path folder) {
        this.folder = Objects.requireNonNull(folder, "folder must not be null");
    }

    /**
     * Converts the audio file into 16-bit signed PCM and writes it to a new
     * temporary WAV file in the folder. The sample rate and the channels stay the same.
     *
     * @param source path of the audio file to convert
     * @return path of the new temporary WAV file
     * @throws NullPointerException if source is null
     * @throws IOException if the file cannot be read or converted
     */
    public Path toWav(Path source) throws IOException {
        Objects.requireNonNull(source, "source must not be null");
        try (var encoded = AudioSystem.getAudioInputStream(source.toFile())) {
            var from = encoded.getFormat();
            var pcm = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED, from.getSampleRate(), 16,
                from.getChannels(), from.getChannels() * 2, from.getSampleRate(), false
            );
            try (var decoded = AudioSystem.getAudioInputStream(pcm, encoded)) {
                var wav = Files.createTempFile(Files.createDirectories(folder), "", ".wav");
                AudioSystem.write(decoded, AudioFileFormat.Type.WAVE, wav.toFile());
                return wav;
            }
        } catch (UnsupportedAudioFileException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
