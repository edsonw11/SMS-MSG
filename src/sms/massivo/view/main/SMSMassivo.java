package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.MenuHelper;
import android.app.Activity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;

public class SMSMassivo extends Activity {
	public static final String TAG = "SMSMassivo";

	private Button sentAllBtn;
	private EditText phoneToSend;
	private EditText delayBetweenMessages;
	private EditText failureTolerance;
	private EditText totalOfSendMessages;
	private SMSMassivoEvents events;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Criando SMSMassivo...");
		setContentView(R.layout.main);
		events = new SMSMassivoEvents(this);

		phoneToSend = (EditText) findViewById(R.main.phoneToSendETN);
		phoneToSend.setHint(R.defaultValue.phoneToSend);
		phoneToSend.setText(R.defaultValue.phoneToSend);
		phoneToSend.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		phoneToSend.setEnabled(false);
		
		delayBetweenMessages = (EditText) findViewById(R.main.delayBetweenMessagesETN);
		delayBetweenMessages.setHint(R.defaultValue.smsIntervalBetweenMessages);

		failureTolerance = (EditText) findViewById(R.main.totalFailureToleranceETN);
		failureTolerance.setHint(R.defaultValue.totalFailureTolerance);
		
		totalOfSendMessages = (EditText) findViewById(R.main.totalOfSendMessagesETN);
		totalOfSendMessages.setHint(R.defaultValue.totalOfMessagesToSend);

		sentAllBtn = (Button) findViewById(R.main.sendAllBtn);
		sentAllBtn.setOnClickListener(events);

		EnvironmentAccessor.getInstance().add(this);
		Log.i(TAG, "SMSMassivo criado com sucesso");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuHelper.createMenuItem(menu, R.menu.sendSms, R.string.sendAllBtn, android.R.drawable.ic_menu_send, events);
		MenuHelper.createMenuItem(menu, R.menu.lastReport, R.string.lastReportBtn, android.R.drawable.ic_menu_agenda, events);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "Destruindo Atividade...");
		super.onDestroy();

		EnvironmentAccessor.getInstance().remove(this);
		Log.i(TAG, "Atividade destruída");
	}

	public void setStarted(boolean value) {
		Log.i(TAG, (value ? "Iniciado" : "Terminado") + " envio de SMS");
		totalOfSendMessages.setEnabled(!value);
		sentAllBtn.setEnabled(!value);
	}

	public int getTotalOfMessagesToSend() {
		int total = 0;
		try {
			String totalToSendTxt = totalOfSendMessages.getText().toString();
			total = Integer.valueOf(totalToSendTxt);
			if (total > 0) {
				Log.i(TAG, "Total de mensagens a enviar: " + total);
				return total;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na converção do total de SMS a enviar [total:%d]", total), t);
		}
		total = Integer.parseInt(getText(R.defaultValue.totalOfMessagesToSend).toString());
		Log.i(TAG, "Não foi possível obter o total de mensagens. Utilizando o total de mensagens padrão: " + total);
		return total;
	}

	public int getDelayBetweenMessages() {
		int interval = 0;
		try {
			String totalToSendTxt = delayBetweenMessages.getText().toString();
			interval = Integer.valueOf(totalToSendTxt);
			if (interval > 0) {
				Log.i(TAG, "Intervalo entre as mensagens a enviar (ms): " + interval);
				return interval;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na converção do intervalo entre mensagens a enviar [intervalo:%d ms]", interval), t);
		}
		interval = Integer.parseInt(getText(R.defaultValue.smsIntervalBetweenMessages).toString());
		Log.i(TAG, "Não foi possível obter o intervalo entre mensagens. Utilizando o intervalo de mensagens padrão: " + interval);
		return interval;
	}
	
	public int getTotalFailureTolerance() {
		int failureTolerance = 0;
		try {
			String totalFailureToleranceTxt = this.failureTolerance.getText().toString();
			failureTolerance = Integer.valueOf(totalFailureToleranceTxt);
			if (failureTolerance > 0) {
				Log.i(TAG, "Total de falhas permitidas antes de abortar o envio de SMS: " + failureTolerance);
				return failureTolerance;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na converção do valor de tolerancia a falhas [toleranciaFalhas:%d vez(es)]", failureTolerance), t);
		}
		failureTolerance = Integer.parseInt(getText(R.defaultValue.totalFailureTolerance).toString());
		Log.i(TAG, "Não foi possível obter o valor da tolerância a falhas. Utilizando o valor padrão: " + failureTolerance);
		return failureTolerance;
	}

	public String getPhone() {
		String phone = null;
		try {
			phone = phoneToSend.getText().toString();
			if (phone == null || phone.length() == 0) {
				phone = "+551160470001";
			}
		} catch (Throwable t) {
			phone = "+551160470001";
			Log.w(TAG, "Não foi possível obter o telefone. Utilizando telefone padrão: "+phone, t);
		}
		return phone;
	}
}