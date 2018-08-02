package slimsimapps.troff.Models;

import android.support.annotation.NonNull;

/**
* Created on 2016-10-21, by Slim Sim.
*/
public class IntBool {

@SuppressWarnings("unused")
private static final String TAG = "IntBool";

private int value;
private boolean active;

public IntBool() {
}

public IntBool( int value, boolean active ) {
	this.value = value;
	this.active = active;
}


public String toString() {
	return "{" +
			"value=" + value +
			", active=" + active +
			"}";
}

public void set(int val) {
	if( val != 0 ) {
		value = val;
		active = true;
	} else {
		active = false;
	}
}

public int get() {
	if( active ) {
		return value;
	} else {
		return 0;
	}
}

}// end Class
