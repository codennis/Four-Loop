package com.example.four_loop;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Keyboard button
        Button launchKeyboard = (Button) findViewById(R.id.keyboard);
        launchKeyboard.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(v.getContext(), KeyboardPage.class);
        		startActivityForResult(intent, 0);
        	}
        });
        
        //Media button
        Button launchMedia = (Button) findViewById(R.id.media);
        launchMedia.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		Intent intent = new Intent(v.getContext(), MediaPage.class);
        		startActivityForResult(intent, 0);
        	}
        });
    }

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
