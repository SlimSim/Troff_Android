package slimsimapps.troff;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import slimsimapps.troff.Models.Song;

/**
 * Created 2016-10-17, by Slim Sim
 */

class SongAdapter extends BaseAdapter {

@SuppressWarnings("unused")
private static final String TAG = "SongAdapter";

private Context context;
private ArrayList<Song> songs;
private LayoutInflater songInf;
private MusicService musicService;

SongAdapter(Context c, ArrayList<Song> theSongs, MusicService ms ){
	context = c;
	songs=theSongs;
	songInf=LayoutInflater.from(c);
	musicService = ms;
}

@Override
public int getCount() {
	return songs.size();
}

@Override
public Object getItem(int arg0) {
	return null;
}

@Override
public long getItemId(int arg0) {
	return 0;
}

@Override
public View getView(int position, View convertView, ViewGroup parent) {
	//map to song layout
	LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.song, parent, false);
	//get title and artist views
	TextView songView = (TextView)songLay.findViewById(R.id.song_title);
	TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
	//get song using position
	Song currSong = songs.get(position);

	if( musicService.getSelectedSongNr() == position ) {
		songLay.setBackgroundColor( ContextCompat.getColor(context, R.color.colorAccent) );
	}
	//get title and artist strings
	songView.setText(currSong.getTitle());
	artistView.setText(currSong.getArtist());
	//set position as tag
	songLay.setTag(position);
	return songLay;
}
}// end Class
