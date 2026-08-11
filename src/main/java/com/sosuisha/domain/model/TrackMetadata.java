package com.sosuisha.domain.model;

import java.util.Objects;

/**
 * Metadata of an audio track, read from the file's tag (the ID3 tag of an mp3
 * file, or the MP4 metadata of an m4a file). A missing field is an empty
 * string.
 *
 * @param title title of the song
 * @param artist artist of the song
 * @param album album name
 * @param albumArtist album artist
 * @param trackNumber track number only, without the total track count (for example "3", not "3/12")
 * @param year release year
 */
public record TrackMetadata(String title, String artist, String album, String albumArtist,
    String trackNumber, String year) {

    /** Track metadata whose fields are all empty strings. */
    public static final TrackMetadata EMPTY = new TrackMetadata("", "", "", "", "", "");

    /**
     * Creates the track metadata.
     *
     * @throws NullPointerException if any field is null
     */
    public TrackMetadata {
        Objects.requireNonNull(title, "title must not be null");
        Objects.requireNonNull(artist, "artist must not be null");
        Objects.requireNonNull(album, "album must not be null");
        Objects.requireNonNull(albumArtist, "albumArtist must not be null");
        Objects.requireNonNull(trackNumber, "trackNumber must not be null");
        Objects.requireNonNull(year, "year must not be null");
    }
}
