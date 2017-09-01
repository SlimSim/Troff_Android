package slimsimapps.troff;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016-12-25, by Slim Sim Apps.
 */
class G {

private Context context;

G(Context context) {
	this.context = context;
}

@SuppressWarnings("unused")
private static final String TAG = "Global_G";


final void showKeyboard() {
	InputMethodManager imm = (InputMethodManager)
			context.getSystemService(Context.INPUT_METHOD_SERVICE);
	if(imm != null){
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
}

final void hideKeyboard(Window window) {
	window.setSoftInputMode(
		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
	);
}

final String getDisplayTime( long timeMillis ) {
	return String.format("%02d:%02d",
			TimeUnit.MILLISECONDS.toMinutes( timeMillis ),
			TimeUnit.MILLISECONDS.toSeconds( timeMillis ) -
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeMillis)));
}

final String getDetailedDisplayTime( long timeMillis ) {
	String time = getDisplayTime( timeMillis );
	long remainder = timeMillis % 1000;
	String millis = "" + remainder;
	if( remainder < 10 ) {
		millis = "00" + remainder;
	} else if( remainder < 100 ) {
		millis = "0" + remainder;
	}
	return time + "." + millis;
}

final long getTimeFromDetailedDisplay( String time ) {
	int length = time.length();
	if( length != 9 ) {
		Log.w(TAG, "getTimeFromDetailedDisplay, wrong length for input time");
		return -1;
	}
	try {
		long millis = Long.parseLong(time.substring(length - 3, length));
		long seconds = Long.parseLong(time.substring(length - 6, length - 4));
		long minutes = Long.parseLong(time.substring(length - 9, length - 7));
		return 60000 * minutes + 1000 * seconds + millis;
	} catch ( Exception e ) {
		Log.e( TAG, "getTimeFromDetailedDisplay, wrong format for time, e = " + e );
		return -1;
	}
}
}// end Class
