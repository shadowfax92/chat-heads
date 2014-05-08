package com.shadowfax.apps.chatheads;

import wei.mark.standout.StandOutWindow;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	Button enableChatHeadsBtn, disableChatHeadsBtn, rateAppButton;
	TextView chatHeadsStateTextView;

	PreferenceOperations prefOps;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		prefOps = new PreferenceOperations(getApplicationContext());

		enableChatHeadsBtn = (Button) findViewById(R.id.main_enable_chat_heads_btn);
		disableChatHeadsBtn = (Button) findViewById(R.id.main_disable_chat_heads_button);
		rateAppButton = (Button) findViewById(R.id.main_rate_app_button);
		chatHeadsStateTextView = (TextView) findViewById(R.id.chat_heads_sate);

		refresh_chat_heads_state();

		enableChatHeadsBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				prefOps.storeCleverStatePref(true);
				refresh_chat_heads_state();
			}
		});
		disableChatHeadsBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				prefOps.storeCleverStatePref(false);
				handleDisableOfChatHeads();
				refresh_chat_heads_state();
			}
		});

		rateAppButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				AppRater.showRateDialog(MainActivity.this, null);

			}
		});

		// prompting users to rate
		AppRater.app_launched(this);
	}

	private void refresh_chat_heads_state() {
		boolean chat_heads_sate = prefOps.getChatHeadsStatePref();
		if (chat_heads_sate == true) {
			chatHeadsStateTextView
					.setText("Currently, Chat Heads are ENABLED!");
		} else {
			chatHeadsStateTextView
					.setText("Currently, Chat Heads are DISABLED!");
		}
	}

	private void handleDisableOfChatHeads() {
		StandOutWindow.closeAll(this, MessageBoxWindow.class);
		StandOutWindow.closeAll(this, ChatHeadWindow.class);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}
}
