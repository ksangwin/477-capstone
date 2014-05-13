package com.example.menus;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.R;


/**
 * A fragment representing a single Function detail screen.
 * This fragment is either contained in a {@link FunctionListActivity}
 * in two-pane mode (on tablets) or a {@link FunctionDetailActivity}
 * on handsets.
 */
public class FunctionDetailFragment extends Fragment {
	private final static String TAG = FunctionDetailActivity.class.getSimpleName();
	
	/**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private ListContent.ListItem mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FunctionDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
    	super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = ListContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_function_detail, container, false);

        // TODO: choose the correct view (not TextView) depending on what was selected
        if (mItem != null) {
            ((TextView) rootView.findViewById(R.id.function_detail)).setText(mItem.content);
        }

        return rootView;
    }

}
