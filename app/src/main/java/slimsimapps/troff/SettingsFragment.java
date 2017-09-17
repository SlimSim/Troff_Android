package slimsimapps.troff;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SettingsFragment extends Fragment {

@SuppressWarnings("unused")
private static final String TAG = "SettingsFragment";

private static final String START_BEFORE = "startBefore";
private static final String STOP_AFTER = "stopAfter";
private static final String PAUSE_BEFORE = "pauseBefore";
private static final String WAIT_BETWEEN = "waitBetween";
private static final String NR_LOOPS = "nrLoops";

private int startBefore;
private int stopAfter;
private int pauseBefore;
private int waitBetween;
private int nrLoops;

private SettingsListener callOut;

/**
 * Use this factory method to create a new instance of
 * this fragment using the provided parameters.
 *
 * @param startBefore Parameter 1.
 * @param stopAfter Parameter 2.
 * @param pauseBefore Parameter 3.
 * @param waitBetween Parameter 4.
 * @param nrLoops Parameter 5.
 * @return A new instance of fragment SettingsFragment.
 */
public static SettingsFragment newInstance(
		int startBefore,
		int stopAfter,
		int pauseBefore,
		int waitBetween,
		int nrLoops
) {
	SettingsFragment fragment = new SettingsFragment();
	Bundle args = new Bundle();

	args.putInt(START_BEFORE, startBefore);
	args.putInt(STOP_AFTER, stopAfter);
	args.putInt(PAUSE_BEFORE, pauseBefore);
	args.putInt(WAIT_BETWEEN, waitBetween);
	args.putInt(NR_LOOPS, nrLoops);
	fragment.setArguments(args);
	return fragment;
}
public SettingsFragment() {
	// Required empty public constructor
}

@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	if (getArguments() != null) {
		startBefore = getArguments().getInt(START_BEFORE);
		stopAfter = getArguments().getInt(STOP_AFTER);
		pauseBefore = getArguments().getInt(PAUSE_BEFORE);
		waitBetween = getArguments().getInt(WAIT_BETWEEN);
		nrLoops = getArguments().getInt(NR_LOOPS);
	}

}

@Override
public void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	// körs när fragmentet tas ner
	outState.putString("myKey", "This is my best fragment");
}

@Override
public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
	super.onViewStateRestored(savedInstanceState);
	// körs när fragmentet återuppstår :)
	if( savedInstanceState == null ) return;
	Log.v(TAG, "onViewStateRestored: myKey = " +
			savedInstanceState.getString("myKey"));

}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
						 Bundle savedInstanceState) {

	final View view = inflater.inflate(R.layout.fragment_settings,
			container, false);

	((EditText) view.findViewById(R.id.settingsStartBefore))
			.setText("" + (startBefore / 1000) );
	((EditText) view.findViewById(R.id.settingsStopAfter))
			.setText("" + (stopAfter / 1000) );
	((EditText) view.findViewById(R.id.settingsPauseBefore))
			.setText( "" + pauseBefore );
	((EditText) view.findViewById(R.id.settingsWaitBetween))
			.setText( "" + waitBetween );

	final View loopBut = view.findViewWithTag( "" + nrLoops );

	view.post(new Runnable() {
		@Override
		public void run() {
			if( loopBut != null ) {
				onLoopClick( loopBut );
			} else {
				onLoopClick( view.findViewById( R.id.loop1 ) );
			}


			((EditText) view.findViewById(
					R.id.settingsStartBefore)).addTextChangedListener(
					new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void afterTextChanged(Editable editable) {
							int startBefore = getIntFromEditable( editable );
							callOut.onStartBeforeChange( startBefore * 1000 );
						}
					}
			);
			((EditText) view.findViewById(
					R.id.settingsStopAfter)).addTextChangedListener(
					new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void afterTextChanged(Editable editable) {
							int stopAfter = getIntFromEditable( editable );
							callOut.onStopAfterChange( stopAfter * 1000 );
						}
					}
			);
			((EditText) view.findViewById(
					R.id.settingsPauseBefore)).addTextChangedListener(
					new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void afterTextChanged(Editable editable) {
							int pause = getIntFromEditable( editable );
							callOut.onPauseBeforeChange( pause );
						}
					}
			);
			((EditText) view.findViewById(
					R.id.settingsWaitBetween)).addTextChangedListener(
					new TextWatcher() {
						@Override
						public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
						}

						@Override
						public void afterTextChanged(Editable editable) {
							int wait = getIntFromEditable( editable );
							callOut.onWaitChange( wait );
						}
					}

			);
			LinearLayout loopParent = (LinearLayout) view.findViewById(
					R.id.settingsLoopParent);
			for( int i = 0; i < loopParent.getChildCount(); i++ ) {
				LinearLayout ll = (LinearLayout)loopParent.getChildAt(i);
				for( int j = 0; j < ll.getChildCount(); j++ ) {
					ll.getChildAt(j).setOnClickListener(
							new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									onLoopClick(view);
								}
							}
					);
				}
			}
		}
	});


	return view;
}

private int getIntFromEditable( Editable editable ) {
	int returnValue;
	try {
		returnValue = Integer.parseInt( editable.toString() );
	} catch( NumberFormatException nfe ) {
		Log.w(TAG, "getIntFromEditable: NumberFormatException for \"" +
				editable.toString() + "\"");
		returnValue = 0;
	}
	return returnValue;
}

private void onLoopClick(View view) {
	nrLoops = Integer.parseInt((String) view.getTag());

	LinearLayout parent = (LinearLayout) view.getParent().getParent();
	for( int i = 0; i < parent.getChildCount(); i++ ) {
		LinearLayout ll = (LinearLayout) parent.getChildAt( i );
		for( int j = 0; j < ll.getChildCount(); j++ ) {
			ll.getChildAt(j).setBackgroundColor(
					Color.TRANSPARENT
			);
		}
	}
	view.setBackgroundColor( ContextCompat.getColor(
			getContext(),
			R.color.colorAccent
	));
	callOut.onLoopChange( nrLoops );
}


@Override
public void onAttach(Context context) {
	super.onAttach(context);
	if (context instanceof SettingsListener) {
		callOut = (SettingsListener) context;
	} else {
		throw new RuntimeException(context.toString()
				+ " must implement SettingsListener");
	}
}

@Override
public void onDetach() {
	super.onDetach();
	callOut = null;
}

/**
 * This interface must be implemented by activities that contain this
 * fragment to allow an interaction in this fragment to be communicated
 * to the activity and potentially other fragments contained in that
 * activity.
 * <p>
 * See the Android Training lesson <a href=
 * "http://developer.android.com/training/basics/fragments/communicating.html"
 * >Communicating with Other Fragments</a> for more information.
 */
interface SettingsListener {
	void onLoopChange( int nrLoops );
	void onStartBeforeChange( int startBefore );
	void onStopAfterChange( int stopAfter );
	void onPauseBeforeChange( int pause );
	void onWaitChange( int wait );
}
}// end Class
