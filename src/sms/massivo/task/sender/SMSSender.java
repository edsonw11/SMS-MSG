package sms.massivo.task.sender;

import java.util.Arrays;
import java.util.Date;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.db.bean.DailyReport;
import sms.massivo.helper.db.dao.HistoryDAO;
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
	private final Context context;
	private HistoryDAO historyDao;
	private DailyReport dailyReport;
	private BroadcastReceiver deliveryBroadcastReceiver;
	private BroadcastReceiver sentBroadcastReceiver;

	public SMSSender(Context context) {
		super();
		this.context = context;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		Log.i(TAG, "Preparando inicio do processamento de SMS...");
		Log.d(TAG, "Carregando diálogo de progresso...");
		progressDialog = loadProgressDialog();

		Log.d(TAG, "Abrindo conexão com banco de dados...");
		historyDao = new HistoryDAO(context);

		Log.d(TAG, "Registrando callback de envio e entrega de SMS...");
		registerSentSMSContextListener(SENT_SMS_INTENT);
		registerReceiverSMSContextListener(DELIVERED_SMS_INTENT);

		Log.d(TAG, "Preparação inicial concluída");
	}

	private ProgressDialog loadProgressDialog() {
		ProgressDialog progressDialog = new ProgressDialog(EnvironmentAccessor.getInstance().get(SMSMassivo.class));
		progressDialog.setMessage(context.getString(R.string.progressDialogSendingSms));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.cancelBtn), new DialogInterface.OnClickListener() {

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
		SMSSenderParams param = params[0];
		Log.i(TAG, "Parâmetros recebidos: " + param);
		progressDialog.setMax(param.getTotalOfMessages());

		String myPhoneNumber = EnvironmentAccessor.getInstance().getSimCardNumber(context);
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), myPhoneNumber, param.getPhone()));

		if (dailyReport.getTotalSent() > param.getTotalOfMessages()) {
			showNotification(context, String.format(context.getString(R.string.progressDialogMessagesHasBeenSent), dailyReport.getTotalSent()));
			cancel(true);
			return null;
		}

		showNotification(context, context.getText(R.string.sendSmsServiceStarted).toString());

		for (int i = dailyReport.getTotalSent(); i < param.getTotalOfMessages(); i++) {
			if (isCancelled()) {
				showNotification(context, String.format(context.getString(R.string.notificationSmsSenderCanceled), i, param.getPhone()));
				return null;
			}
			if (dailyReport.getTotalOfFailures() >= param.getFailureTolerance()) {
				showNotification(context, String.format(context.getString(R.string.notificationSmsSenderAborted), i, param.getPhone(), dailyReport.getTotalOfFailures()));
				return null;
			}

			String message = String.format(context.getString(R.string.notificationSendingSms), i + 1, param.getPhone());
			publishProgress(new SMSSenderProgress(i, message));
			showNotification(context, message);

			String text = String.format(context.getString(R.string.smsMessagePattern), i + 1, new Date());
			sendSMS(param, text);
			historyDao.update(dailyReport);
			try {
				Thread.sleep(param.getDelay());
			} catch (InterruptedException e) {
			}
		}

		String finishMessage = String.format(context.getString(R.string.notificationSmsSentFinished), param.getTotalOfMessages(), param.getPhone());
		publishProgress(new SMSSenderProgress(param.getTotalOfMessages(), finishMessage));
		showNotification(context, finishMessage);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		return null;
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
		Log.i(TAG, "Persistindo dados...");
		historyDao.update(dailyReport);
		Log.i(TAG, "Fechando tela de progresso...");
		progressDialog.dismiss();
		progressDialog = null;
		Log.d(TAG, "Removendo registro de callback de envio e entrega de SMS...");
		unregisterReceiverSMSContextListener();
		unregisterSentSMSContextListener();
		Log.d(TAG, "Fechando conexão com banco de dados...");
		historyDao.close();
		Log.i(TAG, "Alertando tarefa finalizada via som...");
		EnvironmentAccessor.getInstance().playRingtone(context);
		super.onPostExecute(result);
		Log.i(TAG, "Tarefa finalizada");
	}

	private void showNotification(Context context, String message) {
		Log.i(TAG, "Ativando notificações...");

		Notification notification = new Notification(android.R.drawable.ic_menu_upload, message, System.currentTimeMillis());

		Log.i(TAG, "Associando notificações a aplicação...");
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, SMSMassivo.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), message, contentIntent);

		// Send the notification.
		notificationManager.notify(NOTIFICATION, notification);

		Log.i(TAG, "Notificações ativadas");
	}

	private void sendSMS(final SMSSenderParams param, final String message) {
		Log.i(TAG, String.format("Enviando SMS para %s: '%s'", param.getPhone(), message));

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT_SMS_INTENT), PendingIntent.FLAG_ONE_SHOT);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED_SMS_INTENT), PendingIntent.FLAG_ONE_SHOT);

		SmsManager sms = SmsManager.getDefault();
		try {
			sms.sendTextMessage(param.getPhone(), null, message, sentPI, deliveredPI);
			dailyReport.incTotalSent();
		} catch (IllegalArgumentException e) {
			String text = String.format("%s [destinationAddress: %s, text: %s]", param.getPhone(), message);
			Log.e(TAG, text, e);
			Toast.makeText(context, text, Toast.LENGTH_LONG).show();
		}
	}

	private void unregisterReceiverSMSContextListener() {
		if (deliveryBroadcastReceiver != null) {
			context.unregisterReceiver(deliveryBroadcastReceiver);
			deliveryBroadcastReceiver = null;
		}
	}

	private void registerReceiverSMSContextListener(String DELIVERED) {
		if (deliveryBroadcastReceiver == null) {
			deliveryBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Log.i(TAG, "SMS entregue");
						dailyReport.incDelivery();
						break;
					case Activity.RESULT_CANCELED:
						Log.e(TAG, "SMS não entregue");
						Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
						dailyReport.incCanceled();
						break;
					}
				}
			};
		}

		context.registerReceiver(deliveryBroadcastReceiver, new IntentFilter(DELIVERED));
	}

	private void unregisterSentSMSContextListener() {
		if (sentBroadcastReceiver != null) {
			context.unregisterReceiver(sentBroadcastReceiver);
			sentBroadcastReceiver = null;
		}
	}

	private void registerSentSMSContextListener(String SENT) {
		// ---when the SMS has been sent---
		if (sentBroadcastReceiver == null) {
			sentBroadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context arg0, Intent arg1) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Log.i(TAG, "SMS enviado com sucesso");
						dailyReport.incSendSuccessfully();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Log.e(TAG, "Falha generica no envio de SMS");
						Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
						dailyReport.incGenericFailure();
						break;
					case SmsManager.RESULT_ERROR_NO_SERVICE:
						Log.e(TAG, "Sem serviço de envio de SMS");
						Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
						dailyReport.incNoService();
						break;
					case SmsManager.RESULT_ERROR_NULL_PDU:
						Log.e(TAG, "PDU não encontrada");
						Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
						dailyReport.incNullPDU();
						break;
					case SmsManager.RESULT_ERROR_RADIO_OFF:
						Log.e(TAG, "Rádio desligado");
						Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
						dailyReport.incRadioOff();
						break;
					}
				}
			};
		}
		context.registerReceiver(sentBroadcastReceiver, new IntentFilter(SENT));
	}

}
