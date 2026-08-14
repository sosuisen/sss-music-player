-- Schema of the library database (~/.sss-music-player/library.db).
-- Used both by the jOOQ code generator (parsed with H2) and at runtime
-- to create the tables in SQLite, so keep it portable between the two.
CREATE TABLE IF NOT EXISTS track (
  path VARCHAR NOT NULL PRIMARY KEY,
  size BIGINT NOT NULL,
  last_modified BIGINT NOT NULL,
  title VARCHAR NOT NULL,
  artist VARCHAR NOT NULL,
  album VARCHAR NOT NULL,
  album_artist VARCHAR NOT NULL,
  track_number VARCHAR NOT NULL,
  release_year VARCHAR NOT NULL
);
