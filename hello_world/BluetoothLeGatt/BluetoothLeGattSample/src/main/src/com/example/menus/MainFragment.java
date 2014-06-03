package com.example.menus;

//import android.R;

import com.example.android.bluetoothlegatt.R;
import com.example.itemlists.CreateListActivity;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


/**
 * A list fragment representing a list of Functions. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link FunctionDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MainFragment extends ListFragment {

	private final static String TAG =  MainFragment.class.getSimpleName();

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sListCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;
    
    private static ArrayAdapter<ListContent.ListEntry> adapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(int id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sListCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(TAG, "onCreate()");
    	
        super.onCreate(savedInstanceState);
        
    }
    
    // stuff
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView()");
    	
    	// Inflate the layout for this fragment
    	View v = inflater.inflate(R.layout.fragment_main, container, false);
    	
    	Log.d(TAG, "view is " + v.toString());
        
        ListView list = (ListView) (v.findViewById(android.R.id.list));
        
        Log.d(TAG, "list is " + list.toString());
        
         adapter = new ArrayAdapter<ListContent.ListEntry>(
                getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                ListContent.ITEMS);

        list.setAdapter(adapter);
        
        
        Log.d(TAG, "I also want to listen to a button now!");
        Button newList = (Button) v.findViewById(R.id.newList);
        
        OnClickListener l = new OnClickListener() {
        	public void onClick(View v) {
        		Log.d(TAG, "attempting to create new list");
               	Intent intent = new Intent(getActivity(), CreateListActivity.class);
               	intent.putExtra(CreateListActivity.EXTRAS_EDIT_MODE, false); 
        		startActivity(intent);
        	}
    	}; 

        newList.setOnClickListener(l); 
              
        return v;
 
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated()");

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }
    
    
    public static void updateAdapter(){
    	adapter.notifyDataSetChanged();
        Log.d(TAG, "Updated Adapter: " + ListContent.ITEMS.toString());
    }

    @Override
    public void onAttach(Activity activity) {
    	Log.d(TAG, "attaching fragment to activity");
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sListCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Log.d(TAG, "list " + position + " selected in list " + listView.toString());
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(position); //ListContent.ITEMS.get(position).id
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
}
