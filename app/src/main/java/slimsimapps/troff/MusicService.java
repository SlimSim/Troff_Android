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

import slimsimapps.troff.Exceptions.NoSongsException;
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

private int waitTimeLeft;

private int nrLoopsLeft;

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

final private DB db = new DB(MusicService.this);
private MusicServiceListener callOut;


@SuppressWarnings("unused")
public String toString() {
	return "ms: {" +
			"\n  waitTimeLeft = " + waitTimeLeft +
			"\n  nrLoopsLeft = " + nrLoopsLeft +
			//"\n  startMarker = " + startMarker +
			//"\n  endMarker = " + endMarker +
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

private void completeSong() {
	Song song = getSong();
	if( song.getLoop() == 0 ) {
		waitSong( song.getWaitBetween() );
		return;
	}
	nrLoopsLeft--;
	if( nrLoopsLeft == 0 ) {
		stopSong();
	} else {
		waitSong( song.getWaitBetween() );
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
	int startTime = getSong().getStartTime();
	player.seekTo( startTime );
	callOut.getCurrentTime( startTime );

	currentStatus = PlayStatus.STOPPED;
	nrLoopsLeft = getSong().getLoop();

	stopSchedule();
	callOut.nrLoopsLeft( nrLoopsLeft );
	callOut.nrSecondsLeft( getSong().getPauseBefore() );
	callOut.onStop();
}

private void waitSong( int secondsBeforeStart ) {
	if( player.isPlaying() ) {
		player.pause();
	}

	int startTime = getSong().getStartTime();
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
			song.getStopAfter();

			long stopTime = song.getStopTime();

			if( getCurrentPosition() >= stopTime ) {
				completeSong();
			} else {
				callOut.getCurrentTime( getCurrentPosition() );
			}
		}
	}, delay, period, TimeUnit.MILLISECONDS);

	Song song = getSong();
	song.incrementNrPlayed();
	currentStatus = PlayStatus.PLAYING;
}

public void playOrPause() {
	if( currentStatus == PlayStatus.PLAYING ) {
		this.stopSong();
	} else if( currentStatus == PlayStatus.WAITING) {
		this.stopSong();
	} else {
		Song song = getSong();
		nrLoopsLeft = song.getLoop();
		waitSong( song.getPauseBefore() );
	}
}

public void seekTo(int time) {
	player.seekTo( time );
}

public void saveStartMarker( Marker marker ) {
	getSong().setSelectedStartMarker( marker );
	selectStartMarker( marker );
}

public void saveEndMarker( Marker marker ) {
	getSong().setSelectedEndMarker( marker );
	selectEndMarker( marker );
}

public void selectStartMarker( Marker marker ) {
	int startTime = getSong().getStartTime();
	seekTo( startTime );
	callOut.getCurrentTime( startTime );

	int index = getSong().getMarkerIndex( marker );
	callOut.selectStartMarkerIndex( index );
}

public void selectEndMarker( Marker marker ) {
	int index = getSong().getMarkerIndex( marker );
	callOut.selectStopMarkerIndex( index );
}

public void setStartBefore( int startBefore ) {
	Song song = getSong();
	song.setStartBefore( startBefore );
}

public void setStopAfter( int stopAfter ) {
	Song song = getSong();
	song.setStopAfter( stopAfter );
}

public void setPauseBefore( int pauseBefore ) {
	Song song = getSong();
	song.setPauseBefore( pauseBefore );
	if( currentStatus == PlayStatus.STOPPED ) {
		callOut.nrSecondsLeft(pauseBefore);
	}
}

public void setWaitBetween( int waitBetween ) {
	Song song = getSong();
	song.setWaitBetween( waitBetween );
}

public void setLoop( int loop ) {
	Song song = getSong();
	song.setLoop( loop );
	nrLoopsLeft = loop;
	callOut.nrLoopsLeft( loop );
}

public long getCurrentPosition() {
	return player.getCurrentPosition();
}

public int getSelectedSongNr() {
	return selectedSongNr;
}

public List<Marker> getCurrentMarkers() {
	return getSong().getMarkers();
}

public Marker addMarker(String name, long time) {
	return getSong().addMarker( name, time );
}

public void removeMarker(Marker marker) {
	getSong().removeMarker(marker);
}

public void updateMarker( Marker marker ) {
	getSong().updateMarker( marker );
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
		int pathColumn = musicCursor.getColumnIndex
				(android.provider.MediaStore.Audio.Media.DATA);
		int isMusicColumn = musicCursor.getColumnIndex
				(MediaStore.Audio.Media.IS_MUSIC);

		//add songs to list
		do {
			if( musicCursor.getInt( isMusicColumn ) != 1 ) {
				continue;
			}
			Song newSong = new Song();
			newSong.setTitle( musicCursor.getString(titleColumn) );
			newSong.setArtist( musicCursor.getString( artistColumn ) );
			newSong.setFileId( musicCursor.getLong(idColumn) );
			newSong.setPath( musicCursor.getString(pathColumn) );
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

public Song getSong() {
	if( songs.size() == 0 ) {
		throw new NoSongsException("MusicService / getSong: no songs!");
	}

	if( selectedSongNr == -1 ) {
		selectedSongNr = 0;
	}

	return songs.get( selectedSongNr );
}

public void setSong(int songIndex) {
	if( player.isPlaying() ) {
		this.stopSong();
	}
	getSong().discardData();
	selectedSongNr = songIndex;
	player.reset();
	currentStatus = PlayStatus.STOPPED;
	Song song = getSong();

	song.readData();
	callOut.onStop();
	Uri trackUri = ContentUris.withAppendedId(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
			song.getFileId() );

	try {
		player.setDataSource(getApplicationContext(), trackUri);
	} catch ( Exception e ) {
		Log.e(TAG, "setSong: Error setting data source", e);
	}
	db.setCurrentSong( songIndex );
	player.prepareAsync();
}

class MusicBinder extends Binder {
	MusicService getService() {
		return MusicService.this;
	}
}

@Override
public void onPrepared(MediaPlayer mp) {
	Song song = getSong();

	if( !song.hasMarkers() ) {
		song.addMarker(getResources().getString(R.string.start), 0);
		song.addMarker(getResources().getString(R.string.end), mp.getDuration());
	}

	callOut.onSongLoaded( song, mp.getDuration() );
	callOut.nrLoopsLeft( song.getLoop() );
	callOut.nrSecondsLeft( song.getPauseBefore() );

	selectStartMarker( song.getSelectedStartMarker() );
	selectEndMarker( song.getSelectedEndMarker() );
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

interface MusicServiceListener {
	void onSongLoaded( Song song, long duration );
	void getCurrentTime( long currentTime );
	void onStop();
	void onWait();
	void onPlay();
	void selectStartMarkerIndex( int i );
	void selectStopMarkerIndex( int i );
	void nrLoopsLeft( int nrLoopsLeft );
	void nrSecondsLeft( int nrSecondsLeft );
}

}// end Class