package slimsimapps.troff;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import slimsimapps.troff.Models.Marker;
import slimsimapps.troff.Models.Song;
import slimsimapps.troff.database.DB;

/**
 * Created on 2016-10-17, by Slim Sim
 */
public class MusicService extends Service implements
		MediaPlayer.OnPreparedListener,
		MediaPlayer.OnErrorListener,
		MediaPlayer.OnCompletionListener {

private int nrLoops;

private int nrLoopsLeft;

private int waitTimeLeft;

private PlayStatus currentStatus;

private enum PlayStatus {
	PLAYING,
	WAITING,
	STOPPED
}

@SuppressWarnings("unused")
private static final String TAG = "MusicService";

private ScheduledExecutorService schedule;

private MediaPlayer player;
private final IBinder musicBind = new MusicBinder();

private ArrayList<Song> songs;
private int selectedSongNr;
private List<Marker> currentMarkers;
final private DB db = new DB(MusicService.this);
private MusicServiceListener callOut;

private Marker startMarker;
private Marker endMarker;


@SuppressWarnings("unused")
public String toString() {
	return "ms: {" +
			"\n  nrLoops = " + nrLoops +
			"\n  nrLoopsLeft = " + nrLoopsLeft +
			"\n  currentStatus = " + currentStatus +
			"}";
}

public void setCallOut(MusicServiceListener callOut) {
	this.callOut = callOut;
}

public boolean isSongSelected() {
	return selectedSongNr != -1;
}

@Override
public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
	return false;
}

@Override
public void onCompletion(MediaPlayer mediaPlayer) {
	completeSong();
}

private int getStartTime() {
	int startTime;
	Song song = getSong();
	startTime = ((int) startMarker.getTime()) - song.getStartBefore();

	if( startTime < 0 ) {
		startTime = 0;
	}
	return startTime;
}

private void completeSong() {
	nrLoopsLeft--;
	if( nrLoopsLeft == 0 ) {
		stopSong();
	} else {
		waitSong( getSong().getWaitBetween() );
	}
}

public void onCreate() {
	super.onCreate();

	selectedSongNr =  (int) db.getCurrentSong();
	player = new MediaPlayer();
	initMusicPlayer();
}

private void stopSchedule() {
	if( schedule != null && !schedule.isTerminated() ) {
		schedule.shutdownNow();
	}
}

@NonNull
private ScheduledExecutorService newSchedule() {
	stopSchedule();
	return Executors.newScheduledThreadPool(1);
}

private void initMusicPlayer(){
	player.setWakeMode(getApplicationContext(),
			PowerManager.PARTIAL_WAKE_LOCK);
	player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	player.setOnPreparedListener(this);
	player.setOnCompletionListener(this);
	player.setOnErrorListener(this);
}

private void stopSong() {
	if( player.isPlaying() ) {
		player.pause();
	}
	int startTime = getStartTime();
	player.seekTo( startTime );
	callOut.getCurrentTime( startTime );

	currentStatus = PlayStatus.STOPPED;
	nrLoopsLeft = nrLoops;
	getSong().getLoop();

	stopSchedule();
	callOut.nrLoopsLeft( nrLoopsLeft );
	callOut.nrSecondsLeft( getSong().getPauseBefore() );
	callOut.onStop();
}

private void waitSong( int secondsBeforeStart ) {
	if( player.isPlaying() ) {
		player.pause();
	}

	int startTime = getStartTime();
	player.seekTo( startTime );
	callOut.getCurrentTime( startTime );

	currentStatus = PlayStatus.WAITING;
	callOut.onWait();
	callOut.nrLoopsLeft( nrLoopsLeft );

	waitTimeLeft = secondsBeforeStart;

	int delay = 0;
	int period = 1;

	schedule = newSchedule();
	schedule.scheduleWithFixedDelay(new Runnable() {
		@Override
		public void run() {

			if( waitTimeLeft == 0 ) {
				playSong();
			} else {
				callOut.nrSecondsLeft( waitTimeLeft );
				waitTimeLeft--;
			}
		}
	}, delay, period, TimeUnit.SECONDS );
}

