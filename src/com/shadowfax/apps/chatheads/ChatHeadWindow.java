package com.shadowfax.apps.chatheads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.R.integer;
import android.R.string;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This implementation provides multiple windows. You may extend this class or
 * use it as a reference for a basic foundation for your own windows.
 * 
 * <p>
 * Functionality includes system window decorators, moveable, resizeable,
 * hideable, closeable, and bring-to-frontable.
 * 
 * <p>
 * The persistent notification creates new windows. The hidden notifications
 * restores previously hidden windows.
 * 
 * @author Mark Wei <markwei@gmail.com>
 * 
 */
public class ChatHeadWindow extends StandOutWindow {

	public static Map<Integer, Mediator> chatHeadIdToMediatorObjectMap=new HashMap<Integer, Mediator>();
	
	public static final int HIDE_INTENT = 0;
	public static final int CLOSE_INTENT = 1;
	public static final int SHOW_INTENT = 2;
	
	public static int CHAT_WINDOW_MIN_WIDTH=180;
	public static int CHAT_WINDOW_MIN_HEIGHT=150;
	public static int CHAT_WINDOW_WIDTH=180;
	public static int CHAT_WINDOW_HEIGHT=150;

	public ImageView closeButtonImage, userImage, plusButtonImage;
	public ImageButton chatHeadImageButton;
	public TextView userNameTextView;
	
	public int toggleChatMessageWindowFlag = 0;//if 0 then hidden and 1 then displayed
	public int current_window_id_public;
	public int msgbox_window_id_public;

	public int phoneScreenWidth;
	public int phoneScreenHeight;
	public String senderName;
	public Bitmap senderImage;
	public View currentView;

