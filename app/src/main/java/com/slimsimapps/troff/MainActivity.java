package com.slimsimapps.troff;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.slimsimapps.troff.Models.Marker;
import com.slimsimapps.troff.Models.Song;
import com.slimsimapps.troff.MusicService.MusicBinder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";

    private MusicService musicSrv;
    private Intent playIntent;
    private G G;
//    private boolean musicBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        G = new G( this ); // initiate the global helper variable!

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setTitle( R.string.pick_song );

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( !musicSrv.isSongSelected() ){
                    Toast.makeText(getContext(), R.string.pick_song_first, Toast.LENGTH_SHORT).show();
                    return;
                }
                if( musicSrv.playOrPause() == MusicService.PlayStatus.PLAYING ){
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    fab.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
/*
            case R.id.action_settings:
                musicSrv.printCurrSong();
                break;
*/
            case R.id.action_create_marker:
                createMarker();
                break;
            case R.id.action_end:
                stopService(playIntent);
                musicSrv=null;
                System.exit(0);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch( id ) {
            case R.id.nav_song_list:
                showSongList();
                break;
            case R.id.nav_markers:
                showMarkerTimeLine();
                break;
        }
        /*
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showMarkerTimeLine() {
        if( !musicSrv.isSongSelected() ) {
            Toast.makeText(getContext(), R.string.pick_song_first, Toast.LENGTH_SHORT).show();
            return;
        }
        findViewById(R.id.song_list).setVisibility(View.GONE);
        findViewById(R.id.marker_include).setVisibility(View.VISIBLE);
    }

    private void showSongList() {
        findViewById( R.id.song_list ).setVisibility( View.VISIBLE );
        findViewById( R.id.marker_include ).setVisibility( View.GONE );
    }

// --------------------------- below here is the own added methods :) ------------------------------

    public void editMarker(View view) {
    }

    private void createMarker() {
        if( !musicSrv.isSongSelected() ) {
            Toast.makeText(getContext(), R.string.pick_song_first, Toast.LENGTH_SHORT).show();
            return;
        }
        final long time = musicSrv.getCurrentPosition();
        //TODO; how do I get away with the 2 warnings here?
        final View editMarker = getLayoutInflater().inflate(R.layout.edit_marker, null, false);
        ((EditText) editMarker.findViewById(R.id.marker_time)).setText( Double.toString( time/1000D ) );
        editMarker.findViewById(R.id.marker_title).requestFocus();
        G.showKeyboard();
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.create_marker)
//                .setMessage(
//                        getResources().getString(R.string.create_marker_at) +
//                        " " + Marker.getDisplayTime(time))
                .setView( editMarker )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){

                        G.hideKeyboard( getWindow() );
                        String newStringTime = ((EditText) editMarker.findViewById(R.id.marker_time)).getText().toString();
                        Long newTime = (long) (Double.parseDouble( newStringTime ) * 1000);

                        String name = ((EditText) editMarker.findViewById(R.id.marker_title)).getText().toString();

                        if( name.isEmpty() ) {
                            return;
                        }

                        doMarker(musicSrv.saveMarker(name, newTime));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        G.hideKeyboard( getWindow() );
                    }
                })
                .setIcon(R.drawable.ic_marker)
                .show();

    }

    private void doMarker( Marker marker ) {
        View markerList = findViewById(R.id.marker_list);
        View child = getLayoutInflater().inflate(R.layout.marker, (ViewGroup) markerList, false);
        child.setTag(marker);
        ((TextView) child.findViewById(R.id.marker_time)).setText( marker.getDisplayTime());
        ((TextView) child.findViewById(R.id.marker_title)).setText(marker.getName());

        ((LinearLayout) markerList).addView( child );
    }

    private Context getContext() {
        return this;
    }

    //connect to the service
    final private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            ArrayList<Song> songList = musicSrv.setSongList();
            ListView songView = (ListView) findViewById(R.id.song_list);
            songView.setAdapter( new SongAdapter(getContext(), songList, musicSrv) );

            final TextView currentDisplayTime = (TextView) findViewById(R.id.currentDisplayTime);
            final SeekBar timeBar = (SeekBar) findViewById(R.id.timeBar);

            timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    musicSrv.seekTo( seekBar.getProgress() );
                }
            });

            musicSrv.setMusicServiceListener(new MusicService.musicServiceListener() {
                @Override
                public void notifyEndTime(long endTime) {
                    timeBar.setMax( (int) endTime );
                    for(Marker marker : musicSrv.getCurrentMarkers() ){
                        doMarker( marker );
                    }
                }

                @Override
                public void getCurrentTime(final long time) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String strTime = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(time),
                                    TimeUnit.MILLISECONDS.toSeconds(time) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time)));
                            currentDisplayTime.setText( strTime );
                            timeBar.setProgress( (int) time );
                        }
                    });
                }
            });
//            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

    private void resetSongListBackgroundColor() {
        ListView songList = ((ListView) findViewById( R.id.song_list ));
        for(int i = 0; i < songList.getChildCount(); i++ ) {
            songList.getChildAt(i).setBackgroundColor(0);
        }
    }

    public void songPicked(View view) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        LinearLayout markerList = ((LinearLayout) findViewById(R.id.marker_list));

        resetSongListBackgroundColor();
        view.setBackgroundColor( ContextCompat.getColor(getContext(), R.color.colorAccent) );
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_play);

        String title = (String) ((TextView) view.findViewById(R.id.song_title)).getText();
        String artist = (String) ((TextView) view.findViewById(R.id.song_artist)).getText();

        ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setTitle( title + ", " + artist );

        ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(1).setChecked(true);
        showMarkerTimeLine();

        markerList.removeAllViews();
        // The function notifyEndTime above will be called when the song is loaded
        // it will create the markers.
    }

    public void selectMarker(View view) {
        Marker marker = (Marker) view.getTag();
        musicSrv.seekTo( (int) marker.getTime() );
    }

    public void selectEndMarker(View view) {
    }


}
