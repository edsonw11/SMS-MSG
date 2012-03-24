package sms.massivo.task.sender;


public class SMSSenderParams {
	private long delay = 0;
	private String phone;
	private int totalOfMessages = 300;
	private int failureTolerance = 3;

	public long getDelay() {
		return delay;
	}

	public String getPhone() {
		return phone;
	}

	public int getTotalOfMessages() {
		return totalOfMessages;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setTotalOfMessages(int totalOfMessages) {
		this.totalOfMessages = totalOfMessages;
	}
	
	@Override
	public String toString() {
		return String.format("SMSSenderParams[phone:%s, totalOfMessages:%d, delay:%s, failureTolerance: %d]", phone, totalOfMessages, delay, failureTolerance);
	}

	public int getFailureTolerance() {
		return failureTolerance;
	}

	public void setFailureTolerance(int failureTolerance) {
		this.failureTolerance = failureTolerance;
	}
}
