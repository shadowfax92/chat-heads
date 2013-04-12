package com.shadowfax.apps.chatheads;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wei.mark.standout.StandOutWindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class IncomingSmsBroadcastReceiver extends BroadcastReceiver {

	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	public static Map<String, Integer> chatBoxOpenedMap = new HashMap<String, Integer>();
	public static Map<Integer, String> reverseChatBoxOpenedMap = new HashMap<Integer, String>();

	public static final int INCOMING_SMS_BROADCAST_RECEIVER_ID = 1000;
	public static final int SHOW_CHAT_HEAD_WINDOW_INTENT = 1001;

	public PreferenceOperations prefOps;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		prefOps = new PreferenceOperations(context);
		boolean chat_heads_state_pref = prefOps.getChatHeadsStatePref();

		if (chat_heads_state_pref == true) {
			if (intent != null && SMS_RECEIVED.equals(intent.getAction())) {
				final SmsMessage smsMessage = extractSmsMessage(intent);
				processMessage(context, smsMessage);
			}
		}

	}

	private SmsMessage extractSmsMessage(final Intent intent) {

		final Bundle pudsBundle = intent.getExtras();
		final Object[] pdus = (Object[]) pudsBundle.get("pdus");
		final SmsMessage smsMessage = SmsMessage
				.createFromPdu((byte[]) pdus[0]);

		return smsMessage;

	}

	private void processMessage(final Context context,
			final SmsMessage smsMessage) {

		String message_body = smsMessage.getMessageBody();
		String message_sender_number = smsMessage.getOriginatingAddress();
		String originate_address = smsMessage.getDisplayOriginatingAddress();

		if (chatBoxOpenedMap.get(originate_address) == null) {
			int unique_id = StandOutWindow.myGetUniqueId(ChatHeadWindow.class);
			chatBoxOpenedMap.put(originate_address, unique_id);
			reverseChatBoxOpenedMap.put(unique_id, originate_address);

			// Intent call_chat_head_window_intent=new Intent(context,
			// ChatHeadWindow.class);
			// call_chat_head_window_intent.putExtra("message_body",
			// message_body);
			// call_chat_head_window_intent.putExtra("message_sender_number",
			// message_sender_number);
			// call_chat_head_window_intent.putExtra("chat_head_window_unique_id",
			// unique_id);
			// context.startService(call_chat_head_window_intent);
			//
			Bundle data = new Bundle();
			data.putString("message_body", message_body);
			data.putString("message_sender_number", message_sender_number);
			StandOutWindow.show(context, ChatHeadWindow.class, unique_id);

			StandOutWindow.sendData(context, ChatHeadWindow.class, unique_id,
					SHOW_CHAT_HEAD_WINDOW_INTENT, data, StandOutWindow.class,
					INCOMING_SMS_BROADCAST_RECEIVER_ID);

			// StandOutWindow.show(context, ChatHeadWindow.class, unique_id);
		} else {

			// TODO call an intent to refersh the messages in the MessageBox
			int window_id = chatBoxOpenedMap.get(originate_address);
			Bundle data = new Bundle();
			data.putString("message_body", message_body);
			data.putString("message_sender_number", message_sender_number);

			StandOutWindow.sendData(context, ChatHeadWindow.class, window_id,
					SHOW_CHAT_HEAD_WINDOW_INTENT, data, StandOutWindow.class,
					INCOMING_SMS_BROADCAST_RECEIVER_ID);
			// StandOutWindow.show(context, MultiWindow.class, window_id);
		}

	}
}