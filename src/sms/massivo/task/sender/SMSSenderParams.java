package sms.massivo.task.sender;

public class SMSSenderParams {
	private String phone;
	private String simCard;
	private int totalOfMessages = 300;
	private int failureTolerance = 3;
	private int totalOfSlaves = 4;

	public int getTotalOfSlaves() {
		return totalOfSlaves;
	}

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
		return String.format("SMSSenderParams[phone:%s, SIM:%s, totalOfMessages:%d, failureTolerance: %d]", phone, simCard, totalOfMessages, failureTolerance);
	}

	public int getFailureTolerance() {
		return failureTolerance;
	}

	public void setFailureTolerance(int failureTolerance) {
		this.failureTolerance = failureTolerance;
	}

	public String getSimCard() {
		return simCard;
	}

	public void setSimCard(String simCard) {
		this.simCard = simCard;
	}

	public void setTotalOfSlaves(int totalOfSlaves) {
		this.totalOfSlaves = totalOfSlaves;
	}
}
