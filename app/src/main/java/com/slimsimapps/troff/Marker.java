package com.slimsimapps.troff;

/**
 * Created by simon on 2016-10-21.
 */
public class Marker {

    private long id;
    private String title;
    private long time;

    public Marker(long id, String title, long time) {
        this.id = id;
        this.title = title;
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
