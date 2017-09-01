package slimsimapps.troff.Models;

/**
* Created on 2017-08-04 by slim sim.
*/
public class Setting {
@SuppressWarnings("unused")
private static final String TAG = "Marker";

private int id;
private String name;
private long value;
public static final String CURRENT_SONG = "current_song";

public Setting() {
}

public Setting(String name, long value) {
	this.name = name;
	this.value = value;
}

public int getId() {
	return id;
}

public void setId(int id) {
	this.id = id;
}

public String getName() {
	return name;
}

public void setName(String name) {
	this.name = name;
}

public long getValue() {
	return value;
}

public void setValue(long value) {
	this.value = value;
}
}// end Class
