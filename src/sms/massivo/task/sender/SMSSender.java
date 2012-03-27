package sms.massivo.task.sender;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import sms.massivo.R;
import sms.massivo.helper.ContextHelper;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.db.controller.DailyController;
import sms.massivo.view.main.SMSMassivo;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SMSSender extends AsyncTask<SMSSenderParams, SMSSenderProgress, Void> {
	public static final String TAG = "SMSSender";
	public static final String TOTAL_OF_SMS = "totalOfSMS";
	public static final String PHONE = "phone";
	private static final String SENT_SMS_INTENT = "SMS_SENT";
	private static final String DELIVERED_SMS_INTENT = "SMS_DELIVERED";
	
	private int NOTIFICATION = R.notification.smsSenderNotificationId;

	private NotificationManager notificationManager;
	private ProgressDialog progressDialog;
	private final SMSMassivo smsMassivo;
	private DailyController dailyController;
	private BroadcastReceiver deliveryBroadcastReceiver;
	private BroadcastReceiver sentBroadcastReceiver;
	private SMSSenderParams param;
	private int counter;
	private Set<Integer> sentSmsBroadcastReceivedIdCache = new HashSet<Integer>();
	private Set<Integer> receiverSmsBroadcastReceivedIdCache = new HashSet<Integer>();

	public SMSSender(SMSMassivo smsMassivo) {
		super();
		this.smsMassivo = smsMassivo;
		notificationManager = (NotificationManager) smsMassivo.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		smsMassivo.getConfig().markAsStopped();
	}

	@Override
	protected void onPreExecute() {
		smsMassivo.getConfig().markAsRunning();
		Log.i(TAG, "Preparando inicio do processamento de SMS...");
		sentSmsBroadcastReceivedIdCache.clear();
		receiverSmsBroadcastReceivedIdCache.clear();
		Log.d(TAG, "Carregando di�logo de progresso...");
		progressDialog = loadProgressDialog();

		Log.d(TAG, "Registrando callback de envio e entrega de SMS...");
		registerSentSMSContextListener();
		registerReceiverSMSContextListener();

		Log.d(TAG, "Prepara��o inicial conclu�da");
	}

	private ProgressDialog loadProgressDialog() {
		SMSMassivo smsMassivo = EnvironmentAccessor.getInstance().get(SMSMassivo.class);
		ProgressDialog progressDialog = new ProgressDialog(ContextHelper.getBaseContext(smsMassivo));
		progressDialog.setMessage(smsMassivo.getString(R.string.progressDialogSendingSms));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, smsMassivo.getString(R.string.cancelBtn), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				SMSSender.this.cancel(true);
			}
		});
		progressDialog.show();
		return progressDialog;
	}

	@Override
	protected Void doInBackground(SMSSenderParams... params) {
		param = params[0];
		Log.i(TAG, "Par�metros recebidos: " + param);
		progressDialog.setMax(param.getTotalOfMessages());
		String mySimCard = EnvironmentAccessor.getInstance().getSimCardNumber(smsMassivo);
		dailyController = DailyController.getInstance(smsMassivo, mySimCard, param.getPhone());

		if (dailyController.getTotalSent() > param.getTotalOfMessages()) {
			showNotification(smsMassivo, String.format(smsMassivo.getString(R.string.progressDialogMessagesHasBeenSent), dailyController.getTotalSent()));
			cancel(true);
			return null;
		}

		showNotification(smsMassivo, smsMassivo.getText(R.string.sendSmsServiceStarted).toString());

		sendSmsInterator();

		while (mustSendMoreSms()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}

		return null;
	}

	private boolean mustSendMoreSms() {
		boolean hasMore = counter < param.getTotalOfMessages();

		if (hasMore) {
			if (isCancelled()) {
				showNotification(smsMassivo, String.format(smsMassivo.getString(R.string.notificationSmsSenderCanceled), counter, param.getPhone()));
				return false;
			}
			if (dailyController.getTotalOfFailures() >= param.getFailureTolerance()) {
				showNotification(smsMassivo, String.format(smsMassivo.getString(R.string.notificationSmsSenderAborted), counter, param.getPhone(), dailyController.getTotalOfFailures()));
				return false;
			}
		}
		return hasMore;
	}

	private boolean sendSmsInterator() {
		synchronized (dailyController) {
			counter = dailyController.getTotalSent();
			String message = String.format(smsMassivo.getString(R.string.notificationSendingSms), counter, param.getPhone());
			publishProgress(new SMSSenderProgress(counter, message));
			showNotification(smsMassivo, message);
	
			String text = String.format(smsMassivo.getString(R.string.smsMessagePattern), counter + 1, new Date());
			sendSMS(param, counter, text);
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(SMSSenderProgress... values) {
		Log.d(TAG, "Atualizado progresso para " + Arrays.toString(values));
		if (progressDialog != null) {
			SMSSenderProgress progress = values[0];

			progressDialog.setMessage(progress.getMessage());
			progressDialog.setProgress(progress.getProgress());
		}
	}

	@Override
	protected void onPostExecute(Void result) {
		String finishMessage = String.format(smsMassivo.getString(R.string.notificationSmsSentFinished), param.getTotalOfMessages(), param.getPhone());
		publishProgress(new SMSSenderProgress(param.getTotalOfMessages(), finishMessage));
		showNotification(smsMassivo, finishMessage);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		Log.i(TAG, "Fechando tela de progresso...");
		progressDialog.dismiss();
		progressDialog = null;
		Log.d(TAG, "Fechando conex�o com banco de dados...");
		dailyController.close();
		Log.d(TAG, "Removendo registro de callback de envio e entrega de SMS...");
		unregisterReceiverSMSContextListener();
		unregisterSentSMSContextListener();
		Log.i(TAG, "Alertando tarefa finalizada via som...");
		EnvironmentAccessor.getInstance().playRingtone(smsMassivo);
		super.onPostExecute(result);
		Log.i(TAG, "Tarefa finalizada");
		smsMassivo.getConfig().markAsStopped();
	}
	
	private void showNotification(Context context, String message) {
		Log.i(TAG, "Ativando notifica��es...");

		Notification notification = new Notification(android.R.drawable.ic_menu_upload, message, System.currentTimeMillis());

		Log.i(TAG, "Associando notifica��es a aplica��o...");
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 2, new Intent(context, SMSMassivo.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), message, contentIntent);

		// Send the notification.
		notificationManager.notify(NOTIFICATION, notification);

		Log.i(TAG, "Notifica��es ativadas");
	}

	private void sendSMS(final SMSSenderParams param, final int id, final String message) {
		Log.i(TAG, String.format("Enviando SMS %s para %s: '%s'", id, param.getPhone(), message));

		Intent sentSmsIntent = new Intent(SENT_SMS_INTENT);
		sentSmsIntent.putExtra("id", id);
		PendingIntent sentPI = PendingIntent.getBroadcast(smsMassivo, 0, sentSmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent deliveredSmsIntent = new Intent(DELIVERED_SMS_INTENT);
		deliveredSmsIntent.putExtra("id", id);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(smsMassivo, 1, deliveredSmsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		SmsManager sms = SmsManager.getDefault();
		try {
			sms.sendTextMessage(param.getPhone(), null, message, sentPI, deliveredPI);
			dailyController.incTotalSent();
		} catch (IllegalArgumentException e) {
			String text = String.format("%s [destinationAddress: %s, text: %s]", param.getPhone(), message);
			Log.e(TAG, text, e);
			Toast.makeText(smsMassivo, text, Toast.LENGTH_LONG).show();
		}
	}

	private void unregisterReceiverSMSContextListener() {
		if (deliveryBroadcastReceiver != null) {
			smsMassivo.unregisterReceiver(deliveryBroadcastReceiver);
			deliveryBroadcastReceiver = null;
		}
	}

	private void registerReceiverSMSContextListener() {
		if (deliveryBroadcastReceiver == null) {
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
						Log.e(TAG, "SMS n�o entregue");
						Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
						dailyController.incCanceled();
						break;
					}
				}
			};
		}
		smsMassivo.registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED_SMS_INTENT));
	}

	private void unregisterSentSMSContextListener() {
		if (sentBroadcastReceiver != null) {
			smsMassivo.unregisterReceiver(sentBroadcastReceiver);
			sentBroadcastReceiver = null;
		}
	}

	private void registerSentSMSContextListener() {
		if (sentBroadcastReceiver == null) {
			sentBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					int smsId = intent.getExtras().getInt("id");
					if (sentSmsBroadcastReceivedIdCache.contains(smsId)) {
						return;
					} else {
						sentSmsBroadcastReceivedIdCache.add(smsId);
					}
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
					counter++;
					if (mustSendMoreSms()) {
						sendSmsInterator();
					}
				}
			};
		}
		smsMassivo.registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT_SMS_INTENT));
	}

}
