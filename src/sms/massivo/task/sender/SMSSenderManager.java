package sms.massivo.task.sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.NotificatorHelper;
import sms.massivo.helper.db.controller.ConfigController;
import sms.massivo.helper.db.controller.DailyController;
import sms.massivo.view.main.SMSMassivo;
import sms.massivo.view.report.Report;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSSenderManager {
	public static final String TAG = "SMSSenderManager";
	public static final String TOTAL_OF_SMS = "totalOfSMS";
	public static final String PHONE = "phone";
	protected static final String SENT_SMS_INTENT = "SMS_SENT";
	protected static final String DELIVERED_SMS_INTENT = "SMS_DELIVERED";

	private final Map<Integer, SMSSender> senders = new HashMap<Integer, SMSSender>();
	private final SMSMassivo smsMassivo;
	private ConfigController config;
	private String simCard;
	private NotificatorHelper notificatorHelper;
	private DailyController dailyController;
	private int token = 0;
	private BroadcastReceiver deliveryBroadcastReceiver;
	private BroadcastReceiver sentBroadcastReceiver;
	private Set<Integer> sentSmsBroadcastReceivedIdCache = new HashSet<Integer>();
	private Set<Integer> receiverSmsBroadcastReceivedIdCache = new HashSet<Integer>();

	private int NOTIFICATION = R.notification.smsSenderNotificationId;

	public SMSSenderManager(SMSMassivo smsMassivo) {
		this.smsMassivo = smsMassivo;
		config = smsMassivo.getConfig();
		notificatorHelper = new NotificatorHelper(smsMassivo, NOTIFICATION);
	}

	private void load() {
		if (config.isRunning()) {
			Log.d(TAG, "Processo marcado como em execu��o. Marcando processo como parado");
			config.markAsStopped();
		}

		Log.d(TAG, "Instanciando novos SMSSenders");
		for (int i = 0; i < config.getTotalOfSlaves(); i++) {
			SMSSender sender = new SMSSender(i, this);
			senders.put(i, sender);
		}
	}

	public void execute() {
		if (config.isRunning())
			return;

		preExecution();

		if (dailyController.getTotalSent() >= config.getTotalOfMessagesToSend()) {
			notify(R.string.progressDialogMessagesHasBeenSent, dailyController.getTotalSent());
			config.markAsStopped();
			smsMassivo.updateScreen();
			return;
		}
		token = dailyController.getTotalSent();
		Log.i(TAG, "Valor inicial do token: " + token);

		config.markAsRunning();
		notify(R.string.sendSmsServiceStarted);

		Monitor monitor = new Monitor();
		senders.clear();
		for (SMSSender s : senders.values()) {
			monitor.add(s);
		}

		monitor.start();
	}

	private class Monitor extends Thread {
		public static final String TAG = "Monitor";
		private List<Thread> threads = new ArrayList<Thread>();

		public Monitor() {
			super("Monitor");
			setDaemon(true);
		}

		public void add(SMSSender sender) {
			Thread thread = new Thread(sender, "SmsSenderThread[" + sender.getId() + "]");
			thread.isDaemon();
			threads.add(thread);
		}

		@Override
		public void run() {
			try {
				for (Thread t : threads) {
					t.start();
				}
				for (Thread t : threads) {
					try {
						t.join();
					} catch (InterruptedException e) {
						Log.w(TAG, e);
					}
				}

				Log.i(TAG, "Marcando como finalizado com �xito");
				config.markAsStopped();
				SMSSenderManager.this.notify(R.string.notificationSmsSentFinished, token, config.getPhone());
			} finally {
				posExecution();
			}
		}
	}

	private void posExecution() {
		if (dailyController != null) {
			Log.d(TAG, "Fechando conex�o com banco de dados...");
			dailyController.close();
		}

		unregisterSentSMSContextListener();

		Log.i(TAG, "Alertando tarefa finalizada via som...");
		EnvironmentAccessor.getInstance().playRingtone(smsMassivo);
		Log.i(TAG, "Alertando tarefa finalizada via vibrador...");
		EnvironmentAccessor.getInstance().vibrate(smsMassivo);

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		updateScreen();
		Log.i(TAG, "Tarefa finalizada");
	}

	protected void updateScreen() {
		smsMassivo.runOnUiThread(new Runnable() {
			public void run() {
				smsMassivo.updateScreen();
			}
		});
	}

	private void preExecution() {
		simCard = EnvironmentAccessor.getInstance().getSimCardNumber(smsMassivo);

		load();
		clearCaches();
		registerSentSMSContextListener();

		dailyController = DailyController.getInstance(smsMassivo, simCard, config.getPhone());
	}

	private void clearCaches() {
		Log.i(TAG, "Limpando caches de broadcast...");
		sentSmsBroadcastReceivedIdCache.clear();
		receiverSmsBroadcastReceivedIdCache.clear();
	}

	private void unregisterSentSMSContextListener() {
		if (sentBroadcastReceiver != null) {
			Log.d(TAG, "Removendo registro de callback de envio de SMS...");
			smsMassivo.unregisterReceiver(sentBroadcastReceiver);
			sentBroadcastReceiver = null;
		}
	}

	public void cancel() {
		Log.i(TAG, "Envio de SMS cancelado pelo usu�rio");
		config.markAsStoppedByUser();
		smsMassivo.updateScreen();
	}

	protected int getToken() {
		return dailyController.nextCounter();
	}

	public boolean isRunning() {
		return config.isRunning();
	}

	protected String format(int resId, Object... params) {
		return String.format(smsMassivo.getString(resId), params);
	}

	protected void notify(int messageId, Object... messageParams) {
		Log.i(TAG, "Atualizando notifica��es...");

		Intent intentToCall = new Intent(smsMassivo, Report.class);
		intentToCall.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notificatorHelper.notify(android.R.drawable.ic_menu_upload, intentToCall, R.string.app_name, messageId, messageParams);

		Log.i(TAG, "Notifica��es atualizadas");
	}

	protected PendingIntent getSentSmsIntent(int id, int token) {
		Intent sentSmsIntent = new Intent(SMSSenderManager.SENT_SMS_INTENT);
		sentSmsIntent.putExtra("id", id);
		sentSmsIntent.putExtra("token", token);
		PendingIntent sentPI = PendingIntent.getBroadcast(smsMassivo, id, sentSmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return sentPI;
	}

	private void registerSentSMSContextListener() {
		if (sentBroadcastReceiver == null) {
			Log.d(TAG, "Registrando callback de envio de SMS...");
			sentBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int smsToken = intent.getExtras().getInt("token");
					if (sentSmsBroadcastReceivedIdCache.contains(smsToken)) {
						return;
					} else {
						sentSmsBroadcastReceivedIdCache.add(smsToken);
					}

					int smsId = intent.getExtras().getInt("id");
					Log.i(TAG, "BroadcastReceiver do SMS "+smsToken+" pelo SmsSender "+smsId);
					dailyController.incTotalSent();

					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Log.i(TAG, "SMS enviado com sucesso");
						dailyController.incSendSuccessfully();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Log.e(TAG, "Falha generica no envio de SMS");
						Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
						dailyController.incGenericFailure();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Log.e(TAG, "Sem servi�o de envio de SMS");
						Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
						dailyController.incNoService();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Log.e(TAG, "PDU n�o encontrada");
						Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
						dailyController.incNullPDU();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Log.e(TAG, "R�dio desligado");
						Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
						dailyController.incRadioOff();
						break;
					default:
						Toast.makeText(context, "C�digo inesperado: " + getResultCode(), Toast.LENGTH_LONG);
					}
					if (dailyController.getTotalOfFailures() >= config.getFailureTolerance()) {
						SMSSenderManager.this.notify(R.string.notificationSmsSenderAborted, token, config.getPhone(), dailyController.getTotalOfFailures());
						config.markAsStopped();
						return;
					}
					smsMassivo.updateScreen();

					if (isRunning()) {
						senders.get(smsId).sendSmsInterator();
					}
				}
			};
		}
		smsMassivo.registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT_SMS_INTENT));
	}

	protected void toast(String text) {
		Toast.makeText(smsMassivo, text, Toast.LENGTH_LONG).show();
	}

	protected ConfigController getConfig() {
		return config;
	}

	public boolean hasMoreSmsToSend() {
		return config.getTotalOfMessagesToSend() >= dailyController.getTotalSent();
	}
}
