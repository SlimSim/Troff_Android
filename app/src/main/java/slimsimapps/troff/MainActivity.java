package slimsimapps.troff;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
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

import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

import slimsimapps.troff.Models.Marker;
import slimsimapps.troff.Models.Song;
import slimsimapps.troff.MusicService.MusicBinder;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";

	private final int READ_EXTERNAL_STORAGE_INT = 3;

    private MusicService musicSrv;
    private Intent playIntent;
    private G G;
//    private boolean musicBound=false;

    ViewTreeObserver.OnGlobalLayoutListener onRotationListener;

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final View view = findViewById( R.id.timeBarParent );

        onRotationListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recalculateTimeLine();
                view.getViewTreeObserver().removeOnGlobalLayoutListener( onRotationListener );
            }
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener( onRotationListener );
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
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            ((NavigationView) findViewById(R.id.nav_view)).getMenu().getItem(0).setChecked(true);
                        }
                    }, 0);
            return;
        }
        findViewById(R.id.song_list).setVisibility(View.GONE);
        findViewById(R.id.marker_include).setVisibility(View.VISIBLE);

        recalculateTimeLineSoon();
    }

    private void recalculateTimeLineSoon() {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        recalculateTimeLine();
                    }
                }, 0);
    }

    private void recalculateTimeLine() {
        FrameLayout parent = (FrameLayout) findViewById( R.id.timeBarParent );
        SeekBar seekBar = (SeekBar) findViewById( R.id.timeBar );

        seekBar.getLayoutParams().width = parent.getHeight();
        seekBar.requestLayout();
    }

    private void showSongList() {
        findViewById( R.id.song_list ).setVisibility( View.VISIBLE );
        findViewById( R.id.marker_include ).setVisibility( View.GONE );
    }

