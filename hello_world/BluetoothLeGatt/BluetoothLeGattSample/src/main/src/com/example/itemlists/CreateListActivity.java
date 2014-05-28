package com.example.itemlists;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.bluetoothlegatt.*;
import com.example.itemlists.ItemList.Tag;
import com.example.menus.MainActivity;
import com.example.menus.MainFragment;

public class CreateListActivity extends Activity implements Callback {

	private final static String TAG = CreateListActivity.class.getSimpleName();
	public final static String EXTRAS_EDIT_MODE = "EDIT MODE";
	
	private boolean editMode; //TODO: set this properly
	private Button finishList;
	private EditText listname;
	private String listname_s;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		intent.getBooleanExtra(EXTRAS_EDIT_MODE, editMode);
		
		setContentView(R.layout.create_item_list_activity);
		getActionBar().setTitle(R.string.new_list_title);
		listname = ((EditText)findViewById(R.id.ListNameInput));
		
		finishList = ((Button)findViewById(R.id.saveButton));
		
		finishList.setOnClickListener(finishNewList);
		
	}

	View.OnClickListener finishNewList = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "Finish the list now!");
			listname_s = listname.getText().toString();
			MainActivity.allLists.addList(listname_s, null);
			finish();
		}
	};
	/*
	View.OnClickListener addToList = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Log.d(TAG, "making a list with content...");
			listname_s = listname.getText().toString(); 
			// note: ItemLists are always created empty.
			MainActivity.allLists.addList(listname_s, new ItemList(listname_s));
			Log.d(TAG, "Time to scan! Except differently!");
			
			//TODO: start a new activity
			Intent intent = new Intent(getContext(), DeviceScanActivity.class);
			intent.putExtra("EXTRAS_LIST", listname_s);
			startActivity(intent);
		}
	};
*/

	@Override
	public boolean handleMessage(Message arg0) {
		return false;
	}
	
	public Context getContext() {
	    return (Context)this;
	} 
	
	   // Adapter for holding devices found through scanning.
    private class AddListAdapter extends BaseAdapter {
        private ArrayList<Tag> allTags;
        private LayoutInflater mInflator;

        public AddListAdapter() {
            super();
            allTags = new ArrayList<Tag>();
            mInflator = CreateListActivity.this.getLayoutInflater();
        }

        public void addTag(Tag t) {
            if(!allTags.contains(t)) {
                allTags.add(t);
            }
        }

        public Tag getDevice(int position) {
            return allTags.get(position);
        }

        public void clear() {
            allTags.clear();
        }

        @Override
        public int getCount() {
            return allTags.size();
        }

        @Override
        public Object getItem(int i) {
            return allTags.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            // first display bound tags using universal tag list in ListContainer
            // then display any unbound tags that we've picked up in a scan?
            
            if (view == null) {
                view = mInflator.inflate(R.layout.tag_display, null);

                // TODO: figure out what the fuck is happening here
                CheckBox ck = (CheckBox) view.findViewById(R.id.checkEntry);
                
                viewHolder = new ViewHolder();
                viewHolder.entry = ck;
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            Tag tag = allTags.get(i);
            final String tagName = tag.nickname;
            
            //TODO: can do some filtering here?
            if (tagName != null && tagName.length() > 0)
                viewHolder.entry.setText(tagName);
            else
                viewHolder.entry.setText(R.string.unknown_device);

            return view;
        }
    }
    
    static class ViewHolder {
        CheckBox entry;
    }

}