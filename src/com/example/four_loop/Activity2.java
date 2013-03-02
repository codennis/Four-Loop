package com.example.four_loop;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class Activity2 extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity2);
		
		// Elements obtained by id from XML layout
		ListView myListView = (ListView)findViewById(R.id.myListView);
		final EditText myEditText = (EditText)findViewById(R.id.myEditText);
		
		final ArrayList<String> noteList = new ArrayList<String>();
		// Adapter - Class that binds data to view
		final ArrayAdapter<String> aa;
		
		aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, noteList);
		
		myListView.setAdapter(aa);
		Button addButton = (Button)findViewById(R.id.addButton);
		addButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				noteList.add(0, myEditText.getText().toString());
				aa.notifyDataSetChanged();
				myEditText.setText("");
			}
		});
		
	}
}
