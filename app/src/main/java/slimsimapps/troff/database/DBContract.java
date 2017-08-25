package slimsimapps.troff.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created on 2016-12-18, by Slim Sim.
 */

final class DBContract {

    @SuppressWarnings("unused")
    private static final String TAG = "DBContract";

    static final  int    DATABASE_VERSION    = 2;
    static final  String DATABASE_NAME       = "database.db";

    private static final String PRIMARY_KEY         = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String TEXT_TYPE           = " TEXT";
    private static final String INT_TYPE            = " INT";
    private static final String LONG_TYPE           = " LONG";
    private static final String COMMA_SEP           = ",";
    static final String END = ";";
    static final String ASCENDING                   = " ASC";
    @SuppressWarnings("unused") static final String DESCENDING = " DESC";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private DBContract() {}

	/**
	 * This is the global settings class
	 * It operates as a name-value par;
	 * every saved setting will have one row in the DB
	 */
	static abstract class Setting {

		static final String T_NAME = "Setting";
		static final String C_ID = "_ID";
		static final String C_NAME = "setting_name";
		static final String C_VALUE = "setting_value";

		static final String CREATE_TABLE = "CREATE TABLE " +
				T_NAME + " (" +
				C_ID + PRIMARY_KEY +
				C_NAME + TEXT_TYPE + COMMA_SEP +
				C_VALUE + INT_TYPE +
				" )";

		//static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + T_NAME;

		static ContentValues getContentValues(
				slimsimapps.troff.Models.Setting setting ) {
			ContentValues cv = new ContentValues();
			cv.put(C_NAME, setting.getName());
			cv.put(C_VALUE, setting.getValue());
			return cv;
		}

		static slimsimapps.troff.Models.Setting getObject(
				Cursor cursor) {
			slimsimapps.troff.Models.Setting setting =
					new slimsimapps.troff.Models.Setting();
			setting.setId(cursor.getInt(
					cursor.getColumnIndex(C_ID)));
			setting.setName(cursor.getString(
					cursor.getColumnIndex(C_NAME)));
			setting.setValue(cursor.getLong(
					cursor.getColumnIndex(C_VALUE)));
			return setting;
		}
	}


	static abstract class Song {

        static final String T_NAME = "Song";
        static final String C_ID = "_ID";
        static final String C_FILE_ID = "file_id";
        static final String C_TITLE = "title";
        static final String C_ARTIST = "artist";
		static final String C_INFO = "info";
		static final String C_TEMPO = "tempo";
	    static final String C_WAIT_BETWEEN = "wait_between";
	    static final String C_PAUSE_BEFORE = "pause_before";
		static final String C_SELECTED_START_MARKER =
				"selected_start_marker";
		static final String C_SELECTED_END_MARKER =
				"selected_end_marker";
		static final String C_LOOP = "loop";
		static final String C_START_BEFORE = "start_before";
		static final String C_STOP_AFTER = "stop_after";
		static final String C_NR_PLAYED = "nr_played";

        static final String CREATE_TABLE = "CREATE TABLE " +
                T_NAME + " (" +
                C_ID + PRIMARY_KEY +
                C_FILE_ID + LONG_TYPE + COMMA_SEP +
                C_TITLE + TEXT_TYPE + COMMA_SEP +
                C_ARTIST + TEXT_TYPE + COMMA_SEP +
                C_INFO + TEXT_TYPE  + COMMA_SEP +
                C_TEMPO + INT_TYPE + COMMA_SEP +
                C_WAIT_BETWEEN + INT_TYPE + COMMA_SEP +
                C_PAUSE_BEFORE + INT_TYPE + COMMA_SEP +
                C_SELECTED_START_MARKER + INT_TYPE + COMMA_SEP +
                C_SELECTED_END_MARKER + INT_TYPE + COMMA_SEP +
                C_LOOP + INT_TYPE + COMMA_SEP +
		        C_START_BEFORE + INT_TYPE + COMMA_SEP +
		        C_STOP_AFTER + INT_TYPE + COMMA_SEP +
                C_NR_PLAYED + INT_TYPE +
                " )";

