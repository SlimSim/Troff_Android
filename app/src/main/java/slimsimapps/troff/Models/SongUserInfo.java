package slimsimapps.troff.Models;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import slimsimapps.troff.Exceptions.NoMarkersException;

/**
* Created on 2016-10-21, by Slim Sim.
*/
public class SongUserInfo /*implements Comparable<Test>*/{


@SuppressWarnings("unused")
private static final String TAG = "SongUserInfo";


/**
 * According to Google Chrome Troff Json:
 */
private List<String> aStates;
private List<Boolean> abAreas;
private String currentStartMarker;
private String currentStopMarker;
private String info;
private long loopTimes;
private List<Marker> markers;
private IntBool pauseBefStart;
private long speed;
private IntBool startBefore;
private IntBool stopAfter;
private String tempo;
private long volume;
private IntBool wait;
private Long zoomEndTime;
private Long zoomStartTime;
//private int nrPlayed; // TODO: implement :)


SongUserInfo() {
	this.aStates = new ArrayList<>();
	this.abAreas = new ArrayList<>();
	this.loopTimes = 1;
	this.markers = new ArrayList<>();
	this.pauseBefStart = new IntBool(3, true);
	this.speed = 100;
	this.startBefore = new IntBool(3, false);
	this.stopAfter = new IntBool(2, false);
	this.tempo = "?";
	this.volume = 100;
	this.wait = new IntBool(1, true);
	this.zoomEndTime = null;
	this.zoomStartTime = 0L;
}

public String getInfo() {
	return info;
}

public void setInfo(String info) {
	this.info = info;
}

public String toString(){
	return TAG + " {" +
			"\ninfo = " + info +
			//"\nmarkers = " + markers +
			"\ncurrentStartMarker = " + currentStartMarker +
			"\ncurrentStopMarker = " + currentStopMarker +
			//"\npauseBefStart = " + pauseBefStart +
			//"\nspeed = " + speed +
			//"\nstartBefore = " + startBefore +
			//"\nstopAfter = " + stopAfter +
	"}";
}

public List<Marker> getMarkers() {
	return markers;
}

private String getNewId(){
	String markerPrefix = "markerNr";
	int nr = 0;
	while( markersContains( markerPrefix+nr ) ) {
		nr++;
	}
	return markerPrefix + nr;
}

private boolean markersContains( String id ) {
	for( int i = 0; i < markers.size(); i++ ) {
		if( markers.get( i ).getId().equals( id ) ) {
			return true;
		}
	}
	return false;
}

public Marker addMarker(String name, long time ) {
	String id = getNewId();
	Marker m = new Marker(id, name, time, "", "None");
	markers.add(m);
	return m;
}

public void updateMarker( Marker marker ) {
	int index = markers.indexOf( marker );
	markers.set(index, marker);
}

public void removeMarker( Marker marker ) {
	markers.remove(marker);
}

public boolean hasMarkers() {
	return markers != null && markers.size() > 0;
}

public Long getZoomStartTime() {
	return zoomStartTime;
}

public void setZoomStartTime(Long zoomStartTime) {
	this.zoomStartTime = zoomStartTime;
}

public Long getZoomEndTime() {
	return zoomEndTime;
}

public void setZoomEndTime(Long zoomEndTime) {
	this.zoomEndTime = zoomEndTime;
}

public int getPauseBefStart() {
	return pauseBefStart.get();
}

public void setPauseBefStart(int pauseBefStart) {
	this.pauseBefStart.set( pauseBefStart );

}

public long getSpeed() {
	return speed;
}

public void setSpeed(long speed) {
	this.speed = speed;
}

public int getStartBefore() {
	return startBefore.get();
}

public void setStartBefore( int startBefore ) {
	this.startBefore.set( startBefore );
}

public int getStopAfter() {
	return stopAfter.get();
}

public void setStopAfter( int stopAfter ) {
	this.stopAfter.set( stopAfter );
}

public String getTempo() {
	return tempo;
}

public void setTempo(String tempo) {
	this.tempo = tempo;
}

public long getVolume() {
	return volume;
}

public void setVolume(long volume) {
	this.volume = volume;
}

public int getWait() {
	return wait.get();
}

public void setWait( int wait ) {
	this.wait.set( wait );
}

public Marker getCurrentStartMarker() {
	if( markers == null || markers.size() == 0 ) {
		throw new NoMarkersException( "SongUserInfo / getCurrentStartMarker: no markers");
	}
	for( int i = 0; i < markers.size(); i++ ) {
		if( markers.get( i ).getId().equals( currentStartMarker ) ) {
			return markers.get( i );
		}
	}
	return markers.get( 0 );
}

public void setCurrentStartMarker( Marker currentStartMarker ) {
	this.currentStartMarker = currentStartMarker.getId();
}

public Marker getCurrentStopMarker() {
	if( markers == null || markers.size() == 0 ) {
		throw new NoMarkersException( "SongUserInfo / getCurrentStartMarker: no markers");
	}
	for( int i = 0; i < markers.size(); i++ ) {
		if( markers.get( i ).getId().equals(currentStopMarker) ) {
			return markers.get( i );
		}
	}
	return markers.get( markers.size() - 1 );
}

public void setCurrentStopMarker(Marker currentStopMarker) {
	this.currentStopMarker = currentStopMarker.getId();
}

public List<String> getaStates() {
	return aStates;
}

public void setaStates(List<String> aStates) {
	this.aStates = aStates;
}

public List<Boolean> getAbAreas() {
	return abAreas;
}

public void setAbAreas(List<Boolean> abAreas) {
	this.abAreas = abAreas;
}

public int getLoopTimes() {
	return (int) loopTimes;
}

public void setLoopTimes(int loopTimes) {
	this.loopTimes = (long) loopTimes;
}

public int getMarkerIndex( Marker m ) {
	Collections.sort( getMarkers() );
	return getMarkers().indexOf( m );
}
}// end Class
