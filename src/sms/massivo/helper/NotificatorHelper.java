package sms.massivo.helper;

import sms.massivo.R;
import sms.massivo.view.main.SMSMassivo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class NotificatorHelper {
	private static int sequencer = 0;
	private NotificationManager notificationManager;
	private final Context context;
	private final int notificationId;

	public NotificatorHelper(Context context) {
		this(context, Integer.MAX_VALUE - sequencer);
	}

	public NotificatorHelper(Context context, int notificationId) {
		sequencer++;
		this.context = context;
		this.notificationId = notificationId;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	public void notify(int iconRes, Intent intentToCall, int titleId, int messageId, Object ... messageParams){
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentToCall, PendingIntent.FLAG_UPDATE_CURRENT);
		String msg = String.format(context.getString(R.string.notificationSmsSenderAborted), messageParams);

		Notification notification = new Notification(iconRes, msg, System.currentTimeMillis());
		notification.setLatestEventInfo(context, context.getText(titleId), msg, contentIntent);
		
		notificationManager.notify(notificationId, notification );
	}
}
