package com.sosuisha.service;

import java.nio.file.Path;
import java.util.Objects;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;

import com.sosuisha.domain.service.TagWriter;

/**
 * Tag writer that writes to the tag of the audio file with jaudiotagger.
 */
public class JaudiotaggerTagWriter implements TagWriter {
    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if file, album, albumArtist, or year is null
     * @throws IllegalStateException if the tag cannot be written
     */
    @Override
    public void writeAlbumTag(Path file, String album, String albumArtist, String year) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(album, "album must not be null");
        Objects.requireNonNull(albumArtist, "albumArtist must not be null");
        Objects.requireNonNull(year, "year must not be null");
        try {
            var audioFile = AudioFileIO.read(file.toFile());
            var tag = audioFile.getTagOrCreateAndSetDefault();
            tag.setField(FieldKey.ALBUM, album);
            tag.setField(FieldKey.ALBUM_ARTIST, albumArtist);
            tag.setField(FieldKey.YEAR, year);
            audioFile.commit();
        } catch (Exception e) {
            throw new IllegalStateException("cannot write the tag of " + file, e);
        }
    }
}