// --------------------------- below here is the own added methods :) ------------------------------

    public void editMarker(View view) {
        //todo: make all markers attribute editable :)

        final LinearLayout parent =(LinearLayout) view.getParent();
        final Marker m = (Marker) parent.getTag();

        View editMarker = getLayoutInflater().inflate(R.layout.edit_marker, null, false);
        editMarker.findViewById( R.id.edit_marker_time_row ).setVisibility( View.GONE ); // todo: remove
        ((TextView) editMarker.findViewById(R.id.marker_title)).setText( m.getName() );
//        ((EditText) editMarker.findViewById(R.id.marker_time)).setText( Double.toString( m.getTime() ) ); // or DisplayTime?
//        editMarker.findViewById(R.id.marker_title).requestFocus();
//        G.showKeyboard(); //todo: show keyboard
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.edit_marker)
                .setView( editMarker )
                .setNeutralButton( R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        G.hideKeyboard( getWindow() );
                        if( musicSrv.getCurrentMarkers().size() <= 2 ) {
                            Toast.makeText(getContext(), R.string.instr_no_fewer_than_2_markers,
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        new AlertDialog.Builder(getContext())
                                .setTitle( R.string.delete )
                                .setMessage(R.string.about_to_delete)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((LinearLayout) parent.getParent()).removeView(parent);
                                        musicSrv.removeMarker( m.getId() );
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        G.hideKeyboard( getWindow() );
                                    }
                                })
                                .setIcon(R.drawable.ic_marker) //todo: fix a "warnign" icon
                                .show();
                    }
                })
                /*
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
                */
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        G.hideKeyboard( getWindow() );
                    }
                })
                .setIcon(R.drawable.ic_marker)
                .show();
    }

    private void createMarker() {
        if( !musicSrv.isSongSelected() ) {
            Toast.makeText(getContext(), R.string.pick_song_first, Toast.LENGTH_SHORT).show();
            return;
        }
        final long time = musicSrv.getCurrentPosition();
        //TODO; how do I get away with the 2 warnings here?
        final View editMarker = getLayoutInflater().inflate(R.layout.edit_marker, null, false);
        ((EditText) editMarker.findViewById(R.id.marker_time)).setText( G.getDetailedDisplayTime(time) );
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
                        Long newTime = G.getTimeFromDetailedDisplay( newStringTime );

                        String name = ((EditText) editMarker.findViewById(R.id.marker_title)).getText().toString();

                        if( name.isEmpty() ) {
                            Toast.makeText(getContext(),
                                    R.string.instr_name_marker, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if( newTime == -1 ) {
                            Toast.makeText(getContext(),
                                    R.string.instr_correkt_marker_time, Toast.LENGTH_SHORT).show();
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

    private void doMarker( Marker newMarker ) {
        View markerList = findViewById(R.id.marker_list);
        View newMarkerView = getLayoutInflater().inflate(R.layout.marker, (ViewGroup) markerList, false);
        newMarkerView.setTag( newMarker );
        ((TextView) newMarkerView.findViewById(R.id.marker_time)).setText( newMarker.getDisplayTime());
        ((TextView) newMarkerView.findViewById(R.id.marker_title)).setText(newMarker.getName());

        int size = ((ViewGroup) markerList).getChildCount();

        for(int i = 0; i < size; i++ ) {
            Marker m = (Marker) ((ViewGroup) markerList).getChildAt(i).getTag();
            if( newMarker.getTime() < m.getTime() ) {
                ((ViewGroup) markerList).addView( newMarkerView, i );
                return;
            }
        }

        ((ViewGroup) markerList).addView( newMarkerView );
    }

    private Context getContext() {
        return this;
    }

	public boolean haveExternalPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED) {
			return true;
		} else {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					READ_EXTERNAL_STORAGE_INT);
			return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case READ_EXTERNAL_STORAGE_INT: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initiateMusicService();
				} else {
					new AlertDialog.Builder(getContext())
							.setTitle("Extornal storage krävs")
							.setMessage("för att kunna köra Troff :) ")
							.show();
				}
			}
		}
	}

	//connect to the service
    final private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder)service;
            //get service
            musicSrv = binder.getService();
			if( haveExternalPermission() ) {
				initiateMusicService();
			}
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
	        musicSrv = null;
        }
    };

	public void initiateMusicService() {
		ArrayList<Song> songList = musicSrv.setSongList();
		ListView songView = (ListView) findViewById(R.id.song_list);
		songView.setAdapter(new SongAdapter(getContext(), songList, musicSrv));

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
						updateUiTime( time );
					}
				});
			}

			@Override
			public void songCompleted() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						resetSongUI();
					}
				});
			}

			@Override
			public void selectStartMarkerIndex(
					final int markerIndex ) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						selectMarkerUi(((LinearLayout)
								findViewById( R.id.marker_list )
						).getChildAt( markerIndex ));
					}
				});
			}

			@Override
			public void selectStopMarkerIndex(
					final int stopMarkerIndex ) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						selectStopMarkerUi(((LinearLayout)
								findViewById( R.id.marker_list )
						).getChildAt( stopMarkerIndex ));
					}
				});
			}
		});
	}

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
        unbindService(musicConnection);
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
        resetSongUI();

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
        musicSrv.selectStartMarker( marker );
    }

    public void selectEndMarker(View view) {
        Marker marker = (Marker) ((View) view.getParent()).getTag();
        musicSrv.selectEndMarker( marker );
    }

	public void selectMarkerUi( View view ) {
		View markerList = findViewById(R.id.marker_list);
		int size = ((ViewGroup) markerList).getChildCount();
		for(int i = 0; i < size; i++ ) {
			((ViewGroup) markerList).getChildAt(i)
					.findViewById( R.id.marker_title)
					.setBackgroundColor( Color.TRANSPARENT );
		}

		view.findViewById(R.id.marker_title)
				.setBackgroundColor( ContextCompat.getColor(
						getContext(), R.color.colorAccent
				));
	}

	public void selectStopMarkerUi( View view ) {
		View markerList = findViewById(R.id.marker_list);
		int size = ((ViewGroup) markerList).getChildCount();
		for(int i = 0; i < size; i++ ) {
			((ViewGroup) markerList).getChildAt(i)
					.findViewById( R.id.marker_stop)
					.setBackgroundColor( Color.TRANSPARENT );
		}

		view.findViewById(R.id.marker_stop)
				.setBackgroundColor( ContextCompat.getColor(
						getContext(), R.color.colorAccent
				) );
	}

	private void resetSongUI() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(android.R.drawable.ic_media_play);
        updateUiTime( musicSrv.getCurrentPosition() );
    }

    private void updateUiTime( long time ) {
        final SeekBar timeBar = (SeekBar) findViewById(R.id.timeBar);
        timeBar.setProgress( (int) time );
        ((TextView) findViewById(R.id.currentDisplayTime)).setText(G.getDisplayTime( time ));
    }
}
