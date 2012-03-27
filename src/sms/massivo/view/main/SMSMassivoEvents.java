package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.task.sender.SMSSender;
import sms.massivo.task.sender.SMSSenderParams;
import sms.massivo.view.report.Report;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuItem.OnMenuItemClickListener;
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
		Log.i(TAG, "Acionado comando para enviar todos os SMS");
		int totalOfMessages = smsMassivo.getTotalOfMessagesToSend();
		int failureTolerance = smsMassivo.getTotalFailureTolerance();
		String phone = smsMassivo.getPhone();

		SMSSenderParams params = new SMSSenderParams();
		params.setPhone(phone);
		params.setTotalOfMessages(totalOfMessages);
		params.setFailureTolerance(failureTolerance);

		if(!smsMassivo.getConfig().isRunning()){
			SMSSender sender = new SMSSender(smsMassivo);
			sender.execute(params);
		}

		Log.i(TAG, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), totalOfMessages));
		Toast.makeText(smsMassivo, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), totalOfMessages), Toast.LENGTH_SHORT).show();
	}

	private void onClickLastReport() {
		Log.i(TAG, "Acionado comando para visualizar �ltimo relat�rio de envio");

		Intent intent = new Intent(smsMassivo, Report.class);
		smsMassivo.startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.main.sendAllBtn:
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
		}
		return true; //prevents bubble effect
	}
}
