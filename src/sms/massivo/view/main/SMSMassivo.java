package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SMSMassivo extends Activity {
	public static final String TAG = "SMSMassivo";
	
	private Button sentAllBtn;
	private Button lastReportBtn;
	private EditText totalOfSendMessages;
	private TextView sentMessages;

	private SMSMassivoEvents events;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Criando SMSMassivo...");
		setContentView(R.layout.main);
		events = new SMSMassivoEvents(this);

		totalOfSendMessages = (EditText) findViewById(R.main.totalOfSendMessagesETN);
		sentAllBtn = (Button) findViewById(R.main.sendAllBtn);
		lastReportBtn = (Button) findViewById(R.main.lastReportBtn);

		sentAllBtn.setOnClickListener(events);
		lastReportBtn.setOnClickListener(events);
		
		EnvironmentAccessor.getInstance().add(this);
		Log.i(TAG, "SMSMassivo criado com sucesso");
	}
	
	@Override
	protected void onDestroy() {
		Log.i(TAG, "Destruindo Atividade...");
		super.onDestroy();
		
		EnvironmentAccessor.getInstance().remove(this);
		Log.i(TAG, "Atividade destru�da");
	}
	
	public void setStarted(boolean value) {
		Log.i(TAG, (value?"Iniciado":"Terminado") + " envio de SMS");
		totalOfSendMessages.setEnabled(!value);
		sentAllBtn.setEnabled(!value);
		
		//TODO atualizar UI
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
			Log.w(TAG, String.format("Erro na conver��o do total de SMS a enviar [total:%d]", total), t);
		}
		total = Integer.parseInt(getText(R.defaultValue.defaultTotalOfMessagesToSend).toString());
		Log.i(TAG, "N�o foi poss�vel obter o total de mensagens. Utilizando o total de mensagens padr�o: "+total);
		return total;
	}
	
	public void incTotalOfSentMessages(){
		Log.d(TAG, "Total de SMS enviado incrementado no painel da tela");
		String value = sentMessages.getText().toString();
		if(value == null || value.length() == 0){
			value = "0";
		}
		
		sentMessages.setText(Integer.valueOf(value));
	}
}