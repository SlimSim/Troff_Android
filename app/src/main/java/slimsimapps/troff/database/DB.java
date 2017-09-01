package slimsimapps.troff.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import slimsimapps.troff.Models.Marker;
import slimsimapps.troff.Models.Setting;
import slimsimapps.troff.Models.Song;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016-12-16, by SlimSim.
 */
public class DB extends SQLiteOpenHelper {

@SuppressWarnings("unused")
private static final String TAG = "DB";

private SQLiteDatabase database;

public DB(Context context) {
	super(context, DBContract.DATABASE_NAME, null, DBContract.DATABASE_VERSION);
}

@Override
public void onCreate(SQLiteDatabase db) {
	db.execSQL( DBContract.Song.CREATE_TABLE );
	db.execSQL( DBContract.Marker.CREATE_TABLE );
	db.execSQL( DBContract.Setting.CREATE_TABLE );
}

@Override
public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if(oldVersion < 1 )
// tex:        db.execSQL("table Song add column Color String not null");
	if( oldVersion < 2 ) {
		execMultiSQL(db, DBContract.Song.UPDATE_TO_VERSION_2);
		execMultiSQL(db, DBContract.Setting.CREATE_TABLE);
	}
}

private void execMultiSQL(SQLiteDatabase db,
						  String fullUpdateString ) {
	String[] updates = fullUpdateString.split(
			DBContract.END
	);
	for (String update : updates) {
		if( update == null || update.equals("") ) {
			continue;
		}
		try {
			db.execSQL(update);
		} catch( SQLiteException e ) {
			Log.e(TAG, "execMultiSQL: " +
					"On upgrade database exception for\n" + update);
			Log.e(TAG, "execMultiSQL:\nmessage = " + e.getMessage()
					+ "\ncause = " + e.getCause());
			throw e;
		}
	}
}


public void setCurrentSong(long songNr ) {
	database = this.getReadableDatabase();
	Setting setting = new Setting(Setting.CURRENT_SONG, songNr);
	ContentValues cv = DBContract.Setting.getContentValues(setting);
	try {
		Cursor cursor = database.query(
				DBContract.Setting.T_NAME, null,
				DBContract.Setting.C_NAME + " = ?",
				new String[]{Setting.CURRENT_SONG},
				null, null, null);
		if( cursor.getCount() == 0 ) {
			// insert
			database.insertOrThrow(
					DBContract.Setting.T_NAME, null, cv);
		} else {
			// update
			database.update(
					DBContract.Setting.T_NAME, cv,
					DBContract.Setting.C_NAME + " = ?",
					new String[]{Setting.CURRENT_SONG});
		}
		cursor.close();
	} catch (Exception e) {
		Log.e(TAG, "setCurrentSong: Error setting song to database: " + e);
	}
	database.close();
}

public long getCurrentSong(){
	database = this.getReadableDatabase();
	Cursor cursor = database.query(
			DBContract.Setting.T_NAME, //table
			null, // columns
			DBContract.Setting.C_NAME + " = ?", //filter columns
			new String[]{Setting.CURRENT_SONG}, //filter values
			null, // group by
			null, // having
			null // sortOrder
	);
	Setting setting;
	if(cursor.getCount() == 0) {
		return -1;
	} else {
		cursor.moveToNext();
		setting = DBContract.Setting.getObject( cursor );
		if( cursor.getCount() > 1) {
			Log.w(TAG, "getCurrentSong: cursor.getCount = " +
			cursor.getCount() + ", should be 1!");
		}
	}
	cursor.close();
	database.close();
	return setting.getValue();

}

