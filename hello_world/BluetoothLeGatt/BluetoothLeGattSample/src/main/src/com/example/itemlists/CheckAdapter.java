package com.example.itemlists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.android.bluetoothlegatt.R;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class CheckAdapter extends ArrayAdapter<CheckHolder> {

	final static String TAG = CheckAdapter.class.getSimpleName();
	private final ArrayList<CheckHolder> list;
	private final Activity context;

	public CheckAdapter(Activity context, ArrayList<CheckHolder> list) {
		super(context, R.layout.tag_display, list);
		this.context = context;
		this.list = list;
	}

	static class ViewHolder {
		protected TextView text;
		protected CheckBox checkbox;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			LayoutInflater inflator = context.getLayoutInflater();
			view = inflator.inflate(R.layout.tag_display, null);
			Log.d(TAG, "inlfated a checkbox");
			
			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.text = (TextView) view.findViewById(R.id.label);
			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.check);
			viewHolder.checkbox
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							CheckHolder element = (CheckHolder) viewHolder.checkbox
									.getTag();
							element.selected = buttonView.isChecked();
						}
					});
			Log.d(TAG, "filled the view holder and attached the click listener");
			view.setTag(viewHolder);
			viewHolder.checkbox.setTag(list.get(position));
		} else {
			Log.d(TAG, "view is already created");
			view = convertView;
			((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		holder.text.setText(list.get(position).getName());
		holder.checkbox.setChecked(list.get(position).selected);
		
		Log.d(TAG, "done creating the view.. return it now");
		return view;
	}
	
	public boolean hasEntry(CheckHolder e){
		for(CheckHolder c : list){
			if(c.getAddr().equals(e.getAddr())){
				return true;
			}
		}
		return false;
	}
	
	public void add(CheckHolder newentry){
		if(!hasEntry(newentry)){
			list.add(newentry);
			CreateListActivity.updateAdapter();
		}
	}
	
	public ArrayList<BluetoothDevice> getSelected(){
		ArrayList<BluetoothDevice> ret = new ArrayList<BluetoothDevice>();
		for(CheckHolder c : list){
			if(c.selected){
				ret.add(c.getDev());
			}
		}
		return (ret.isEmpty() ? null : ret);
	}

}