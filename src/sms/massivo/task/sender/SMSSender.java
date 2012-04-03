package sms.massivo.task.sender;

import java.util.Date;

import sms.massivo.R;
import sms.massivo.helper.db.controller.ConfigController;
import android.app.PendingIntent;
import android.telephony.SmsManager;
import android.util.Log;

public class SMSSender implements Runnable {
	private final String TAG;

	private final SMSSenderManager manager;

	private final int id;
	private boolean isFinished;

	private ConfigController config;

	public SMSSender(int id, SMSSenderManager manager) {
		super();
		TAG = "SMSSender[" + id + "]";
		this.id = id;
		this.manager = manager;
		config = manager.getConfig();
		Log.d(TAG, "Instanciado SMSSender "+ id);
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
		int token = manager.getToken();
		if (manager.hasMoreSmsToSend()) {
//			token++;
			if (!manager.isRunning()) {
				Log.i(TAG,manager.format(R.string.notificationSmsSenderCanceled, token, config.getPhone()));
				manager.notify(R.string.notificationSmsSenderCanceled, token, config.getPhone());
				isFinished = true;
				return;
			}

			Log.i(TAG,manager.format(R.string.notificationSendingSms, token, config.getPhone()));
			manager.updateScreen();
			manager.notify(R.string.notificationSendingSms, token, config.getPhone());

			text = manager.format(R.string.smsMessagePattern, token, new Date());
			sendSMS(token, text);
		} else{
			isFinished = true;
		}
	}

	private void sendSMS(final int token, final String message) {
		Log.i(TAG, String.format("Enviando SMS %s para %s: '%s'", token, config.getPhone(), message));

		PendingIntent sentPI = manager.getSentSmsIntent(getId(), token);

		try {
			SmsManager sms = SmsManager.getDefault();
			
			//FIXME remover....
			try {
				Thread.sleep((long)(Math.random() * 5000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sms.sendTextMessage(config.getPhone(), null, message, sentPI, null);
		} catch (IllegalArgumentException e) {
			String text = String.format("%s [destinationAddress: %s, text: %s]", config.getPhone(), message);
			Log.e(TAG, text, e);
			manager.toast(text);
		}
	}

	public int getId() {
		return id;
	}
}
