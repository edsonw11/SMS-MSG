package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.MenuHelper;
import sms.massivo.helper.db.controller.ConfigController;
import sms.massivo.helper.db.controller.DailyController;
import sms.massivo.task.sender.SMSSenderManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class SMSMassivo extends Activity {
	public static final String TAG = "SMSMassivo";

	private Button sentAllBtn;
	private EditText phoneToSend;
	private EditText failureTolerance;
	private SeekBar totalOfSlaves;
	private TextView totalOfSlavesLbl;
	private EditText totalOfSendMessages;
	private SMSMassivoEvents events;
	private ConfigController config;
	private SMSSenderManager manager;
	private ProgressBar smsSent;

	private DailyController dailyController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Criando SMSMassivo...");
		setContentView(R.layout.main);

		config = ConfigController.getInstance(this);
		events = new SMSMassivoEvents(this);
		String simCard = EnvironmentAccessor.getInstance().getSimCardNumber(this);

		if (simCard == null) {
			messageDialogToExit();
		}

		phoneToSend = (EditText) findViewById(R.main.phoneToSendETN);
		phoneToSend.setHint(R.defaultValue.phoneToSend);
		phoneToSend.setText(config.getPhone());
		phoneToSend.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
		phoneToSend.setEnabled(false);

		failureTolerance = (EditText) findViewById(R.main.totalFailureToleranceETN);
		failureTolerance.setHint(R.defaultValue.totalFailureTolerance);

		totalOfSlaves = (SeekBar) findViewById(R.main.totalOfSlavesSKB);
		totalOfSlaves.setProgress(getResources().getInteger(R.defaultValue.totalOfSlaves));
		totalOfSlaves.setOnSeekBarChangeListener(events);

		totalOfSlavesLbl = (TextView) findViewById(R.main.totalOfSlavesLbl);

		totalOfSendMessages = (EditText) findViewById(R.main.totalOfSendMessagesETN);
		totalOfSendMessages.setHint(R.defaultValue.totalOfMessagesToSend);
		totalOfSendMessages.setOnFocusChangeListener(events);

		sentAllBtn = (Button) findViewById(R.main.sendAllBtn);
		sentAllBtn.setOnClickListener(events);

		smsSent = (ProgressBar) findViewById(R.main.smsSentPRB);
		smsSent.setMax(config.getTotalOfMessagesToSend());
		if (simCard != null) {
			dailyController = DailyController.getInstance(this, simCard, config.getPhone());
			smsSent.setProgress(dailyController.getTotalSent());
		}

		manager = new SMSSenderManager(this);

		EnvironmentAccessor.getInstance().add(this);

		config.markAsStopped();
		Log.i(TAG, "SMSMassivo criado com sucesso");
	}

	private void messageDialogToExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("N‹o h‡ SIM. Insira um SIM Card e tente novamente").setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				SMSMassivo.this.finish();
			}
		});
		builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuHelper.createMenuItem(menu, R.menuGroup.task, R.menu.sendSms, R.string.sendAllBtn, android.R.drawable.ic_menu_send, events);
		MenuHelper.createMenuItem(menu, R.menuGroup.main, R.menu.lastReport, R.string.lastReportBtn, android.R.drawable.ic_menu_agenda, events);
		MenuHelper.createMenuItem(menu, R.menuGroup.untask, R.menu.cancelSendSms, R.string.cancelBtn, android.R.drawable.ic_menu_close_clear_cancel, events);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean enabled = !config.isRunning();

		menu.setGroupEnabled(R.menuGroup.task, enabled);
		menu.setGroupEnabled(R.menuGroup.untask, !enabled);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateScreen();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Destruindo Atividade...");
		super.onDestroy();

		if (dailyController != null)
			dailyController.close();

		EnvironmentAccessor.getInstance().remove(this);
		Log.i(TAG, "Atividade destru’da");
	}

	public void updateScreen() {
		boolean enabled = !config.isRunning();

		totalOfSendMessages.setEnabled(enabled);
		failureTolerance.setEnabled(enabled);
		totalOfSlaves.setEnabled(enabled);

		if (enabled)
			sentAllBtn.setText(R.string.sendAllBtn);
		else {
			sentAllBtn.setText(R.string.cancelAllBtn);
			sentAllBtn.requestFocus();
			sentAllBtn.requestFocusFromTouch();
		}

		totalOfSlavesLbl.setText(getText(R.string.totalOfSlaves) + ": " + totalOfSlaves.getProgress());

		smsSent.setMax(getTotalOfMessagesToSend());
		smsSent.setProgress(0);
		smsSent.setProgress(dailyController == null ? 0 : dailyController.getTotalSent());
	}

	public int getTotalOfMessagesToSend() {
		int total = 0;
		try {
			String totalToSendTxt = totalOfSendMessages.getText().toString();
			total = Integer.valueOf(totalToSendTxt);
			if (total > 0) {
				Log.d(TAG, "Total de mensagens a enviar: " + total);
				return total;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na conver‹o do total de SMS a enviar [total:%d]", total), t);
		}
		total = config.getTotalOfMessagesToSend();
		Log.d(TAG, "N‹o foi poss’vel obter o total de mensagens. Utilizando o total de mensagens padr‹o: " + total);
		return total;
	}

	public int getTotalFailureTolerance() {
		int failureTolerance = 0;
		try {
			String totalFailureToleranceTxt = this.failureTolerance.getText().toString();
			failureTolerance = Integer.valueOf(totalFailureToleranceTxt);
			if (failureTolerance > 0) {
				Log.d(TAG, "Total de falhas permitidas antes de abortar o envio de SMS: " + failureTolerance);
				return failureTolerance;
			}
		} catch (Throwable t) {
			Log.w(TAG, String.format("Erro na conver‹o do valor de tolerancia a falhas [toleranciaFalhas:%d vez(es)]", failureTolerance), t);
		}
		failureTolerance = config.getFailureTolerance();
		Log.d(TAG, "N‹o foi poss’vel obter o valor da toler‰ncia a falhas. Utilizando o valor padr‹o: " + failureTolerance);
		return failureTolerance;
	}

	public int getTotalOfSlaves() {
		int totalOfSlaves = 0;
		totalOfSlaves = this.totalOfSlaves.getProgress();
		if (totalOfSlaves > 0) {
			Log.d(TAG, "Total de processos para o envio de SMS: " + totalOfSlaves);
			return totalOfSlaves;
		}
		totalOfSlaves = config.getTotalOfSlaves();
		Log.d(TAG, "N‹o foi poss’vel obter o total de processos. Utilizando o valor padr‹o: " + totalOfSlaves);
		return totalOfSlaves;
	}

	public String getPhone() {
		String phone = null;
		try {
			phone = phoneToSend.getText().toString();
			if (phone == null || phone.length() == 0) {
				phone = "+551160470001";
			}
		} catch (Throwable t) {
			phone = config.getPhone();
			Log.w(TAG, "N‹o foi poss’vel obter o telefone. Utilizando telefone padr‹o: " + phone, t);
		}
		return phone;
	}

	public ConfigController getConfig() {
		return config;
	}

	public SMSSenderManager getManager() {
		return manager;
	}
}