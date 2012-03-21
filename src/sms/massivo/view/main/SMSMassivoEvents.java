package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.task.sender.SMSSender;
import sms.massivo.task.sender.SMSSenderParams;
import sms.massivo.view.report.Report;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SMSMassivoEvents implements OnClickListener {
	public final static String TAG = "SMSMassivoEvents";

	private final SMSMassivo smsMassivo;

	public SMSMassivoEvents(SMSMassivo smsMassivo) {
		Log.i(TAG, "Associado eventos a SMSMassivo");
		this.smsMassivo = smsMassivo;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.main.sendAllBtn:
			onClickSendAll(v);
			break;
		case R.main.lastReportBtn:
			onClickLastReport(v);
			break;
		}
	}

	private void onClickSendAll(View v) {
		Log.i(TAG, "Acionado comando para enviar todos os SMS");
		int totalOfMessages = smsMassivo.getTotalOfMessagesToSend();
		int delay = smsMassivo.getDelayBetweenMessages();

		SMSSenderParams params = new SMSSenderParams();
		params.setPhone("+551160470001");
		params.setTotalOfMessages(totalOfMessages);
		params.setDelay(delay);

		new SMSSender(smsMassivo).execute(params);

		Log.i(TAG, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), totalOfMessages));
		Toast.makeText(smsMassivo, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), totalOfMessages), Toast.LENGTH_SHORT).show();
	}

	private void onClickLastReport(View v) {
		Log.i(TAG, "Acionado comando para visualizar œltimo relat—rio de envio");

		Intent intent = new Intent(smsMassivo, Report.class);
		smsMassivo.startActivity(intent);
	}
}
