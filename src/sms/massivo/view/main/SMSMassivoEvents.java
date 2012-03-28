package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.task.sender.SMSSender;
import sms.massivo.task.sender.SMSSenderParams;
import sms.massivo.view.report.Report;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SMSMassivoEvents implements OnClickListener, OnMenuItemClickListener {
	public final static String TAG = "SMSMassivoEvents";

	private final SMSMassivo smsMassivo;

	public SMSMassivoEvents(SMSMassivo smsMassivo) {
		Log.i(TAG, "Associado eventos a SMSMassivo");
		this.smsMassivo = smsMassivo;
	}

	private void onClickSendAll() {
		if (smsMassivo.getConfig().isRunning())
			return;
		Log.i(TAG, "Acionado comando para enviar todos os SMS");
		int totalOfMessages = smsMassivo.getTotalOfMessagesToSend();
		int failureTolerance = smsMassivo.getTotalFailureTolerance();
		String phone = smsMassivo.getPhone();

		SMSSenderParams params = new SMSSenderParams();
		params.setPhone(phone);
		params.setTotalOfMessages(totalOfMessages);
		params.setFailureTolerance(failureTolerance);

		SMSSender sender = new SMSSender(smsMassivo);
		sender.execute(params);

		Log.i(TAG, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), totalOfMessages));
		Toast.makeText(smsMassivo, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), totalOfMessages), Toast.LENGTH_SHORT).show();

		smsMassivo.updateScreen();
	}

	private void onClickLastReport() {
		Log.i(TAG, "Acionado comando para visualizar œltimo relat—rio de envio");

		Intent intent = new Intent(smsMassivo, Report.class);
		smsMassivo.startActivity(intent);
	}

	private void onClickCancel() {
		Log.i(TAG, "Acionado comando para cancelar envio de SMS");

		smsMassivo.getConfig().markAsStoppedByUser();
		smsMassivo.updateScreen();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.main.sendAllBtn:
			if (smsMassivo.getConfig().isRunning())
				onClickCancel();
			else
				onClickSendAll();
			break;
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.menu.sendSms:
			onClickSendAll();
			break;
		case R.menu.lastReport:
			onClickLastReport();
			break;
		case R.menu.cancelSendSms:
			onClickCancel();
		}
		return true; // prevents bubble effect
	}
}