private void playSong() {
	player.start();
	callOut.nrLoopsLeft( nrLoopsLeft );
	callOut.nrSecondsLeft( 0 );
	callOut.onPlay();

	int delay = 0;
	int period = 10;

	schedule = newSchedule();
	schedule.scheduleWithFixedDelay(new Runnable() {
		@Override
		public void run() {
			// This is run constantly and instantly
			// when the song is playing

			Song song = getSong();
			long stopTime = endMarker.getTime() +
					song.getStopAfter();

			if( endMarker != null &&
					getCurrentPosition() >= stopTime ) {
				completeSong();
			} else {
				callOut.getCurrentTime(
						getCurrentPosition() );
			}
		}
	}, delay, period, TimeUnit.MILLISECONDS);

	Song song = getSong();
	song.incrementNrPlayed();
	db.updateSong(song);
	currentStatus = PlayStatus.PLAYING;
}

public void playOrPause() {
	if( currentStatus == PlayStatus.PLAYING ) {
		this.stopSong();
	} else if( currentStatus == PlayStatus.WAITING) {
		this.stopSong();
	} else {
		nrLoopsLeft = nrLoops;
		waitSong( getSong().getPauseBefore() );
	}
}

public void seekTo(int time) {
	player.seekTo( time );
}

public void selectStartMarker( Marker marker ) {
	Song song = getSong();
	startMarker = marker;
	int t = (int) marker.getTime() - song.getStartBefore();
	if( t < 0 ) {
		t = 0;
	}
	seekTo( t );
	callOut.getCurrentTime( t );
	int index = getMarkerIndex( marker );
	callOut.selectStartMarkerIndex( index );

	song.setSelectedStartMarker( index );
	db.updateSong( song );
}

private int getMarkerIndex( Marker m ) {
	Collections.sort(currentMarkers);
	return currentMarkers.indexOf( m );
}

public void selectEndMarker( Marker marker ) {
	endMarker = marker;
	int index = getMarkerIndex( marker );
	callOut.selectStopMarkerIndex( index );

	Song song = getSong();
	song.setSelectedEndMarker( index );
	db.updateSong( song );
}

public void setPauseBefore( int pauseBefore ) {
	Song song = getSong();
	song.setPauseBefore( pauseBefore );
	db.updateSong( song );
}

public void setWaitBetween( int waitBetween ) {
	Song song = getSong();
	song.setWaitBetween( waitBetween );
	db.updateSong( song );
}

public void setStartBefore( int startBefore ) {
	Song song = getSong();
	song.setStartBefore( startBefore );
	db.updateSong( song );
}

public void setStopAfter( int stopAfter ) {
	Song song = getSong();
	song.setStopAfter( stopAfter );
	db.updateSong( song );
}

public long getCurrentPosition() {
	return player.getCurrentPosition();
}

public int getSelectedSongNr() {
	return selectedSongNr;
}

public List<Marker> getCurrentMarkers() {
	return currentMarkers;
}

public Marker saveMarker(String name, long time) {
	Marker m = db.insertMarker( new Marker(name, time, getCurrSongId() ) );
	currentMarkers = db.getAllMarkers( getCurrSongId() );
	return m;
}

public void removeMarker(int markerId) {
	db.removeMarker( getCurrSongId(), markerId );
	currentMarkers = db.getAllMarkers( getCurrSongId() );
}

public void updateMarker( Marker marker ) {
	db.updateMarker( marker );
	currentMarkers = db.getAllMarkers( getCurrSongId() );
}

private int getCurrSongId() {
	return songs.get( selectedSongNr ).getId();
}
private long getCurrSongFileId() {
	return songs.get( selectedSongNr ).getFileId();
}


