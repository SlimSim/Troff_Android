package slimsimapps.troff.Models;

import android.support.annotation.NonNull;

/**
* Created on 2016-10-21, by Slim Sim.
*/
public class Marker implements Comparable<Marker>{

@SuppressWarnings("unused")
private static final String TAG = "Marker";

private String id, name, info, color;
private double time;

public Marker(String id, String name, long time, String info, String color) {
	this.id = id;
	this.name = name;
	setTime( time );
	this.info = info;
	this.color = color;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}


private static String getDisplayTime(long totalMillis) {
	long totalSecs = totalMillis/1000,
			hours, minutes, seconds;

	hours = totalSecs / 3600;
	minutes = (totalSecs % 3600) / 60;
	seconds = totalSecs % 60;

	String h, m, s;
	h = (hours>0?""+hours+":":"");
	m = (minutes<10?"0":"") + minutes + ":";
	s = (seconds<10?"0":"") + seconds;

	return h + m + s;
}

public String getDisplayTime() {
	return getDisplayTime( getTime() );
}

public long getTime() {
	return (long) time * 1000;
}

public void setTime(long time) {
	this.time = ((double) time) / 1000;
}

public String getId() {
	return id;
}

public void setId(String id) {
	this.id = id;
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
	return "{" +
			name +
			", id=" + id +
			", time=" + time +
			//", info=" + info +
			//", color=" + color +
			"}";
}

@Override
public int compareTo(@NonNull Marker marker) {
	return (int) ( this.getTime() - marker.getTime() );
}

@Override
public boolean equals(Object obj) {
	if (this == obj)
		return true;
	if (obj == null)
		return false;
	if (getClass() != obj.getClass())
		return false;
	Marker other = (Marker) obj;
	return id.equals(other.id);
}
}// end Class