	    static final String UPDATE_TO_VERSION_2 = "" +
			    addColumn(C_SELECTED_START_MARKER, INT_TYPE) + END +
			    addColumn(C_SELECTED_END_MARKER, INT_TYPE) + END +
			    addColumn(C_START_BEFORE, INT_TYPE) + END +
			    addColumn(C_STOP_AFTER, INT_TYPE) + END +
			    addColumn(C_PAUSE_BEFORE, INT_TYPE) + END +
			    addColumn(C_WAIT_BETWEEN, INT_TYPE) + END +
			    addColumn(C_LOOP, INT_TYPE ) + END +
			    "UPDATE " + T_NAME +
			    " SET " + C_SELECTED_START_MARKER + " = 0" +
			    COMMA_SEP + C_SELECTED_END_MARKER + " = -1" +
			    COMMA_SEP + C_START_BEFORE + " = 3" +
			    COMMA_SEP + C_STOP_AFTER + " = 1" +
			    COMMA_SEP + C_PAUSE_BEFORE + " = 3" +
			    COMMA_SEP + C_WAIT_BETWEEN + " = 1" +
			    COMMA_SEP + C_LOOP + " = 1" +
			    END;

        //static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + T_NAME;


        static ContentValues getContentValues(slimsimapps.troff.Models.Song s) {
            ContentValues cv = new ContentValues();
            cv.put(C_FILE_ID, s.getFileId());
            cv.put(C_TITLE, s.getTitle());
            cv.put(C_ARTIST, s.getArtist());
            cv.put(C_INFO, s.getInfo());
            cv.put(C_TEMPO, s.getTempo());
            cv.put(C_WAIT_BETWEEN, s.getWaitBetween());
            cv.put(C_PAUSE_BEFORE, s.getPauseBefore());
            cv.put(C_SELECTED_START_MARKER, s.getSelectedStartMarker());
            cv.put(C_SELECTED_END_MARKER, s.getSelectedEndMarker());
            cv.put(C_LOOP, s.getLoop() );
            cv.put(C_START_BEFORE, s.getStartBefore() );
            cv.put(C_STOP_AFTER, s.getStopAfter() );
            cv.put(C_NR_PLAYED, s.getNrPlayed());
            return cv;
        }

        static slimsimapps.troff.Models.Song getObject(Cursor c) {
            slimsimapps.troff.Models.Song s =
		            new slimsimapps.troff.Models.Song();
            s.setId(getI(c, C_ID ));
            s.setFileId(getL(c, C_FILE_ID ));
            s.setTitle(getS(c, C_TITLE));
            s.setArtist(getS(c, C_ARTIST ));
            s.setInfo(getS(c, C_INFO ));
            s.setTempo(getI(c, C_TEMPO ));
            s.setWaitBetween(getI(c, C_WAIT_BETWEEN ));
            s.setPauseBefore(getI(c, C_PAUSE_BEFORE ));
            s.setSelectedStartMarker(getI(c, C_SELECTED_START_MARKER ));
            s.setSelectedEndMarker(getI(c, C_SELECTED_END_MARKER ));
            s.setLoop(getI(c, C_LOOP ));
            s.setStartBefore(getI(c, C_START_BEFORE ));
            s.setStopAfter(getI(c, C_STOP_AFTER ));
            s.setNrPlayed(getI(c, C_NR_PLAYED ));
            return s;
        }

        static private String addColumn( String name, String type ) {
	        return "ALTER TABLE " + T_NAME +
			        " ADD " + name + " " + type + " ";
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

    static private String getS(Cursor cursor, String columnName ) {
	    return cursor.getString( cursor.getColumnIndex( columnName ) );
    }
    static private long getL(Cursor cursor, String columnName ) {
	    return cursor.getLong( cursor.getColumnIndex( columnName ) );
    }
    static private int getI(Cursor cursor, String columnName ) {
	    return cursor.getInt( cursor.getColumnIndex( columnName ) );
    }

}
