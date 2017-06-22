package slimsimapps.troff;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.sax.RootElement;
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

import slimsimapps.troff.Models.Song;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class SettingsFragment extends Fragment {

	/**
	 * TODO: det är bra att skicka information till fragmentet när
	 * man skapar upp det (och behöver man byta information, tex när
	 * låten ändras, så tar man bara bort det gamla fragmentet och
	 * ersätter med det nya)
	 * och för att skicka tillbaka information så använd
	 * OnFragmentInteractionListener längst ner.
	 * alltså INTE "musicSrv.setWaitBetween( wait );" och liknande :(
	 */

	@SuppressWarnings("unused")
	private static final String TAG = "SettingsFragment";

	private View rootView; // TODO: ask me, ask Rasmus: är detta ok
	// best practice? att använda en rootView för att kunna använda
	// findViewById() ?

	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	private static MusicService musicSrv;

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;

	private OnFragmentInteractionListener mListener;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
//	 * @param param1 Parameter 1.
//	 * @param param2 Parameter 2.
	 * @return A new instance of fragment SettingsFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static SettingsFragment newInstance( MusicService
													musicService ) {
		SettingsFragment fragment = new SettingsFragment();
		Bundle args = new Bundle();
		String param1 = "stuff", param2 = "staff"; //should come form
		// calling method....
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		Log.v(TAG, "musicService = " + musicService);
		musicSrv = musicService;
		return fragment;
	}
	public SettingsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);
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
		// Inflate the layout for this fragment

		View view = inflater.inflate(R.layout.fragment_settings,
				container, false);

		rootView = view;

//		LinearLayout loopParent =
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
						onWaitChange( editable );
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

		return view;
	}

	private void onWaitChange( Editable editable ) {
		int wait = 0;
		if( !editable.toString().equals("") )
			wait = Integer.parseInt( editable.toString() );
		Log.v(TAG, "wait = " + wait);
		musicSrv.setWaitBetween( wait );
	}

	private void onLoopClick(View view) {
		Log.v(TAG, "onLoopClick ->");
		int nrLoops = Integer.parseInt((String) view.getTag());

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
		Log.v(TAG, "onLoopClick: nrLoops = " + nrLoops);
		musicSrv.setLoop( nrLoops );
	}


	@Override
	public void onAttach(Context context) {
		Log.v(TAG, "onAttach getView = " + getView());
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This method is run when a song is picked,
	 * it should load all the settings from the DB and apply them.
	 * @param song
	 */
	public void onLoadedSong( Song song ) {
		// todo: fix so that all the settings are saved in the DB and
		// applyed when a song is selected.
		Log.v( TAG, "onLoadedSong ->" );

		// set the loopsetting to the "saved" value:
		onLoopClick( rootView.findViewById( R.id.loop1 ) );

		// set the wait between to the "saved" value:
		((EditText) rootView.findViewById(
				R.id.settingsWaitBetween)).setText( "1" );


		// load the wait between value to the musicService:
		onWaitChange(((EditText) rootView.findViewById(
				R.id.settingsWaitBetween)).getText());
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
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}
}
