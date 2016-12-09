package com.slimsimapps.troff;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import java.util.ArrayList;
import android.content.ContentUris;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.PowerManager;
import android.util.Log;

/**
 * Created on 2016-10-17, by Slim Sim
 */


public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = "MusicService";

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private int selectedSongNr;
    private final IBinder musicBind = new MusicBinder();

    public void setOwnOnPreparedListener(OwnOnPreparedListener ownOnPreparedListener) {
        this.ownOnPreparedListener = ownOnPreparedListener;
    }

    private OwnOnPreparedListener ownOnPreparedListener;

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion ->");

    }

    public void onCreate() {
        //create the service
        super.onCreate();
        //initialize position
        selectedSongNr = 0;
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
    }
    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    public void playOrPause() {
        Log.v(TAG, "playOrPause ->");
        if( player.isPlaying() ) {
            player.pause();
        } else {
            player.start();
        }
    }

    public void seekTo(int time) {
        player.seekTo( time );
    }

    public long getCurrentPosition() {
        Log.d(TAG, "getCurrentPosition -> ");
        return player.getCurrentPosition();
    }

    public long getDuration() {
        return player.getDuration();
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
        ownOnPreparedListener.notifyEndTime(mp.getDuration());
    }

    public void setSong(int songIndex){
        selectedSongNr = songIndex;

        //play a song
        player.reset();
        //get song
        Song playSong = songs.get(selectedSongNr);
        //get id
        long currSong = playSong.getID();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();

    }

    public interface OwnOnPreparedListener {
        void notifyEndTime(long endTime);
    }

}