package slimsimapps.troff.Models;

import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * Created on 2016-10-17, by Silm Sim
 */

public class Song {

@SuppressWarnings("unused")
private static final String TAG = "Song";

//private int id = -1;
private long fileId;
private String title;
private String artist;
private String path;
private SongUserInfo songUserInfo;

/*
private int nrPlayed;
private String info;
private int tempo;
private int pauseBefore;
private int waitBetween;
private int selectedStartMarker;
private int selectedEndMarker;
private int startBefore;
private int stopAfter;
private int loop;
*/
public Song() {} // empty constructor
/*
public void setId(int id) {
	this.id = id	if(songs.size() == 0 ) {
		throw new NoSongsException("MusicService / getSong: no songs!");
	}

;
}
public int getId(){return id;}
*/
public long getFileId() {
	return fileId;
}
public void setFileId(long fileId) {
	this.fileId = fileId;
}
public String getTitle(){return title;}
public String getArtist(){return artist;}
/*
public int getNrPlayed() {
	return songUserInfo.getNrPlayed();
}
public void setNrPlayed(int nrPlayed) {
	this.nrPlayed = nrPlayed;
}

public String getInfo() {
	return info;
}

public void setInfo(String info) {
	this.info = info;
}

public int getTempo() {
	return tempo;
}

public void setTempo(int tempo) {
	this.tempo = tempo;
}

public void incrementNrPlayed() {
	nrPlayed++;
}

*/
public void setTitle(String title) {
	this.title = title;
}

public void setArtist(String artist) {
	this.artist = artist;
}

public String toString(){
	return "{" +
			title + " by " + artist +
			"\nsongUserInfo = " + songUserInfo +
			"}";
}

public Marker getSelectedEndMarker() {
	return songUserInfo.getCurrentStopMarker();
}

public void setSelectedEndMarker( Marker selectedEndMarker ) {
	songUserInfo.setCurrentStopMarker( selectedEndMarker );
}

public Marker getSelectedStartMarker() {
	return songUserInfo.getCurrentStartMarker();

}

public void setSelectedStartMarker( Marker selectedStartMarker) {
	songUserInfo.setCurrentStartMarker( selectedStartMarker );
	saveData();
}

public int getStartTime() {
	long markerTime = songUserInfo.getCurrentStartMarker().getTime();
	long startBefore = songUserInfo.getStartBefore();
	long startTime = markerTime - startBefore;
	if( startTime < 0 ) {
		startTime = 0;
	}
	return (int) startTime;
}

public int getStopTime(){
	long markerTime = songUserInfo.getCurrentStopMarker().getTime();
	long stopAfter = songUserInfo.getStopAfter();
	return (int) (markerTime + stopAfter);
}

public int getWaitBetween() {
	return songUserInfo.getWait();
}

public void setWaitBetween(int waitBetween) {
	songUserInfo.setWait( waitBetween );
	saveData();
}

public int getLoop() {
	return songUserInfo.getLoopTimes();
}

public void setLoop(int loop) {
	songUserInfo.setLoopTimes( loop );
	saveData();
}

public int getStartBefore() {
	return songUserInfo.getStartBefore();
}

public void setStartBefore(int startBefore) {
	songUserInfo.setStartBefore( startBefore );
	saveData();
}

public int getStopAfter() {
	return songUserInfo.getStopAfter();
}

public void setStopAfter(int stopAfter) {
	songUserInfo.setStopAfter( stopAfter );
	saveData();
}

public int getPauseBefore() {
	return songUserInfo.getPauseBefStart();
}

public void setPauseBefore(int pauseBefore) {
	songUserInfo.setPauseBefStart( pauseBefore );
	saveData();
}

public void setPath(String path) {
	this.path = path;
}

public String getPath() {
	return path;
}

public void incrementNrPlayed(){
	// todo: implement :)
	//	songUserInfo.incrementNrPlayed();
	//	saveData();
}

public boolean hasMarkers(){
	return songUserInfo.hasMarkers();
}

public int getMarkerIndex( Marker m ) {
	return songUserInfo.getMarkerIndex( m );
}

public Marker addMarker(String name, long time) {
	Marker m = songUserInfo.addMarker( name, time );
	saveData();
	return m;
}
public void removeMarker(Marker marker) {
	songUserInfo.removeMarker(marker);
	saveData();
}

public void updateMarker( @SuppressWarnings("unused") Marker marker ) {
	//TODO: implement :)
	songUserInfo.updateMarker( marker );
	saveData();
}

public void discardData(){
	songUserInfo = null;
	System.gc();
}

public void readData() {
	Log.v(TAG, "readData ->" );
	//	songUserInfo = readText( path );
	//}
	//private static SongUserInfo readText(String path) {

	Log.v(TAG, "readText -> path = " + path);
	File file = getTroffFile(path);

	Log.v(TAG, "readText: file = " + file);
	Log.v(TAG, "readText: file.exists = " + file.exists() );

	SongUserInfo sui = null;
	if( file.exists() ) {

		//Read text from file
		StringBuilder text = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
			br.close();
		} catch (IOException e) {
			//You'll need to add proper error handling here
			Log.e(TAG, "readText: exception on reading the file:\n");
			e.printStackTrace();
		}

		Log.v(TAG, "readText: text = " + text);

		Gson g = new Gson();
		try {
			sui = g.fromJson(text.toString(), SongUserInfo.class);
		} catch ( Exception e ) {
			Log.e(TAG, "readText: exception reading file, corrupt json?");
			e.printStackTrace();
		}
	}

	if( sui == null ) {
		sui = new SongUserInfo();
	}

	songUserInfo = sui;
}


private void saveData() {

	//	saveText( path, songUserInfo );
	//}
	//private void saveText( String path, SongUserInfo songUserInfo ) {
	Gson g = new Gson();
	String message = g.toJson( songUserInfo );
	File file = getTroffFile(path);

	if( file.exists() ) {
		//TODO: fix merging of old-file and new file :)
		boolean deleted = file.delete();
		if( !deleted ) {
			Log.e(TAG, "saveText: file was not deleted!" );
		}
	}
	try {
		FileOutputStream f = new FileOutputStream(file);
		PrintWriter pw = new PrintWriter(f);
		pw.print(message);
		pw.flush();
		pw.close();
		f.close();
	} catch (Exception e) {
		Log.e(TAG, "saveText: error:" + e.getMessage());
		e.printStackTrace();
	}
}

private static File getTroffFile( String songPath ){
	int index = songPath.lastIndexOf('/' );
	String dirPath = songPath.substring( 0, index );
	String songName = songPath.substring( index + 1, songPath.length() );
	String troffName = "." + songName + ".troff";
	File dir = new File( dirPath );
	return new File( dir, troffName );
}

public List<Marker> getMarkers() {
	return songUserInfo.getMarkers();
}

}// end Class
