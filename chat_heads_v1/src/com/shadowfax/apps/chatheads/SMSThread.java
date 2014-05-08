package com.shadowfax.apps.chatheads;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SMSThread extends Activity {
	ListView smsThreadList;
	TextView smsThreadListHeader;
	int numMessagesInInbox;
	EditText smsThreadListSendMessage;
	Button smsThreadListSendButton;
	

	Long senderId;
	String senderDisplayName;
	String senderNumber;
	int messageThreadId;
	String messageBody;

	// database part
	private Cursor myCursor;
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_thread_list);
		smsThreadList=(ListView)findViewById(R.id.sms_thread_listview);	
		smsThreadListHeader=(TextView)findViewById(R.id.sms_thread_list_header);
		smsThreadListSendButton=(Button)findViewById(R.id.sms_thread_list_send_button);
		smsThreadListSendMessage=(EditText)findViewById(R.id.sms_thread_list_send_message);
		
		Bundle extras=getIntent().getExtras();
		senderId=extras.getLong("sender_id");
		senderDisplayName=extras.getString("sender_display_name");
		senderNumber=extras.getString("sender_number");
		messageThreadId=extras.getInt("message_thread_id");
		messageBody=extras.getString("message_body");
		
		smsThreadListSendButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				String send_message=smsThreadListSendMessage.getText().toString();
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(senderNumber, null, send_message, null, null); 
				smsThreadListSendMessage.setText("");
				fillSMSThreadList();
			}
		});
		
		Toast.makeText(getApplicationContext(), "Sender ID:"+senderId+"\nDisplay Name:"+senderDisplayName
				+"\nThread ID:"+messageThreadId, Toast.LENGTH_LONG).show();
		fillSMSThreadList();
		
		
	}
	protected void onResume()
   	{
   		super.onResume();
   		fillSMSThreadList();
   	}
	private void fillSMSThreadList()
	{
		if(messageThreadId!=-1)
		{
			fillUsingSMSConversation();
		}
		else
		{
			//TODO This is just a temporary solution.
			fillUsingSingleSMS();
		}
	}
	private void fillUsingSMSConversation()
	{
		Uri uriSMSURI = Uri.parse("content://sms/");
		//content://mms-sms/conversations/
		//Uri uriSMSURI = Uri.parse("content://mms-sms/conversations/");
		String SORT_ORDER = " _id ASC";
		Cursor cur = getContentResolver().query(uriSMSURI, new String[] { "_id", "thread_id", "address", "person", "date", "body", "type" },
				" thread_id = " + messageThreadId, null, "DATE asc");
		
		//Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
		numMessagesInInbox=cur.getCount();		
		smsThreadListHeader.setText(String.valueOf(numMessagesInInbox)+"/"+String.valueOf(numMessagesInInbox));
		if(cur!=null && cur.getCount()!=0)
		{
			cur.moveToFirst();
		}	

		myCursor=cur;
		startManagingCursor(cur);

		String[] from = new String[] {"body"};

		int[] to = new int[] {
				R.id.sms_thread_list_row_descrip};
		smsThreadList.setAdapter(new SMSThreadListAdapter(from, to, cur));	
		smsThreadList.setSelection(smsThreadList.getAdapter().getCount()-1);
	}
	private void fillUsingSingleSMS()
	{
		//TODO This is just a temporary solution.
		String temp[]=new String[1];
		temp[0]=messageBody;
		smsThreadList.setAdapter(new ArrayAdapter<String>(this, R.layout.sms_thread_list_sender_row_new,R.id.sms_thread_list_row_descrip, temp));  
		
	}
	class SMSThreadListAdapter extends SimpleCursorAdapter
	{
		TextView title,descrip,date;
		Cursor cursor;
		public SMSThreadListAdapter(String from[],int to[],Cursor cursor) {
			super(SMSThread.this,R.layout.sms_thread_list_row, cursor, from, to);
			this.cursor=cursor;
		}
		public View getView(int position,View convertView,ViewGroup parent)
		{
			View row=null;

			row=convertView;
			/*if(row==null)
			{
				LayoutInflater inflater=getLayoutInflater();
				row=inflater.inflate(R.layout.sms_thread_list_row, parent, false);
			}*/

			int type_index=cursor.getColumnIndex("type");
			int type_value=cursor.getInt(type_index);
			LayoutInflater inflater=getLayoutInflater();
			if(type_value==1)
			{
				//Sender's messages
				row=inflater.inflate(R.layout.sms_thread_list_sender_row_new, parent, false);
			}
			else if(type_value==2)
			{
				//User's messages
				row=inflater.inflate(R.layout.sms_thread_list_user_row_new, parent, false);
			}
			else
			{
				//Ignore
			}

			title=(TextView)row.findViewById(R.id.sms_thread_list_row_title);
			descrip=(TextView)row.findViewById(R.id.sms_thread_list_row_descrip);
			//date=(TextView)row.findViewById(R.id.sms_thread_list_row_date);

			cursor.moveToPosition(position);

			/*int date_index=cursor.getColumnIndex("date");
			long date_value=cursor.getLong(date_index);
			Date dateFromSms = new Date(date_value);
			Format formatter = new SimpleDateFormat("HH:mm:ss, yyyy-MM-dd");
			String date_formatted = formatter.format(dateFromSms);*/
			//date.setText(date_formatted);
			//String address = cursor.getString(cursor.getColumnIndex("address"));
			title.setText(senderDisplayName);

			String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
			descrip.setText(body);

			return(row);
		}
	}
}
