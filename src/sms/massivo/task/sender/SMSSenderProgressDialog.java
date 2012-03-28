package sms.massivo.task.sender;

import sms.massivo.R;
import sms.massivo.view.main.SMSMassivo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

public class SMSSenderProgressDialog implements DialogInterface.OnClickListener {
	
	private ProgressDialog progressDialog;
	private final Context context;
	private final SMSSender smsSender;
	private final SMSMassivo smsMassivo;

	public SMSSenderProgressDialog(Context context, SMSSender smsSender, SMSMassivo smsMassivo){
		this.context = context;
		this.smsSender = smsSender;
		this.smsMassivo = smsMassivo;
		
		progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(R.string.app_name);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		setOnClickListener(ProgressDialog.BUTTON_NEGATIVE, R.string.cancelBtn, this);
		progressDialog.show();
	}
	
	public void setMax(int max){
		progressDialog.setMax(max);
	}
	
	public void update(SMSSenderProgress progress){
		progressDialog.setMessage(progress.getMessage());
		progressDialog.setProgress(progress.getProgress());
	}
	
	public void show(){
		progressDialog.show();
	}
	
	public void dismiss(){
		progressDialog.dismiss();
	}
	
	public void setOnClickListener(int witchButton, int buttonTitleId, DialogInterface.OnClickListener listener){
		progressDialog.setButton(witchButton, context.getString(buttonTitleId), listener);
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		smsSender.cancel(true);
		smsMassivo.getConfig().markAsStoppedByUser();
		smsMassivo.updateScreen();
	}
}
