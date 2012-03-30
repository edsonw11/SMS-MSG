package sms.massivo.task.sender;

import java.util.Date;

import sms.massivo.R;
import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender implements Runnable {
	private final String TAG;

	private final SMSSenderManager manager;
	private SMSSenderParams param;

	private final int id;
	private boolean isFinished;

	public SMSSender(int id, SMSSenderManager manager) {
		super();
		TAG = "SMSSender[" + id + "]";
		this.id = id;
		this.manager = manager;
	}

	public void setParams(SMSSenderParams params) {
		param = params;
	}

	@Override
	public void run() {
		isFinished = false;
		sendSmsInterator();

		while (!isFinished && manager.isRunning()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	public boolean isFinished() {
		return isFinished;
	}

	protected void sendSmsInterator() {
		String text = null;
		int token = manager.getToken(param);
		if (token >= 0) {
			token++;
			if (!manager.isRunning()) {
				Log.i(TAG,manager.format(R.string.notificationSmsSenderCanceled, token, param.getPhone()));
				manager.notify(R.string.notificationSmsSenderCanceled, token, param.getPhone());
				isFinished = true;
				return;
			}

			Log.i(TAG,manager.format(R.string.notificationSendingSms, token, param.getPhone()));
			manager.updateScreen();
			manager.notify(R.string.notificationSendingSms, token, param.getPhone());

			text = manager.format(R.string.smsMessagePattern, token, new Date());
			sendSMS(param, token, text);
		} else{
			isFinished = true;
		}
	}

	private void sendSMS(final SMSSenderParams param, final int token, final String message) {
		Log.i(TAG, String.format("Enviando SMS %s para %s: '%s'", token, param.getPhone(), message));

		PendingIntent sentPI = manager.getSentSmsIntent(getId(), token);
		PendingIntent deliveredPI = manager.getDeliveredSmsIntent(getId(), token);

		try {
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(param.getPhone(), null, message, sentPI, deliveredPI);
		} catch (IllegalArgumentException e) {
			String text = String.format("%s [destinationAddress: %s, text: %s]", param.getPhone(), message);
			Log.e(TAG, text, e);
			manager.toast(text);
		}
	}

	public int getId() {
		return id;
	}
}
