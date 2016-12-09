package com.slimsimapps.troff;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.widget.TextView;

import com.slimsimapps.troff.MusicService.MusicBinder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onCreate_own_code_fixing_songs();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * FIXME: site2 här kanske jag vill ändra ikonen på play knappen,
                 * eller vill jag det i själva musicSrv?
                 */
                Log.v(TAG, "fab onClick ->");
                musicSrv.playOrPause();
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
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
        Log.d(TAG, "createMarker ->");
        final long time = musicSrv.getCurrentPosition();
        final EditText nameView = new EditText(getContext());
        new AlertDialog.Builder(getContext())
                .setTitle("Create Marker")
                .setMessage("create maeker at " + time)
                .setView( nameView )
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        Log.d(TAG, "create the marker");
                        Log.d(TAG, "input = " + nameView.getText() );


                        doMarker("" + nameView.getText(), time);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        Log.d(TAG, "no marker today :(");
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    public void doMarker(String title, long time){
        ((LinearLayout) findViewById(R.id.marker_list)).addView(
                inflateMarker(new Marker(1, "" + title , time))
        );
    }

    public Context getContext() {
        return this;
    }

    private void onCreate_own_code_fixing_songs() {
        songView = (ListView)findViewById(R.id.song_list);
        songList = new ArrayList<>();
        getSongList();
        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });

        Collections.sort(songList, new Comparator<Song>(){
            public int compare(Song a, Song b){
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        SongAdapter songAdt = new SongAdapter(this, songList);
        songView.setAdapter(songAdt);
/*
        for( int i = 0; i < 60; i++ ){
            ((LinearLayout) findViewById(R.id.marker_list)).addView(
                    inflateMarker(new Marker(1, "go to " + i*5 , i*5000))
            );
        }
        */

    }

    private View inflateMarker( Marker marker ) {
        View child = getLayoutInflater().inflate(R.layout.marker, null);
        ((TextView) child.findViewById(R.id.marker_time)).setText("" + marker.getTime());
        ((TextView) child.findViewById(R.id.marker_title)).setText("" + marker.getTitle());
        return child;
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(songList);
            musicSrv.setOwnOnPreparedListener(new MusicService.OwnOnPreparedListener() {
                @Override
                public void notifyEndTime(long endTime) {
                    Log.v(TAG, "notifyEndTime ->");
                    doMarker("Start", 0);
                    doMarker("Halfway", (endTime / 2)); // todo: ta bort denna :)
                    doMarker("End", endTime);
                }
            });
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
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

    public void getSongList() {
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
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }
    }

    private void resetSonglistBackgroundColor() {
        ListView songList = ((ListView) findViewById( R.id.song_list ));
        for(int i = 0; i < songList.getChildCount(); i++ ) {
            songList.getChildAt(i).setBackgroundColor(0);
        }
    }

    public void songPicked(View view){
        musicSrv.setSong(Integer.parseInt(view.getTag().toString()));
        LinearLayout markerList = ((LinearLayout) findViewById(R.id.marker_list));



        resetSonglistBackgroundColor();
        view.setBackgroundColor( getResources().getColor( R.color.colorAccent ) );



        markerList.removeAllViews();
        // när låten är laddad kös notifyEndTime ovan, där sätts nya markörer.
    }

    public void selectMarker(View view) {
        int t = Integer.parseInt("" + ((TextView) view.findViewById(R.id.marker_time)).getText());
        musicSrv.seekTo( t );
    }
}