	@Override
	public String getAppName() {
		return "MultiWindow";
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
		View view = inflater.inflate(R.layout.body, frame, true);
		currentView=view;
		initializeParameters();

		final int id_current_multiwindow = id;
		current_window_id_public = id;
		final int unique_id_msg_box = id_current_multiwindow;
		msgbox_window_id_public = unique_id_msg_box;
		
		closeButtonImage.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				handleCloseEvents(id_current_multiwindow, unique_id_msg_box);
			}
		});

		// toggle using plus button
		
		plusButtonImage.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				toggleChatMessageWindow(unique_id_msg_box,
						id_current_multiwindow);
			}
		});

		// userimage on touch
		

	}
	
	public void initializeParameters()
	{
		//getting phone width and height
		WindowManager wm = (WindowManager) this
				.getSystemService(this.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		phoneScreenWidth = display.getWidth(); // deprecated
		phoneScreenHeight = display.getHeight(); // deprecated
		
		closeButtonImage = (ImageView) currentView.findViewById(R.id.close_image);
		plusButtonImage = (ImageView) currentView.findViewById(R.id.plus_image);
		userImage = (ImageView) currentView.findViewById(R.id.user_picture_image);
		userNameTextView=(TextView) currentView.findViewById(R.id.user_name_rounded_textview);
	}

	// TODO onTouch is not as required. see if you can correct it.
	public boolean onTouchBody(int id, Window window, View view,
			MotionEvent event) {
		// if(R.id.user_picture_image==view.getId())
		// if (id == current_window_id_public
		// && event.getAction() != MotionEvent.ACTION_MOVE
		// && event.getAction() == MotionEvent.ACTION_UP) {
		// toggleChatMessageWindow(msgbox_window_id_public,
		// current_window_id_public);
		// }
		// final int action = event.getAction();
		// switch(action & MotionEventCompat.ACTION_MASK) {
		// case MotionEvent.ACTION_MOVE:
		// // user is still touching view and moving pointer around
		// break;
		// case MotionEvent.ACTION_DOWN:
		// toggleChatMessageWindow(msgbox_window_id_public,current_window_id_public);
		// break;
		//
		// case MotionEvent.ACTION_UP:
		// // user lifts pointer
		// break;
		// }
		return false;
	}
	
	public void handleCloseEvents(int current_window_id, int unique_msg_box_id) {
		closeChatHeadWindow(current_window_id);
		closeChatMessageWindow(unique_msg_box_id, current_window_id);
		// remove the user from hashmap in broadcast receiver.

		String originating_address = IncomingSmsBroadcastReceiver.reverseChatBoxOpenedMap
				.get(current_window_id);
		IncomingSmsBroadcastReceiver.chatBoxOpenedMap
				.remove(originating_address);
		IncomingSmsBroadcastReceiver.reverseChatBoxOpenedMap
				.remove(current_window_id);
	}

	public void closeChatMessageWindow(int msg_box_id, int my_id) {
		// TODO add the appt data that has to be passed
		String closeIntentString = "";
		Bundle data = new Bundle();
		data.putString("changedText", closeIntentString);
		sendData(my_id, MessageBoxWindow.class, msg_box_id, CLOSE_INTENT, data);
	}

	public void toggleChatMessageWindow(int msg_box_id, int my_id) {
		Mediator mediator_Object = chatHeadIdToMediatorObjectMap.get(my_id);
		if (toggleChatMessageWindowFlag == 0) {

			// before displaying move the chatHead to the top
			toggleChatMessageWindowFlag = 1;

			Window window = new Window(ChatHeadWindow.this, my_id);
			int x_pos_chathead = phoneScreenWidth/2-CHAT_WINDOW_WIDTH/2;

			// TODO instead of intial layout param try to get the current x and
			// y coordinates.

			window.edit().setPosition(x_pos_chathead, 10).commit();
			window.setId(my_id);

			// show the window
			// StandOutWindow.show(this, MessageBoxWindow.class, msg_box_id);
			Bundle data = new Bundle();
			data.putLong("sender_id",mediator_Object.senderId);
			data.putString("sender_display_name", mediator_Object.messageSenderDisplayName);
			data.putString("sender_number", mediator_Object.messageSenderNumber);
			data.putInt("message_thread_id",mediator_Object.messageThreadId);
			data.putString("message_body", mediator_Object.messageBody);
			
			sendData(my_id, MessageBoxWindow.class, msg_box_id, SHOW_INTENT,
					data);

		} else {
			toggleChatMessageWindowFlag = 0;

			// send an intent to hide the message box
			// TODO add the appt data that has to be passed
			String hideIntentString = "";
			Bundle data = new Bundle();
			data.putString("hideWindow", hideIntentString);
			sendData(my_id, MessageBoxWindow.class, msg_box_id, HIDE_INTENT,
					data);

		}

	}

	public void closeChatHeadWindow(int id) {
		if (StandOutWindow.myIsWindowExisting(id, getClass())) {
			StandOutWindow.close(this, getClass(), id);
		}
	}

	public void createRoundedImage2(View view, int width, int height,Bitmap bitmap_user) {
		Bitmap bitmapOriginal;
		if(bitmap_user==null)
		{
		
		bitmapOriginal = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_user_icon256);
		}
		else
		{
			bitmapOriginal=bitmap_user;
		}
		ImageView myImageView = (ImageView) view
				.findViewById(R.id.user_picture_image);
		Bitmap bitmap = Bitmap.createScaledBitmap(bitmapOriginal, width,
				height, true);
		Bitmap circleBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		BitmapShader shader = new BitmapShader(bitmap, TileMode.CLAMP,
				TileMode.CLAMP);
		Paint paint = new Paint();
		paint.setShader(shader);

		Canvas c = new Canvas(circleBitmap);
		c.drawCircle(width / 2, height / 2, width / 2, paint);

		myImageView.setImageBitmap(circleBitmap);
	}

	// creates a rounded image and sets it
	// TODO take the bitmap image and set it, rather than default bitmap image
	public void createRoundedImage(View view) {
		ImageView img1 = (ImageView) view.findViewById(R.id.user_picture_image);
		Bitmap bm = BitmapFactory.decodeResource(getResources(),
				R.drawable.default_user_icon256);
		Bitmap resized = Bitmap.createScaledBitmap(bm, 100, 100, true);
		Bitmap conv_bm = getRoundedRectBitmap(resized, 100);
		img1.setImageBitmap(conv_bm);
	}

	public static Bitmap getRoundedRectBitmap(Bitmap bitmap, int pixels) {
		Bitmap result = null;
		try {
			result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(result);

			int color = 0xff424242;
			Paint paint = new Paint();
			Rect rect = new Rect(0, 0, 200, 200);

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawCircle(50, 50, 50, paint);
			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);

		} catch (NullPointerException e) {
		} catch (OutOfMemoryError o) {
		}
		return result;
	}

	// every window is initially same size
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		// TODO correct it to the appropriate value
		return new StandOutLayoutParams(id, CHAT_WINDOW_WIDTH, CHAT_WINDOW_HEIGHT,
				StandOutLayoutParams.AUTO_POSITION,
				StandOutLayoutParams.TOP + 10, CHAT_WINDOW_MIN_WIDTH, CHAT_WINDOW_MIN_HEIGHT);
	}

	// we want the system window decorations, we want to drag the body, we want
	// the ability to hide windows, and we want to tap the window to bring to
	// front
	@Override
	public int getFlags(int id) {
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
		return StandOutFlags.FLAG_BODY_MOVE_ENABLE
				| StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE
				| StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TOUCH
				| StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
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
								ChatHeadWindow.this,
								getAppName()
										+ " is a demonstration of StandOut.",
								Toast.LENGTH_SHORT).show();
					}
				}));
		items.add(new DropDownListItem(android.R.drawable.ic_menu_preferences,
				"Settings", new Runnable() {

					public void run() {
						Toast.makeText(ChatHeadWindow.this,
								"There are no settings.", Toast.LENGTH_SHORT)
								.show();
					}
				}));
		return items;
	}

	public void onReceiveData(int id, int requestCode, Bundle data,
			Class<? extends StandOutWindow> fromCls, int fromId) {
		
		Mediator mediator_object;
		
		switch (requestCode) {
		case IncomingSmsBroadcastReceiver.SHOW_CHAT_HEAD_WINDOW_INTENT:
			String message_body = data.getString("message_body");
			String message_sender_number = data
					.getString("message_sender_number");

			if(chatHeadIdToMediatorObjectMap.get(id)==null)
			{
				mediator_object = new Mediator(this, message_body,
						message_sender_number);
				mediator_object.initMediatorObjet();
				
				senderName = mediator_object.messageSenderDisplayName;
				senderImage = mediator_object.senderImage;
				userNameTextView.setText(senderName);
				createRoundedImage2(currentView, 100, 100, senderImage);
				
				chatHeadIdToMediatorObjectMap.put(id, mediator_object);
			}
			else
			{
				mediator_object=chatHeadIdToMediatorObjectMap.get(id);
			}
			
			
			break;

		case MessageBoxWindow.INFORM_CHAT_HEAD_CLASS_MSG_WINDOW_HIDDEN_INTENT:
			toggleChatMessageWindowFlag=0;
			break;
		default:
			break;
		}
	}

}
