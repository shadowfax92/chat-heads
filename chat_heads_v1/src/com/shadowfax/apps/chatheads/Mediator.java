package com.shadowfax.apps.chatheads;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.widget.Toast;

public class Mediator {
	String messageBody;
	String messageSenderNumber;
	Long senderId;
	String messageSenderDisplayName;
	int messageThreadId;
	int photoId;
	Bitmap senderImage;

	Context myContext;

	public Mediator(Context context, String message_body,
			String message_sender_number) {
		myContext = context;
		messageBody = message_body;
		messageSenderNumber = message_sender_number;
	}

	public void initMediatorObjet() {

		String message_body = messageBody;
		String message_sender_number = messageSenderNumber;

		if (message_body != null && message_sender_number != null) {
			// Extract Sender ID and Display Name from message_sender_number
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					Uri.encode(message_sender_number));
			Cursor cur = myContext.getContentResolver().query(
					uri,
					new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID,
							PhoneLookup.PHOTO_ID }, null, null, null);

			try {
				Long sender_id = (long) -1;
				int photo_id = -1;
				String message_sender_display_name = null;
				if (cur != null && cur.getCount() != 0) {
					cur.moveToFirst();
					sender_id = cur.getLong(cur.getColumnIndex("_id"));
					photo_id = cur.getInt(cur.getColumnIndex("photo_id"));
					message_sender_display_name = cur.getString(cur
							.getColumnIndex("display_name"));
				} else {
					// This number is not a contact in phone book
					message_sender_display_name = "Unknown";
					sender_id = (long) -1;
				}

				Log.d("My Logs", "Sender ID:" + sender_id);
				Log.d("My Logs", "Sender Display Name:"
						+ message_sender_display_name);

				Uri uriSMSURI = Uri.parse("content://sms/");
				Cursor cur2 = myContext.getContentResolver().query(uriSMSURI,
						null, " person = " + sender_id, null, null);
				Log.d("My Logs", "Cursor2 Count:" + cur2.getCount());

				int message_thread_id;
				if (cur2 != null && cur2.getCount() != 0) {
					cur2.moveToFirst();
					message_thread_id = cur2.getInt(cur2
							.getColumnIndex("thread_id"));
				} else {
					Uri uriSMSURI2 = Uri
							.parse("content://mms-sms/conversations/");
					Cursor cur3 = myContext.getContentResolver().query(
							uriSMSURI, null,
							" address = " + message_sender_number, null, null);
					/*
					 * Cursor cur4=getContentResolver().query(uriSMSURI2, null,
					 * " address = "+message_sender_number, null, null); String
					 * list[]=cur4.getColumnNames(); String result=""; for(int
					 * i=0;i<list.length;i++) { result+=list[i]+"\n"; }
					 * Log.d("My Logs", "Cursor 4 Column Names:"+result);
					 * Log.d("My Logs", "Cursor3 Count:"+cur3.getCount());
					 * Log.d("My Logs","Cursor4 Count:"+cur4.getCount());
					 * Log.d("My Logs","Sender Number:"+message_sender_number);
					 */
					if (cur3 != null && cur3.getCount() != 0) {
						cur3.moveToFirst();
						message_thread_id = cur3.getInt(cur3
								.getColumnIndex("thread_id"));
					} else {
						message_thread_id = -1;
					}

				}
				Log.d("My Logs", "Thread ID:" + message_thread_id);

				photoId = photo_id;
				senderId = sender_id;
				messageSenderDisplayName = message_sender_display_name;
				messageThreadId = message_thread_id;

				// get contact photo
				try {
					senderImage = loadContactPhoto(
							myContext.getContentResolver(), sender_id, photo_id);
				} catch (Exception e) {
					senderImage = null;
				}

			} catch (NullPointerException e) {
				Toast.makeText(myContext,
						"Meidator: PhoneNumber could not be Parsed!",
						Toast.LENGTH_LONG).show();
				Log.d("My Logs", "Mediator: NullPointerException");
			}
		}
	}

	public static Bitmap loadContactPhoto(ContentResolver cr, long id,
			long photo_id) {
		try {
			Uri uri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, id);
			InputStream input = ContactsContract.Contacts
					.openContactPhotoInputStream(cr, uri);
			if (input != null) {
				return BitmapFactory.decodeStream(input);
			} else {
				Log.d("PHOTO", "first try failed to load photo");

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		byte[] photoBytes = null;

		Uri photoUri = ContentUris.withAppendedId(
				ContactsContract.Data.CONTENT_URI, photo_id);

		Cursor c = cr.query(photoUri,
				new String[] { ContactsContract.CommonDataKinds.Photo.PHOTO },
				null, null, null);

		try {
			if (c.moveToFirst())
				photoBytes = c.getBlob(0);

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

		} finally {

			c.close();
		}

		if (photoBytes != null)
			return BitmapFactory.decodeByteArray(photoBytes, 0,
					photoBytes.length);
		else
			Log.d("PHOTO", "second try also failed");
		return null;
	}
}
