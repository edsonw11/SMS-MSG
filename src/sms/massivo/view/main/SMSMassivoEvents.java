package sms.massivo.view.main;

import sms.massivo.R;
import sms.massivo.helper.db.controller.ConfigController;
import sms.massivo.view.report.Report;
import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class SMSMassivoEvents implements OnClickListener, OnMenuItemClickListener, OnSeekBarChangeListener, OnFocusChangeListener {
	public final static String TAG = "SMSMassivoEvents";

	private final SMSMassivo smsMassivo;

	public SMSMassivoEvents(SMSMassivo smsMassivo) {
		Log.i(TAG, "Associado eventos a SMSMassivo");
		this.smsMassivo = smsMassivo;
	}

	private void onClickSendAll() {
		ConfigController config = smsMassivo.getConfig();
		if (config.isRunning())
			return;
		Log.i(TAG, "Acionado comando para enviar todos os SMS");
		config.setTotalOfMessagesToSend(smsMassivo.getTotalOfMessagesToSend());
		config.setFailureTolerance(smsMassivo.getTotalFailureTolerance());
		config.setTotalOfSlaves(smsMassivo.getTotalOfSlaves());
		config.setPhone(smsMassivo.getPhone());
		
		Log.i(TAG, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), config.getTotalOfMessagesToSend()));
		Toast.makeText(smsMassivo, String.format(smsMassivo.getString(R.string.smsMassivoEvents_log_smsSenderStarted), config.getTotalOfMessagesToSend()), Toast.LENGTH_SHORT).show();
		
		smsMassivo.getManager().execute();

		smsMassivo.updateScreen();
	}

	private void onClickLastReport() {
		Log.i(TAG, "Acionado comando para visualizar �ltimo relat�rio de envio");

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

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (fromUser && progress < 1)
			seekBar.setProgress(1);
		smsMassivo.updateScreen();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if(!hasFocus && v.getId() == R.main.totalOfSendMessagesETN){
			smsMassivo.updateScreen();
		}
	}
}
