package slimsimapps.troff;

import android.Manifest;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

    enum PlayStatus {
        PLAYING,
        PAUSED
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
    private musicServiceListener musicServiceListener;

    private Marker startMarker;
    private Marker endMarker;


    public void setMusicServiceListener(musicServiceListener musicServiceListener) {
        this.musicServiceListener = musicServiceListener;
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
	    if( startMarker != null ) {
		    startTime = (int) startMarker.getTime();
	    }
	    player.seekTo( startTime );
	    pause();
	    musicServiceListener.songCompleted();
    }

    public void onCreate() {
        super.onCreate();

        selectedSongNr = -1;
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
        schedule.shutdownNow();
        return PlayStatus.PAUSED;
    }

    private PlayStatus play() {
        player.start();

        int delay = 0;
        int period = 10;
		final MusicService that = this;
        schedule = Executors.newScheduledThreadPool(1);
        schedule.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
				if( endMarker != null && getCurrentPosition() >= endMarker.getTime() ) {
					that.completeSong();
				} else {
					musicServiceListener.getCurrentTime( getCurrentPosition() );
				}
            }
        }, delay, period, TimeUnit.MILLISECONDS);

        Song song = getSong();
        song.incrementNrPlayed();
        db.updateSong(song);
        return PlayStatus.PLAYING;
    }

    public PlayStatus playOrPause() {
        if( player.isPlaying() ) {
            return this.pause();
        } else {
            return play();
        }
    }

    public void seekTo(int time) {
        player.seekTo( time );
    }

    public void selectStartMarker( Marker marker ) {
		startMarker = marker;
		seekTo( (int) marker.getTime() );
		musicServiceListener.getCurrentTime( (int) marker.getTime() );
	}

	public void selectEndMarker( Marker marker ) {
		endMarker = marker;
	}

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public int getSelectedSongNr() {
        return selectedSongNr;
    }

/*
    public long getDuration() {
        return player.getDuration();
    }
*/

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

        startMarker = currentMarkers.get(0);
	    endMarker = currentMarkers.get( currentMarkers.size() -1 );

        musicServiceListener.notifyEndTime(mp.getDuration());
    }

    public void setSong(int songIndex) {
        if( player.isPlaying() ) {
            this.pause();
        }
        selectedSongNr = songIndex;
        player.reset();
        //todo: ev fix better way to handle the schedule
        if( schedule != null && !schedule.isTerminated() ) {
            schedule.shutdownNow();
        }
        Song song = getSong();
        Uri trackUri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song.getFileId() );

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    private Song getSong() {
        Song song = songs.get( selectedSongNr );

        if( song.getId() == -1 ) {
            Song dbSong = db.getSong( getCurrSongFileId() );
            if( dbSong == null ) {
                dbSong = db.insertSong( song );
            }
            song = dbSong;
            songs.set( selectedSongNr, song );
        }
        return song;
    }

    interface musicServiceListener {
        void notifyEndTime(long endTime);
        void getCurrentTime(long currentTime);
        void songCompleted();
    }

}