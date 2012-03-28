package sms.massivo.task.sender;

import java.util.ArrayList;
import java.util.List;

import sms.massivo.view.main.SMSMassivo;

public class SMSSenderManager {
	private List<SMSSender> senders = new ArrayList<SMSSender>();
	private final SMSMassivo smsMassivo;

	public SMSSenderManager(SMSMassivo smsMassivo, int totalOfSenders) {
		this.smsMassivo = smsMassivo;
		for (int i = 0; i < totalOfSenders; i++) {
			SMSSender sender = new SMSSender(smsMassivo);
			senders.add(sender);
		}
	}

	public void execute() {

	}

	public void cancel() {

	}
}
