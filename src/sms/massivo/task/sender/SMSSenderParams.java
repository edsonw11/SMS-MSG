package sms.massivo.task.sender;


public class SMSSenderParams {
	private String phone;
	private int totalOfMessages = 300;
	private int failureTolerance = 3;

	public String getPhone() {
		return phone;
	}

	public int getTotalOfMessages() {
		return totalOfMessages;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setTotalOfMessages(int totalOfMessages) {
		this.totalOfMessages = totalOfMessages;
	}
	
	@Override
	public String toString() {
		return String.format("SMSSenderParams[phone:%s, totalOfMessages:%d, failureTolerance: %d]", phone, totalOfMessages, failureTolerance);
	}

	public int getFailureTolerance() {
		return failureTolerance;
	}

	public void setFailureTolerance(int failureTolerance) {
		this.failureTolerance = failureTolerance;
	}
}
