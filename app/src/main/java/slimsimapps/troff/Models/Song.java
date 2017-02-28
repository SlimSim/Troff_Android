package slimsimapps.troff.Models;

/**
 * Created on 2016-10-17, by Silm Sim
 */

public class Song {

//    private static final String TAG = "Song";

    private int id = -1;
    private long fileId;
    private String title;
    private String artist;
    private int nrPlayed;
    private String info;
    private int tempo;

    public Song() {}



    public void setId(int id) {
        this.id = id;
    }
    public int getId(){return id;}
    public long getFileId() {
        return fileId;
    }
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public int getNrPlayed() {
        return nrPlayed;
    }
    public void setNrPlayed(int nrPlayed) {
        this.nrPlayed = nrPlayed;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void incrementNrPlayed() {
        nrPlayed++;
    }

    public String toString(){
        return "{"+title+" by "+artist+", id="+ id +", fileId="+fileId+", nrPlayed="+nrPlayed+"}";
    }

}
