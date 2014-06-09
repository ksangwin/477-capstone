package com.example.menus;

import com.example.android.bluetoothlegatt.*;
import com.example.itemlists.*;
import com.example.itemlists.ItemList.Tag;

//import android.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;



/**
 * An activity representing a list of Functions. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link FunctionDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FunctionListFragment} and the item details
 * (if present) is a {@link FunctionDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link FunctionListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MainActivity extends Activity
        implements MainFragment.Callbacks {

    private final static String TAG = MainActivity.class.getSimpleName();


    public static ListContainer allLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /*
        // getting crap to do fragment stuff
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        
        // adding the fragment to this activity
        fragmentTransaction.add(R.layout.activity_main, new MainFragment());
        fragmentTransaction.commit();
        */
        allLists = new ListContainer();
        Log.d(TAG, "adding Show All as initial list..." );
        if(!allLists.containsList("Show All")){
        	Log.d(TAG, "adding show all");
        	// don't add duplicates
        	allLists.addList("Show All", null);        	
        }
        
        Log.d(TAG, "ShowAll: " + allLists.getList("Show All").dbgString());

        Log.d(TAG, "finished onCreate()");

    }

    /**
     * Callback method from {@link FunctionListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(int id) {
    	
    	Log.d(TAG, "onItemSelected(" + id + ")");

       	String selectedList = ListContent.ITEMS.get(id).name;
       	
       	Log.d(TAG, "attempting to scan " + selectedList);
       	
       	Intent intent = new Intent(this, DeviceScanActivity.class);
       	
       	intent.putExtra(DeviceScanActivity.EXTRAS_LIST, selectedList); 
       			
		startActivity(intent);
    }
    
    
}
