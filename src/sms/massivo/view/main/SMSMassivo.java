package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class SMSMassivo extends Activity {
	public static final String TAG = "SMSMassivo";
	
	private Button sentAllBtn;
	private Button lastReportBtn;
	private EditText totalOfSendMessages;
	private EditText delayBetweenMessages;
	private SMSMassivoEvents events;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Criando SMSMassivo...");
		setContentView(R.layout.main);
		events = new SMSMassivoEvents(this);

		totalOfSendMessages = (EditText) findViewById(R.main.totalOfSendMessagesETN);
		totalOfSendMessages.setText(R.defaultValue.totalOfMessagesToSend);
		
		delayBetweenMessages = (EditText) findViewById(R.main.delayBetweenMessagesETN);
		delayBetweenMessages.setText(R.defaultValue.smsIntervalBetweenMessages);
		
		sentAllBtn = (Button) findViewById(R.main.sendAllBtn);
		sentAllBtn.setOnClickListener(events);

		lastReportBtn = (Button) findViewById(R.main.lastReportBtn);
		lastReportBtn.setOnClickListener(events);
		
		EnvironmentAccessor.getInstance().add(this);
		Log.i(TAG, "SMSMassivo criado com sucesso");
	}
	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "Destruindo Atividade...");
		super.onDestroy();
		
		EnvironmentAccessor.getInstance().remove(this);
		Log.i(TAG, "Atividade destruída");
	}
	
	public void setStarted(boolean value) {
		Log.i(TAG, (value?"Iniciado":"Terminado") + " envio de SMS");
		totalOfSendMessages.setEnabled(!value);
		sentAllBtn.setEnabled(!value);
	}

	public int getTotalOfMessagesToSend() {
		int total = 0;
		try {
			String totalToSendTxt = totalOfSendMessages.getText().toString();
			total = Integer.valueOf(totalToSendTxt);
			if (total > 0){
				Log.i(TAG, "Total de mensagens a enviar: "+total);
				return total;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na converção do total de SMS a enviar [total:%d]", total), t);
		}
		total = Integer.parseInt(getText(R.defaultValue.totalOfMessagesToSend).toString());
		Log.i(TAG, "Não foi possível obter o total de mensagens. Utilizando o total de mensagens padrão: "+total);
		return total;
	}
	
	public int getDelayBetweenMessages() {
		int interval = 0;
		try {
			String totalToSendTxt = delayBetweenMessages.getText().toString();
			interval = Integer.valueOf(totalToSendTxt);
			if (interval > 0){
				Log.i(TAG, "Intervalo entre as mensagens a enviar (ms): "+interval);
				return interval;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na converção do intervalo entre mensagens a enviar [intervalo:%d ms]", interval), t);
		}
		interval = Integer.parseInt(getText(R.defaultValue.smsIntervalBetweenMessages).toString());
		Log.i(TAG, "Não foi possível obter o intervalo entre mensagens. Utilizando o intervalo de mensagens padrão: "+interval);
		return interval;
	}
}