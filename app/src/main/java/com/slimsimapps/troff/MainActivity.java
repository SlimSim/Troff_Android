package com.slimsimapps.troff;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.TextView;

import com.slimsimapps.troff.Models.Marker;
import com.slimsimapps.troff.Models.Song;
import com.slimsimapps.troff.MusicService.MusicBinder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

//    private static final String TAG = "MainActivity";

    private MusicService musicSrv;
    private Intent playIntent;
//    private boolean musicBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            case R.id.action_settings:
                musicSrv.printCurrSong();
                break;
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

// --------------------------- below here is the own added methods :) ------------------------------

    private void createMarker() {
        final long time = musicSrv.getCurrentPosition();
        final EditText nameView = new EditText(getContext());
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.create_marker)
                .setMessage( getResources().getString(R.string.create_marker_at) + " " + time)
                .setView( nameView )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        String name = nameView.getText().toString();
                        doMarker(musicSrv.saveMarker(name, time));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void doMarker( Marker marker){
        ((LinearLayout) findViewById(R.id.marker_list)).addView(inflateMarker(marker));
    }

    public Context getContext() {
        return this;
    }

    private View inflateMarker( Marker marker ) {
        View child = getLayoutInflater().inflate(R.layout.marker, null);
        child.setTag(marker);
        ((TextView) child.findViewById(R.id.marker_time)).setText( marker.getDisplayTime());
        ((TextView) child.findViewById(R.id.marker_title)).setText(marker.getName());
        return child;
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            ArrayList<Song> songList = musicSrv.setSongList();
            ListView songView = (ListView) findViewById(R.id.song_list);
            songView.setAdapter( new SongAdapter(getContext(), songList) );

            musicSrv.setOwnOnPreparedListener(new MusicService.OwnOnPreparedListener() {
                @Override
                public void notifyEndTime(long endTime) {
                    for(Marker marker : musicSrv.getCurrentMarkers() ){
                        doMarker( marker );
                    }
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

    private void resetSonglistBackgroundColor() {
        ListView songList = ((ListView) findViewById( R.id.song_list ));
        for(int i = 0; i < songList.getChildCount(); i++ ) {
            songList.getChildAt(i).setBackgroundColor(0);
        }
    }

    public void songPicked(View view) {
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        LinearLayout markerList = ((LinearLayout) findViewById(R.id.marker_list));

        resetSonglistBackgroundColor();
        view.setBackgroundColor( ContextCompat.getColor(getContext(), R.color.colorAccent) );
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_play);


        markerList.removeAllViews();
        // när låten är laddad kös notifyEndTime ovan, där sätts nya markörer.
    }

    public void selectMarker(View view) {
        Marker marker = (Marker) view.getTag();
        musicSrv.seekTo( (int) marker.getTime() );
    }
}
