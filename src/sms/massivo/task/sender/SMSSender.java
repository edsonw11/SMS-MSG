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
	private int NOTIFICATION = R.notification.smsSenderNotificationId;

	private NotificationManager notificationManager;
	private ProgressDialog progressDialog;
	private final Context context;
	private HistoryDAO historyDao;
	private DailyReport dailyReport;

	public SMSSender(Context context) {
		super();
		this.context = context;
		historyDao = new HistoryDAO(context);
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected void onPreExecute() {
		Log.i(TAG, "Preparando inicio do processamento de SMS...");
		Log.d(TAG, "Carregando di‡logo de progresso...");
		progressDialog = loadProgressDialog();

		Log.d(TAG, "Prepara‹o inicial conclu’da");
	}

	private ProgressDialog loadProgressDialog() {
		ProgressDialog progressDialog = new ProgressDialog(EnvironmentAccessor.getInstance().get(SMSMassivo.class));
		progressDialog.setMessage("Enviando Mensagens");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {

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
		Log.i(TAG, "Par‰metros recebidos: " + param);
		progressDialog.setMax(param.getTotalOfMessages());

		String myPhoneNumber = EnvironmentAccessor.getMyPhoneNumber(context);
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), myPhoneNumber, param.getPhone()));

		if (dailyReport.getTotalSent() > param.getTotalOfMessages()) {
			showNotification(context, "J‡ enviou a quantidade especificada hoje. Enviados: " + dailyReport.getTotalSent());
			cancel(true);
			return null;
		}

		showNotification(context, context.getText(R.string.sendSmsServiceStarted).toString());

		for (int i = dailyReport.getTotalSent(); i < param.getTotalOfMessages(); i++) {
			if (isCancelled()) {
				showNotification(context, String.format("Envio Cancelado. Enviou %d SMSs para %s", i, param.getPhone()));
				return null;
			}

			String message = String.format("Enviando SMS %d para %s", i + 1, param.getPhone());
			publishProgress(new SMSSenderProgress(i, message));
			showNotification(context, message);

			String text = String.format("%1$s %2$tD %2$tR enviada via android.", i + 1, new Date());
			sendSMS(param, text);
			historyDao.update(dailyReport);
			try {
				Thread.sleep(param.getDelay());
			} catch (InterruptedException e) {
			}
		}

		String finishMessage = String.format("Finalizado envio de %s mensagens para %s", param.getTotalOfMessages(), param.getPhone());
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
		super.onPostExecute(result);
		Log.i(TAG, "Tarefa finalizada");
	}

	private void showNotification(Context context, String message) {
		Log.i(TAG, "Ativando notifica›es...");

		Notification notification = new Notification(android.R.drawable.ic_menu_upload, message, System.currentTimeMillis());

		Log.i(TAG, "Associando notifica›es a aplica‹o...");
		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, SMSMassivo.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(context, context.getText(R.string.app_name), message, contentIntent);

		// Send the notification.
		notificationManager.notify(NOTIFICATION, notification);

		Log.i(TAG, "Notifica›es ativadas");
	}

	private void sendSMS(final SMSSenderParams param, final String message) {
		Log.i(TAG, String.format("Enviando SMS para %s: '%s'", param.getPhone(), message));

		String SENT = "SMS_SENT";
		String DELIVERED = "SMS_DELIVERED";

		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);

		// ---when the SMS has been sent---
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					// ContentValues values = new ContentValues();
					// values.put("address", phoneNumber);
					// values.put("body", message);
					//
					// getContentResolver().insert(Uri.parse("content://sms/sent"), values);
					// Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
					Log.i(TAG, "SMS enviado com sucesso");
					dailyReport.incSendSuccessfully();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.e(TAG, "Falha generica no envio de SMS");
					Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT).show();
					dailyReport.incGenericFailure();
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					Log.e(TAG, "Sem servio de envio de SMS");
					Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
					dailyReport.incNoService();
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					Log.e(TAG, "PDU n‹o encontrada");
					Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
					dailyReport.incNullPDU();
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					Log.e(TAG, "R‡dio desligado");
					Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
					dailyReport.incRadioOff();
					break;
				}
			}
		}, new IntentFilter(SENT));

		// ---when the SMS has been delivered---
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Log.i(TAG, "SMS entregue");
					dailyReport.incDelivery();
					break;
				case Activity.RESULT_CANCELED:
					Log.e(TAG, "SMS n‹o entregue");
					Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
					dailyReport.incCanceled();
					break;
				}
			}
		}, new IntentFilter(DELIVERED));

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

}
