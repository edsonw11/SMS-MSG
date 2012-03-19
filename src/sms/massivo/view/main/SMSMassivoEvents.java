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
		}
	}

	private void onClickSendAll(View v) {
		Log.i(TAG, "Acionado comando para enviar todos os SMS");
		int totalOfMessages = smsMassivo.getTotalOfMessagesToSend();

		SMSSenderParams params = new SMSSenderParams();
		params.setPhone("+551160470001");
		params.setTotalOfMessages(3);
		params.setDelay(1500);
		
		new SMSSender(smsMassivo).execute(params);
		
		Log.i(TAG, String.format("Envio de %s SMS's iniciado!", totalOfMessages));
		Toast.makeText(smsMassivo, String.format("Envio de %s SMS's iniciado!", totalOfMessages), Toast.LENGTH_SHORT).show();
	}

	private void onClickLastReport(View v) {
		Log.i(TAG, "Acionado comando para visualizar œltimo relat—rio de envio");

		Intent intent = new Intent(smsMassivo, Report.class);
		smsMassivo.startActivity(intent);
	}
	
}
