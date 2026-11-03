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

-- Favorites in the order they were added. The id is an alias of the SQLite
-- rowid, so it is assigned in ascending order on insert. Columns that do not
-- apply to the kind hold an empty string.
CREATE TABLE IF NOT EXISTS favorite (
  id INTEGER PRIMARY KEY,
  kind VARCHAR NOT NULL,
  album_name VARCHAR NOT NULL,
  album_artist VARCHAR NOT NULL,
  track_path VARCHAR NOT NULL
);
