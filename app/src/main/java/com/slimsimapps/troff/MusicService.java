package com.slimsimapps.troff;

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
import android.util.Log;

import com.slimsimapps.troff.Models.Marker;
import com.slimsimapps.troff.Models.Song;
import com.slimsimapps.troff.database.DB;

/**
 * Created on 2016-10-17, by Slim Sim
 */


public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    public enum PlayStatus {
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
        Log.v(TAG, "onCompletion ->");
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

    public PlayStatus playOrPause() {
        if( player.isPlaying() ) {
            player.pause();
            schedule.shutdown();
            return PlayStatus.PAUSED;
        } else {
            player.start();

            int delay = 0;
            int period = 10;
            schedule = Executors.newScheduledThreadPool(1);
            schedule.scheduleWithFixedDelay(new Runnable() {
                @Override
                public void run() {
                    musicServiceListener.getCurrentTime( getCurrentPosition() );
                }
            }, delay, period, TimeUnit.MILLISECONDS);

            Song song = getSong();
            song.incrementNrPlayed();
            db.updateSong(song);
            return PlayStatus.PLAYING;
        }
    }

    public void seekTo(int time) {
        player.seekTo( time );
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
        return db.insertMarker( new Marker(name, time, getCurrSongId() ) );
    }

    public void removeMarker(int markerId) {
        db.removeMarker( getCurrSongId(), markerId );
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

    public class MusicBinder extends Binder {
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
            m = new Marker("Halfway", mp.getDuration() / 2, songId);
            db.insertMarker(m);
            currentMarkers.add(m);

            m = new Marker("End", mp.getDuration(), songId);
            db.insertMarker(m);
            currentMarkers.add(m);
        }

        musicServiceListener.notifyEndTime(mp.getDuration());
    }

    public void setSong(int songIndex) {
        selectedSongNr = songIndex;
        player.reset();
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

    public interface musicServiceListener {
        void notifyEndTime(long endTime);
        void getCurrentTime(long currentTime);
    }

}