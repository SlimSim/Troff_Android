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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.provider.MediaStore;
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

private PlayStatus currentStatus;

enum PlayStatus {
	PLAYING,
	COUNTING,
	PAUSED
}

@SuppressWarnings("unused")
private static final String TAG = "MusicService";

private ScheduledExecutorService timeUpdateSchedule;
private ScheduledFuture waitBetweenSchedule;

private MediaPlayer player;
private final IBinder musicBind = new MusicBinder();

private ArrayList<Song> songs;
private int selectedSongNr;
private List<Marker> currentMarkers;
final private DB db = new DB(MusicService.this);
private MusicServiceListener callOut;

private Marker startMarker;
private Marker endMarker;


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

private void completeSong() {
	int startTime = 0;
	Song song = getSong();
	if( startMarker != null ) {
		startTime = ((int) startMarker.getTime()) -
				song.getStartBefore();

		if( startTime < 0 ) {
			startTime = 0;
		}
	}
	player.seekTo( startTime );
	pause();

	if( nrLoopsLeft == 0 ) {
		callOut.songCompleted();
	} else {
		currentStatus = PlayStatus.COUNTING;
		waitBetweenSchedule = Executors
				.newScheduledThreadPool(1).schedule(new Runnable() {
			@Override
			public void run() {
				play();
			}
		}, song.getWaitBetween(), TimeUnit.SECONDS);
	}
}

public void onCreate() {
	super.onCreate();

	selectedSongNr =  (int) db.getCurrentSong();
	player = new MediaPlayer();
	initMusicPlayer();
}
private void initMusicPlayer(){
	player.setWakeMode(getApplicationContext(),
			PowerManager.PARTIAL_WAKE_LOCK);
	player.setAudioStreamType(AudioManager.STREAM_MUSIC);
	player.setOnPreparedListener(this);
	player.setOnCompletionListener(this);
	player.setOnErrorListener(this);
}

private PlayStatus pause() {
	player.pause();
	currentStatus = PlayStatus.PAUSED;
	timeUpdateSchedule.shutdownNow();
	if( waitBetweenSchedule != null ) {
		waitBetweenSchedule.cancel( true );
	}
	return PlayStatus.PAUSED;
}

private PlayStatus play() {
	player.start();
	nrLoopsLeft--;
	callOut.nrLoopsLeft(nrLoopsLeft);

	int delay = 0;
	int period = 10;
	final MusicService that = this;
	timeUpdateSchedule = Executors.newScheduledThreadPool(1);
	timeUpdateSchedule.scheduleWithFixedDelay(new Runnable() {
		@Override
		public void run() {
			Song song = getSong();
			long stopTime = endMarker.getTime() +
					song.getStopAfter();

			if( endMarker != null &&
					getCurrentPosition() >= stopTime ) {
				that.completeSong();
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
	return PlayStatus.PLAYING;
}

public PlayStatus playOrPause() {
	if( currentStatus == PlayStatus.PLAYING ) {
		return this.pause();
	} else if( currentStatus == PlayStatus.COUNTING ) {
		return this.pause();
	} else {
		nrLoopsLeft = nrLoops;
		callOut.nrLoopsLeft(nrLoopsLeft);
		return play();
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

@SuppressWarnings("unused")
public void printCurrSong() {
	Log.d(TAG, "printCurrSong: songs   = " + songs.get( selectedSongNr ) );
	Log.d(TAG, "printCurrSong: fileId  = " + getCurrSongFileId() );
	Log.d(TAG, "printCurrSong: db-song = " + db.getSong( getCurrSongFileId() ));
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
		this.pause();
	}
	selectedSongNr = songIndex;
	player.reset();
	//todo: ev fix better way to handle the timeUpdateSchedule
	if( timeUpdateSchedule != null && !timeUpdateSchedule.isTerminated() ) {
		timeUpdateSchedule.shutdownNow();
	}
	Song song = getSong();
	nrLoops = song.getLoop();

	callOut.onLoadedSong( song );
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
}

interface MusicServiceListener {
	void notifyEndTime(long endTime);
	void getCurrentTime(long currentTime);
	void songCompleted();
	void selectStartMarkerIndex(int i);
	void selectStopMarkerIndex(int i);
	void nrLoopsLeft(int nrLoopsLeft);
	void onLoadedSong(Song song);
}

}// end Class