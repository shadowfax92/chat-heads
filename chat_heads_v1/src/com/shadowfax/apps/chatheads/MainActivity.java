package com.shadowfax.apps.chatheads;

import wei.mark.standout.StandOutWindow;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button new_instance_button;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        //TODO remove below line.
        StandOutWindow.closeAll(this, MessageBoxWindow.class);
        StandOutWindow.closeAll(this, ChatHeadWindow.class);
        
        new_instance_button=(Button)findViewById(R.id.button1);
        new_instance_button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				createNewMultiWindow();
			}
		});
        
//    	StandOutWindow.closeAll(this, SimpleWindow.class);
////		// show a MultiWindow, SimpleWindow
//		StandOutWindow.show(this, SimpleWindow.class, StandOutWindow.DEFAULT_ID);
		//finish();
        //StandOutWindow.showNew(this, MultiWindow.class);
		
    }

    public void createNewMultiWindow()
    {
    	//StandOutWindow.showNew(this,SimpleWindow.class);
    	StandOutWindow.showNew(this, ChatHeadWindow.class);
    	//StandOutWindow.showNew(this, MessageBoxWindow.class);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }
}
