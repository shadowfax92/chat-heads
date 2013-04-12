package com.shadowfax.apps.chatheads;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MessageBoxWindow extends StandOutWindow {
	public View senderRowView, userRowView;
	public TextView senderRowTitle, senderRowDescrip, userRowTitle,
			userRowDescrip;

	public int typeOfMessage[];

	public static final int INFORM_CHAT_HEAD_CLASS_MSG_WINDOW_HIDDEN_INTENT = 2001;

	Long senderId;
	String senderDisplayName;
	String senderNumber;
	int messageThreadId;
	String messageBody;
	int parentChatHeadId;

	ListView smsThreadList;
	TextView smsThreadListHeader;
	int numMessagesInInbox;
	EditText smsThreadListSendMessage;
	Button smsThreadListSendButton;

	// database part
	private Cursor myCursor;

	@Override
	public String getAppName() {
		return "MessageBoxWindow";
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_add;
	}

	@Override
	public String getTitle(int id) {
		return getAppName() + " " + id;
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		// create a new layout from body.xml
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.sms_thread_list, frame, true);

		smsThreadList = (ListView) view.findViewById(R.id.sms_thread_listview);
		smsThreadListHeader = (TextView) view
				.findViewById(R.id.sms_thread_list_header);
		smsThreadListSendButton = (Button) view
				.findViewById(R.id.sms_thread_list_send_button);
		smsThreadListSendMessage = (EditText) view
				.findViewById(R.id.sms_thread_list_send_message);

		senderRowView = null;
		senderRowTitle = null;
		senderRowDescrip = null;
		userRowView = null;
		userRowTitle = null;
		userRowDescrip = null;

		smsThreadListSendButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View arg0) {
				String send_message = smsThreadListSendMessage.getText()
						.toString();
				if (send_message.toString().trim() != "") {
					SmsManager sms = SmsManager.getDefault();
					sms.sendTextMessage(senderNumber, null, send_message, null,
							null);
					ContentValues values = new ContentValues();
					values.put("address", senderNumber);
					values.put("body", send_message);
					getContentResolver().insert(
							Uri.parse("content://sms/sent"), values);
					fillSMSThreadList();
					smsThreadListSendMessage.setText("");
				}
			}
		});
		fillSMSThreadList();

		final int id_for_close = id;

	}

	public boolean onTouchBody(int id, Window window, View view,
			MotionEvent event) {
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_OUTSIDE:
			this.hide(id);
			String hideIntentString = "";
			Bundle data = new Bundle();
			data.putString("hideWindow", hideIntentString);
			// int
			// parent_chat_head_window_id=IncomingSmsBroadcastReceiver.chatBoxOpenedMap.get(senderNumber);
			// Toast.makeText(this,
			// "parent chat id="+parent_chat_head_window_id,
			// Toast.LENGTH_SHORT).show();
			sendData(id, ChatHeadWindow.class, parentChatHeadId,
					INFORM_CHAT_HEAD_CLASS_MSG_WINDOW_HIDDEN_INTENT, data);
			break;

		}
		return false;
	}

	// //////////////SMSThread functions/////////////////////////////

	private void fillSMSThreadList() {
		if (messageThreadId != -1) {
			fillUsingSMSConversation();
		} else {
			// TODO This is just a temporary solution.
			fillUsingSingleSMS();
		}
	}

	private void fillUsingSMSConversation() {
		Uri uriSMSURI = Uri.parse("content://sms/");
		// content://mms-sms/conversations/
		// Uri uriSMSURI = Uri.parse("content://mms-sms/conversations/");
		String SORT_ORDER = " _id ASC";
		Cursor cur = getContentResolver().query(
				uriSMSURI,
				new String[] { "_id", "thread_id", "address", "person", "date",
						"body", "type" }, " thread_id = " + messageThreadId,
				null, "DATE asc");

		// Cursor temp_cur = getContentResolver().query(uriSMSURI,
		// new String[] { "_id", "thread_id", "type" },
		// " thread_id = " + messageThreadId, null, "DATE asc");
		// if (temp_cur != null && temp_cur.getCount() != 0) {
		// temp_cur.moveToFirst();
		// }
		// int i = 0;
		// typeOfMessage = new int[cur.getCount()];
		// do {
		// typeOfMessage[i] = temp_cur.getInt(temp_cur.getColumnIndex("type"));
		// i++;
		//
		// } while (temp_cur.moveToNext());

		// if (cur != null && cur.getCount() != 0) {
		// cur.moveToFirst();
		// }
		// int i = 0;
		// typeOfMessage=new int[cur.getCount()];
		// do {
		// typeOfMessage[i] = cur.getInt(cur.getColumnIndex("type"));
		// i++;
		//
		// } while (cur.moveToNext());

		// Cursor cur = getContentResolver().query(uriSMSURI, null, null, null,
		// null);
		numMessagesInInbox = cur.getCount();
		smsThreadListHeader.setText(String.valueOf(numMessagesInInbox) + "/"
				+ String.valueOf(numMessagesInInbox));
		if (cur != null && cur.getCount() != 0) {
			cur.moveToFirst();
		}

		myCursor = cur;
		// startManagingCursor(cur);

		String[] from = new String[] { "body" };

		int[] to = new int[] { R.id.sms_thread_list_row_descrip };
		smsThreadList.setAdapter(new SMSThreadListAdapter(from, to, cur));
		smsThreadList.setSelection(smsThreadList.getAdapter().getCount() - 1);
	}

	private void fillUsingSingleSMS() {
		// TODO This is just a temporary solution.
		String temp[] = new String[1];
		temp[0] = messageBody;
		smsThreadList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.sms_thread_list_sender_row_new,
				R.id.sms_thread_list_row_descrip, temp));

	}

	class SMSThreadListAdapter extends SimpleCursorAdapter {
		TextView title, descrip, date;
		Cursor cursor;

		public SMSThreadListAdapter(String from[], int to[], Cursor cursor) {
			super(MessageBoxWindow.this, R.layout.sms_thread_list_row, cursor,
					from, to);
			this.cursor = cursor;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = null;
			row = convertView;
			cursor.moveToPosition(position);
			LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

			// if (row == null) {
			// LayoutInflater inflater = getLayoutInflater();
			// row = inflater.inflate(R.layout.sms_thread_list_row, parent,
			// false);
			// }

			int type_value = cursor.getInt(cursor.getColumnIndex("type"));
			// int type_value = typeOfMessage[position];
			if (type_value == 1) {
				// Sender's messages
				if (senderRowView == null) {
					row = inflater.inflate(
							R.layout.sms_thread_list_sender_row_new, parent,
							false);
				} else
					row = senderRowView;

			} else if (type_value == 2) {
				// User's messages
				if (userRowView == null) {
					row = inflater.inflate(
							R.layout.sms_thread_list_user_row_new, parent,
							false);
				} else
					row = userRowView;
			} else {
				// Ignore
			}

			// if(type_value==1 && senderRowTitle==null)
			// {
			// title = (TextView)
			// row.findViewById(R.id.sms_thread_list_row_title);
			// descrip = (TextView) row
			// .findViewById(R.id.sms_thread_list_row_descrip);
			// senderRowTitle=title;
			// senderRowDescrip=descrip;
			// }
			// else if(type_value==1 && senderRowTitle!=null)
			// {
			// title=senderRowTitle;
			// descrip=senderRowDescrip;
			// }
			// else if(type_value==2 && userRowTitle==null)
			// {
			// title = (TextView)
			// row.findViewById(R.id.sms_thread_list_row_title);
			// descrip = (TextView) row
			// .findViewById(R.id.sms_thread_list_row_descrip);
			// userRowTitle=title;
			// userRowDescrip=descrip;
			// }
			// else if(type_value==2 && userRowTitle!=null)
			// {
			// title=userRowTitle;
			// descrip=userRowDescrip;
			// }

			title = (TextView) row.findViewById(R.id.sms_thread_list_row_title);
			descrip = (TextView) row
					.findViewById(R.id.sms_thread_list_row_descrip);
			// date = (TextView)
			// row.findViewById(R.id.sms_thread_list_row_date);

			cursor.moveToPosition(position);

			// int date_index=cursor.getColumnIndex("date"); long
			// date_value=cursor.getLong(date_index); Date dateFromSms = new
			// Date(date_value); Format formatter = new
			// SimpleDateFormat("HH:mm:ss, yyyy-MM-dd"); String date_formatted =
			// formatter.format(dateFromSms);

			// date.setText(date_formatted);
			// String address =
			// cursor.getString(cursor.getColumnIndex("address"));
			title.setText(String.valueOf(type_value));

			String body = cursor
					.getString(cursor.getColumnIndexOrThrow("body"));
			descrip.setText(body);

			return (row);
		}
	}

	// //////////////SMSThread functions END/////////////////////////////

	// every window is initially same size
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {

		WindowManager wm = (WindowManager) this
				.getSystemService(this.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int width = display.getWidth(); // deprecated
		int height = display.getHeight(); // deprecated

		// TODO uncomment this code after deubugging
		int msg_box_height = (int) (height - .5 * height);
		int msg_box_width = (int) (width - 30);

		// TODO remove after debugging the touch button
		// int msg_box_height = (int) (100);
		// int msg_box_width = (int) (100);

		return new StandOutLayoutParams(id, msg_box_width, msg_box_height,
				StandOutLayoutParams.CENTER, StandOutLayoutParams.TOP+200,
				msg_box_width, msg_box_height);
	}

	// we want the system window decorations, we want to drag the body, we want
	// the ability to hide windows, and we want to tap the window to bring to
	// front
	@Override
	public int getFlags(int id) {
		return StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
				| StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
				| StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP;
		// return StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
		// | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP;
		// return StandOutFlags.FLAG_DECORATION_SYSTEM
		// | StandOutFlags.FLAG_BODY_MOVE_ENABLE
		// | StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
		// | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP
		// | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
		// | StandOutFlags.FLAG_WINDOW_PINCH_RESIZE_ENABLE;

		// return StandOutFlags.FLAG_DECORATION_MAXIMIZE_DISABLE
		// |StandOutFlags.FLAG_DECORATION_SYSTEM
		// |StandOutFlags.FLAG_DECORATION_RESIZE_DISABLE
		// | StandOutFlags.FLAG_BODY_MOVE_ENABLE
		// | StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP;
		// return super.getFlags(id) | StandOutFlags.FLAG_BODY_MOVE_ENABLE
		// | StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
	}

	@Override
	public String getPersistentNotificationTitle(int id) {
		return getAppName() + " Running";
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return "Click to add a new " + getAppName();
	}

	// return an Intent that creates a new MultiWindow
	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return StandOutWindow.getShowIntent(this, getClass(), getUniqueId());
	}

	@Override
	public int getHiddenIcon() {
		return android.R.drawable.ic_menu_info_details;
	}

	@Override
	public String getHiddenNotificationTitle(int id) {
		return getAppName() + " Hidden";
	}

	@Override
	public String getHiddenNotificationMessage(int id) {
		return "Click to restore #" + id;
	}

	// return an Intent that restores the MultiWindow
	@Override
	public Intent getHiddenNotificationIntent(int id) {
		return StandOutWindow.getShowIntent(this, getClass(), id);
	}

	@Override
	public Animation getShowAnimation(int id) {
		if (isExistingId(id)) {
			// restore
			return AnimationUtils.loadAnimation(this,
					android.R.anim.slide_in_left);
		} else {
			// show
			return super.getShowAnimation(id);
		}
	}

	@Override
	public Animation getHideAnimation(int id) {
		return AnimationUtils.loadAnimation(this,
				android.R.anim.slide_out_right);
	}

	@Override
	public List<DropDownListItem> getDropDownItems(int id) {
		List<DropDownListItem> items = new ArrayList<DropDownListItem>();
		items.add(new DropDownListItem(android.R.drawable.ic_menu_help,
				"About", new Runnable() {

					public void run() {
						Toast.makeText(
								MessageBoxWindow.this,
								getAppName()
										+ " is a demonstration of StandOut.",
								Toast.LENGTH_SHORT).show();
					}
				}));
		items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences,
				"Settings", new Runnable() {

					public void run() {
						Toast.makeText(MessageBoxWindow.this,
								"There are no settings.", Toast.LENGTH_SHORT)
								.show();
					}
				}));
		return items;
	}

	public void onReceiveData(int id, int requestCode, Bundle data,
			Class<? extends StandOutWindow> fromCls, int fromId) {
		// receive data from WidgetsWindow's button press
		// to show off the data sending framework

		switch (requestCode) {
		case ChatHeadWindow.HIDE_INTENT:
			if (this.isExistingId(id)) {
				this.hide(id);
			}
			break;
		case ChatHeadWindow.CLOSE_INTENT:
			if (this.isExistingId(id)) {
				this.close(id);
			}
			break;
		case ChatHeadWindow.SHOW_INTENT:

			senderId = data.getLong("sender_id");
			senderDisplayName = data.getString("sender_display_name");
			senderNumber = data.getString("sender_number");
			messageThreadId = data.getInt("message_thread_id");
			messageBody = data.getString("message_body");
			parentChatHeadId = fromId;

			// if (this.getWindow(id).visibility == Window.VISIBILITY_GONE) {
			// TODO remove the commented exception code for showing window when
			// its open (StandOutWindow.java)
			this.show(id);

			break;
		}
		/*
		 * switch (requestCode) { case WidgetsWindow.DATA_CHANGED_TEXT: Window
		 * window = getWindow(id); if (window == null) { String errorText =
		 * String.format(Locale.US,
		 * "%s received data but Window id: %d is not open.", getAppName(), id);
		 * Toast.makeText(this, errorText, Toast.LENGTH_SHORT).show(); return; }
		 * String changedText = data.getString("changedText"); TextView status =
		 * (TextView) window.findViewById(R.id.id); status.setTextSize(20);
		 * status.setText("Received data from WidgetsWindow: " + changedText);
		 * break; default: Log.d("MultiWindow", "Unexpected data received.");
		 * break; }
		 */
	}
}
