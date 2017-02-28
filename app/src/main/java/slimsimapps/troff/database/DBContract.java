package slimsimapps.troff.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created on 2016-12-18, by Slim Sim.
 */

final class DBContract {

    @SuppressWarnings("unused")
    private static final String TAG = "DBContract";

    static final  int    DATABASE_VERSION    = 1;
    static final  String DATABASE_NAME       = "database.db";

    private static final String PRIMARY_KEY         = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String TEXT_TYPE           = " TEXT";
    private static final String INT_TYPE            = " INT";
    private static final String LONG_TYPE           = " LONG";
    private static final String COMMA_SEP           = ",";
    static final String ASCENDING                   = " ASC";
    @SuppressWarnings("unused") static final String DESCENDING = " DESC";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DBContract() {}

    static abstract class Song {

        static final String T_NAME = "Song";
        static final String C_ID = "_ID";
        static final String C_FILE_ID = "file_id";
        static final String C_TITLE = "title";
        static final String C_ARTIST = "artist";
        static final String C_TEMPO = "tempo";
        static final String C_INFO = "info";
        static final String C_NR_PLAYED = "nr_played";

        static final String CREATE_TABLE = "CREATE TABLE " +
                T_NAME + " (" +
                C_ID + PRIMARY_KEY +
                C_FILE_ID + LONG_TYPE + COMMA_SEP +
                C_TITLE + TEXT_TYPE + COMMA_SEP +
                C_ARTIST + TEXT_TYPE + COMMA_SEP +
                C_INFO + TEXT_TYPE  + COMMA_SEP +
                C_TEMPO + INT_TYPE + COMMA_SEP +
                C_NR_PLAYED + INT_TYPE +
                " )";

        //static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + T_NAME;


        static ContentValues getContentValues(slimsimapps.troff.Models.Song song) {
            ContentValues cv = new ContentValues();
            cv.put(DBContract.Song.C_FILE_ID, song.getFileId());
            cv.put(DBContract.Song.C_TITLE, song.getTitle());
            cv.put(DBContract.Song.C_ARTIST, song.getArtist());
            cv.put(DBContract.Song.C_INFO, song.getInfo());
            cv.put(DBContract.Song.C_TEMPO, song.getTempo());
            cv.put(DBContract.Song.C_NR_PLAYED, song.getNrPlayed());
            return cv;
        }

        static slimsimapps.troff.Models.Song getObject(Cursor cursor) {
            slimsimapps.troff.Models.Song song = new slimsimapps.troff.Models.Song();
            song.setId(cursor.getInt(cursor.getColumnIndex( DBContract.Song.C_ID )));
            song.setFileId(cursor.getLong(cursor.getColumnIndex( DBContract.Song.C_FILE_ID )));
            song.setTitle(cursor.getString(cursor.getColumnIndex( DBContract.Song.C_TITLE )));
            song.setArtist(cursor.getString(cursor.getColumnIndex( DBContract.Song.C_ARTIST )));
            song.setInfo(cursor.getString(cursor.getColumnIndex( DBContract.Song.C_INFO )));
            song.setTempo(cursor.getInt(cursor.getColumnIndex( DBContract.Song.C_TEMPO )));
            song.setNrPlayed(cursor.getInt(cursor.getColumnIndex( DBContract.Song.C_NR_PLAYED )));
            return song;
        }
    }

    static abstract class Marker {

        static final String T_NAME = "Marker";
        static final String C_ID = "_ID";
        static final String C_SONG_ID = "song_id";
        static final String C_TIME = "time";
        static final String C_NAME = "name";
        static final String C_INFO = "info";
        static final String C_COLOR = "color";

        static final String CREATE_TABLE = "CREATE TABLE " +
                T_NAME + " (" +
                C_ID + PRIMARY_KEY +
                C_SONG_ID + INT_TYPE + COMMA_SEP +
                C_TIME + LONG_TYPE + COMMA_SEP +
                C_NAME + TEXT_TYPE + COMMA_SEP +
                C_INFO + TEXT_TYPE + COMMA_SEP +
                C_COLOR + TEXT_TYPE + COMMA_SEP +
                "FOREIGN KEY(" + C_SONG_ID + ") REFERENCES " + Song.T_NAME + " ("+Song.C_ID+")" +
                " )";

        //static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + T_NAME;

        static ContentValues getContentValues(slimsimapps.troff.Models.Marker marker) {
            ContentValues cv = new ContentValues();
            cv.put(DBContract.Marker.C_SONG_ID, marker.getSongId());
            cv.put(DBContract.Marker.C_NAME, marker.getName());
            cv.put(DBContract.Marker.C_TIME, marker.getTime());
            cv.put(DBContract.Marker.C_INFO, marker.getInfo());
            cv.put(DBContract.Marker.C_COLOR, marker.getColor());
            return cv;
        }

        static slimsimapps.troff.Models.Marker getObject(Cursor cursor) {
            slimsimapps.troff.Models.Marker marker = new slimsimapps.troff.Models.Marker();
            marker.setId(cursor.getInt(cursor.getColumnIndex(DBContract.Marker.C_ID)));
            marker.setSongId(cursor.getInt(cursor.getColumnIndex(DBContract.Marker.C_SONG_ID)));
            marker.setName(cursor.getString(cursor.getColumnIndex( DBContract.Marker.C_NAME )));
            marker.setTime(cursor.getLong(cursor.getColumnIndex( DBContract.Marker.C_TIME )));
            marker.setInfo(cursor.getString(cursor.getColumnIndex( DBContract.Marker.C_INFO )));
            marker.setColor(cursor.getString(cursor.getColumnIndex( DBContract.Marker.C_COLOR )));
            return marker;
        }
    }

}