public Song insertSong( Song song ) {
	database = this.getReadableDatabase();
	ContentValues cv = DBContract.Song.getContentValues( song );
	try {
		long rowId = database.insertOrThrow(
				DBContract.Song.T_NAME, null, cv);
		Cursor cursor = database.query(
				DBContract.Song.T_NAME,
				null,
				"rowId = ?",
				new String[]{""+rowId}, null, null, null);
		if( cursor.getCount() != 1 ) {
			throw( new Exception("insertSong: cursor.getCount() is not 1!") );
		}
		cursor.moveToFirst();
		song.setId(cursor.getInt(cursor.getColumnIndex(DBContract.Song.C_ID)));
		cursor.close();
	} catch (Exception e) {
		Log.e(TAG, "insertSong: Error inserting song to database: " + e);
		song = null;
	}
	database.close();
	return song;
}


public Marker insertMarker(Marker marker) {
	database = this.getReadableDatabase();
	ContentValues cv = DBContract.Marker.getContentValues(marker);
	try {
		long rowId = database.insertOrThrow(DBContract.Marker.T_NAME, null, cv);
		Cursor cursor = database.query(DBContract.Marker.T_NAME, null, "rowId = ?", new String[]{""+rowId}, null, null, null);
		if( cursor.getCount() != 1 ) {
			throw( new Exception("insertMarker: cursor.getCount() is not 1!") );
		}
		cursor.moveToFirst();
		marker.setId(cursor.getInt(cursor.getColumnIndex(DBContract.Marker.C_ID)));
		cursor.close();
	} catch (Exception e) {
		Log.e(TAG, "insertMarker: Error inserting marker to database: " + e);
		marker = null;
	}
	database.close();

	return marker;
}

public void removeMarker(int songId, int markerId) {
	database = this.getReadableDatabase();
	database.delete(
			DBContract.Marker.T_NAME, //table
			DBContract.Marker.C_SONG_ID + "=? and " +
			DBContract.Marker.C_ID + "=?", // whereClause
			new String[]{String.valueOf(songId), String.valueOf(markerId)} // whereArgs
	);
	database.close();
}

@SuppressWarnings("unused")
public void updateMarker(Marker marker) {
	database = this.getReadableDatabase();
	ContentValues cv = DBContract.Marker.getContentValues( marker );
	database.update(DBContract.Marker.T_NAME, cv, DBContract.Marker.C_ID + " = ?", new String[]{""+marker.getId()});
	database.close();
}


public List<Marker> getAllMarkers(int songId) {
	database = this.getReadableDatabase();
	Cursor cursor = database.query(
			DBContract.Marker.T_NAME, //table
			null, // columns
			DBContract.Marker.C_SONG_ID + " = ?", // filter columns
			new String[]{""+songId}, //filter values
			null, // group by
			null, // having
			DBContract.Marker.C_TIME + DBContract.ASCENDING // sortOrder
	);
	List<Marker> markers = new ArrayList<>();
	Marker marker;
	if (cursor.getCount() > 0) {
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			marker = DBContract.Marker.getObject( cursor );
			markers.add(marker);
		}
	}
	cursor.close();
	database.close();
	return markers;
}



public Song getSong(long fileId) {
	Song song;
	database = this.getReadableDatabase();
	Cursor cursor = database.query(DBContract.Song.T_NAME, null, DBContract.Song.C_FILE_ID + " = ?", new String[]{""+fileId}, null, null, null);

	if( cursor.getCount() == 0 ) {
		Log.w(TAG, "no song found for fileId = "+fileId+", returning null.");
		song = null;
	} else {
		if( cursor.getCount() != 1 ) {
			Log.e(TAG, "Error, multiple songs found for fileId = " + fileId + "! Returning first...");
		}

		cursor.moveToNext();
		song = DBContract.Song.getObject( cursor );
	}

	cursor.close();
	database.close();
	return song;
}

public void updateSong(Song song) {
	database = this.getReadableDatabase();
	ContentValues cv = DBContract.Song.getContentValues( song );

	try {
		database.update(DBContract.Song.T_NAME, cv, DBContract.Song.C_ID + " = ?", new String[]{""+song.getId()});
	} catch (Exception e) {
		Log.e(TAG, "Error updating song to database: " + e);
	}
	database.close();
}
}//end Class

