package com.slimsimapps.troff;

/**
 * Created on 2016-10-17, by Silm Sim
 */

public class Song {

    private static final String TAG = "Song";

    private long id;
    private String title;
    private String artist;

    public Song(long songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}

}
