package sms.massivo.task.sender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.NotificatorHelper;
import sms.massivo.helper.db.controller.ConfigController;
import sms.massivo.helper.db.controller.DailyController;
import sms.massivo.view.main.SMSMassivo;
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

	private final List<SMSSender> senders = new ArrayList<SMSSender>();
	private final SMSMassivo smsMassivo;
	private ConfigController config;
	private NotificatorHelper notificatorHelper;
	private DailyController dailyController;
	private int token = 0;
	private BroadcastReceiver deliveryBroadcastReceiver;
	private BroadcastReceiver sentBroadcastReceiver;
	private Set<Integer> sentSmsBroadcastReceivedIdCache = new HashSet<Integer>();
	private Set<Integer> receiverSmsBroadcastReceivedIdCache = new HashSet<Integer>();

	private int NOTIFICATION = R.notification.smsSenderNotificationId;
	private SMSSenderParams params;

	public SMSSenderManager(SMSMassivo smsMassivo) {
		this.smsMassivo = smsMassivo;
		config = smsMassivo.getConfig();
		notificatorHelper = new NotificatorHelper(smsMassivo, NOTIFICATION);
	}

	protected SMSSenderParams getParams() {
		return params;
	}

	private void load() {
		if (config.isRunning()) {
			Log.d(TAG, "Processo marcado como em execução. Marcando processo como parado");
			config.markAsStopped();
		}

		Log.d(TAG, "Instanciando novos SMSSenders");
		for (int i = 0; i < params.getTotalOfSlaves(); i++) {
			SMSSender sender = new SMSSender(i, this);
			senders.add(sender);
		}
	}

	public void execute(SMSSenderParams params) {
		if (config.isRunning())
			return;

		this.params = params;

		preExecution();

		if (dailyController.getTotalSent() >= params.getTotalOfMessages()) {
			notify(R.string.progressDialogMessagesHasBeenSent, dailyController.getTotalSent());
			cancel();
			return;
		}
		token = dailyController.getTotalSent();
		Log.i(TAG, "Valor inicial do token: " + token);

		config.markAsRunning();
		notify(R.string.sendSmsServiceStarted);

		Monitor monitor = new Monitor();
		for (SMSSender s : senders) {
			s.setParams(params);
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

				Log.i(TAG, "Marcando como finalizado com êxito");
				config.markAsStopped();
				SMSSenderManager.this.notify(R.string.notificationSmsSentFinished, token, params.getPhone());
			} finally {
				posExecution();
			}
		}
	}

	private void posExecution() {
		if (dailyController != null) {
			Log.d(TAG, "Fechando conexão com banco de dados...");
			dailyController.close();
		}

		unregisterReceiverSMSContextListener();
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
		params.setSimCard(EnvironmentAccessor.getInstance().getSimCardNumber(smsMassivo));
		Log.i(TAG, "Parâmetros recebidos: " + params);

		load();
		clearCaches();
		registerSentSMSContextListener();
		registerReceiverSMSContextListener();

		dailyController = DailyController.getInstance(smsMassivo, params.getSimCard(), params.getPhone());
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

	private void unregisterReceiverSMSContextListener() {
		if (deliveryBroadcastReceiver != null) {
			Log.d(TAG, "Removendo registro de callback de entrega de SMS...");
			smsMassivo.unregisterReceiver(deliveryBroadcastReceiver);
			deliveryBroadcastReceiver = null;
		}
	}

	public void cancel() {
		Log.i(TAG, "Envio de SMS cancelado pelo usuário");
		config.markAsStoppedByUser();
		smsMassivo.updateScreen();
	}

	protected int getToken(SMSSenderParams params) {
		synchronized (params) {
			if (token >= params.getTotalOfMessages()) {
				return -1;
			}
			return token++;
		}
	}

	public boolean isRunning() {
		return config.isRunning();
	}

	protected String format(int resId, Object... params) {
		return String.format(smsMassivo.getString(resId), params);
	}

	protected void notify(int messageId, Object... messageParams) {
		Log.i(TAG, "Atualizando notificações...");

		Intent intentToCall = new Intent(smsMassivo, SMSMassivo.class);
		notificatorHelper.notify(android.R.drawable.ic_menu_upload, intentToCall, R.string.app_name, messageId, messageParams);

		Log.i(TAG, "Notificações atualizadas");
	}

	private void registerReceiverSMSContextListener() {
		if (deliveryBroadcastReceiver == null) {
			Log.d(TAG, "Registrando callback de entrega de SMS...");
			deliveryBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int smsId = intent.getExtras().getInt("id");
					if (receiverSmsBroadcastReceivedIdCache.contains(smsId)) {
						return;
					} else {
						receiverSmsBroadcastReceivedIdCache.add(smsId);
					}
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Log.i(TAG, "SMS entregue");
						dailyController.incDelivery();
						break;
					case Activity.RESULT_CANCELED:
						Log.e(TAG, "SMS não entregue");
						Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
						dailyController.incCanceled();
						break;
					}
				}
			};
		}
		smsMassivo.registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED_SMS_INTENT));
	}

	protected PendingIntent getSentSmsIntent(int id, int token) {
		Intent sentSmsIntent = new Intent(SMSSenderManager.SENT_SMS_INTENT);
		sentSmsIntent.putExtra("id", id);
		sentSmsIntent.putExtra("token", token);
		PendingIntent sentPI = PendingIntent.getBroadcast(smsMassivo, id, sentSmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return sentPI;
	}

	protected PendingIntent getDeliveredSmsIntent(int id, int token) {
		Intent deliveredSmsIntent = new Intent(SMSSenderManager.DELIVERED_SMS_INTENT);
		deliveredSmsIntent.putExtra("id", id);
		deliveredSmsIntent.putExtra("token", token);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(smsMassivo, id << 2, deliveredSmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return deliveredPI;
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
						Log.e(TAG, "Sem serviço de envio de SMS");
						Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
						dailyController.incNoService();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Log.e(TAG, "PDU não encontrada");
						Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
						dailyController.incNullPDU();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Log.e(TAG, "Rádio desligado");
						Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
						dailyController.incRadioOff();
						break;
					default:
						Toast.makeText(context, "Código inesperado: " + getResultCode(), Toast.LENGTH_LONG);
					}
					if (dailyController.getTotalOfFailures() >= params.getFailureTolerance()) {
						SMSSenderManager.this.notify(R.string.notificationSmsSenderAborted, token, params.getPhone(), dailyController.getTotalOfFailures());
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
}