public ArrayList<Song> setSongList() {

	ArrayList<Song> songList = new ArrayList<>();

	//retrieve song info
	ContentResolver musicResolver = getContentResolver();
	Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

	if (musicCursor != null && musicCursor.moveToFirst()) {
		//get columns
		int titleColumn = musicCursor.getColumnIndex
				(android.provider.MediaStore.Audio.Media.TITLE);
		int idColumn = musicCursor.getColumnIndex
				(android.provider.MediaStore.Audio.Media._ID);
		int artistColumn = musicCursor.getColumnIndex
				(android.provider.MediaStore.Audio.Media.ARTIST);

		//add songs to list
		do {
			long fileId = musicCursor.getLong(idColumn);
			String thisTitle = musicCursor.getString(titleColumn);
			String thisArtist = musicCursor.getString(artistColumn);
			//Song newSong = musicSrv.getSongFromDB( fileId, thisTitle, thisArtist );
			Song newSong = new Song();
			newSong.setTitle( thisTitle );
			newSong.setArtist( thisArtist );
			newSong.setFileId( fileId );
			songList.add( newSong );
		}
		while (musicCursor.moveToNext());
		musicCursor.close();
	}

	Collections.sort(songList, new Comparator<Song>(){
		public int compare(Song a, Song b){
			return a.getTitle().compareTo(b.getTitle());
		}
	});

	songs = songList;
	return songList;
}

class MusicBinder extends Binder {
	MusicService getService() {
		return MusicService.this;
	}
}

@Override
public IBinder onBind(Intent intent) {
	return musicBind;
}

@Override
public boolean onUnbind(Intent intent){
	player.stop();
	player.release();
	return false;
}

@Override
public void onPrepared(MediaPlayer mp) {
	int songId = getCurrSongId();

	currentMarkers = db.getAllMarkers( songId );

	if( currentMarkers.size() == 0 ) {
		Marker m = new Marker("Start", 0, songId);
		db.insertMarker(m);
		currentMarkers.add(m);

		m = new Marker("End", mp.getDuration(), songId);
		db.insertMarker(m);
		currentMarkers.add(m);
	}

	callOut.notifyEndTime( mp.getDuration() );

	Song song = getSong();

	int startMarkerId = song.getSelectedStartMarker();
	int endMarkerId = song.getSelectedEndMarker();

	selectStartMarker( currentMarkers.get( startMarkerId ) );

	if( endMarkerId == -1 ) {
		endMarkerId = currentMarkers.size() - 1;
	}
	selectEndMarker( currentMarkers.get( endMarkerId ) );

}

public void setSong(int songIndex) {
	if( player.isPlaying() ) {
		this.stopSong();
	}
	selectedSongNr = songIndex;
	player.reset();
	currentStatus = PlayStatus.STOPPED;
	Song song = getSong();
	nrLoops = song.getLoop();

	callOut.onLoadedSong( song );
	callOut.nrLoopsLeft( nrLoops );
	callOut.onStop();
	Uri trackUri = ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
			song.getFileId() );

	try{
		player.setDataSource(getApplicationContext(), trackUri);
	}
	catch(Exception e){
		Log.e(TAG, "setSong: Error setting data source", e);
	}
	db.setCurrentSong( songIndex );
	player.prepareAsync();
}

public Song getSong() {
	if( selectedSongNr == -1 ) {
		selectedSongNr = 0;
	}
	Song song = songs.get( selectedSongNr );

	if( song.getId() == -1 ) {
		Song dbSong = db.getSong( getCurrSongFileId() );
		if( dbSong == null ) {
			song.setSelectedEndMarker( 1 );

			dbSong = db.insertSong( song );
		}
		song = dbSong;
		songs.set( selectedSongNr, song );
	}
	return song;
}

public void setLoop( int loop ) {
	this.nrLoops = loop;
	Song song = getSong();
	song.setLoop( loop );
	db.updateSong( song );
	callOut.nrLoopsLeft( loop );
}

interface MusicServiceListener {
	void notifyEndTime(long endTime);
	void getCurrentTime(long currentTime);
	void onStop();
	void onWait();
	void onPlay();
	void selectStartMarkerIndex(int i);
	void selectStopMarkerIndex(int i);
	void nrLoopsLeft(int nrLoopsLeft);
	void nrSecondsLeft( int nrSecondsLeft );
	void onLoadedSong(Song song);
}

}// end Class