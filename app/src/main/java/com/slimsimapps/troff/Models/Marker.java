package com.slimsimapps.troff.Models;

/**
 * Created on 2016-10-21, by Slim Sim.
 */
public class Marker {

    private int id, songId;
    private String name, info, color;
    private long time;

    public Marker() {
    }

    public Marker(String name, long time, int songId) {
        this.name = name;
        this.time = time;
        this.songId = songId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String toString() {
        return "{" + name +", id="+id+", songId="+songId+", time="+time + "}";
    }
}
