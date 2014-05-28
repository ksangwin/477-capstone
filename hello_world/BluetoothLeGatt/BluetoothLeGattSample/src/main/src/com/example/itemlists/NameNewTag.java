package com.example.itemlists;

import com.example.android.bluetoothlegatt.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NameNewTag extends Activity implements Callback {
	
	public String tagName;
	private Button accept;
	private EditText nameInput;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.name_new_tag);

		nameInput = ((EditText)findViewById(R.id.tagName));
		
		accept = ((Button)findViewById(R.id.acceptName));
		
		accept.setOnClickListener(enterNewName);

	}
	
	View.OnClickListener enterNewName = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			//Log.d(TAG, "making an empty list...");
			tagName = nameInput.getText().toString();
			
			Intent returnIntent = new Intent();
			returnIntent.putExtra("result", tagName); 
			setResult(RESULT_OK,returnIntent);
			finish(); 
		}
	};

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		return false;
	}


}
