package sms.massivo.helper.db.bean;

public class Configuration {
	private String phone;
	private int failureTolerance;
	private int totalOfMessagesToSend;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getFailureTolerance() {
		return failureTolerance;
	}
	public void setFailureTolerance(int failureTolerance) {
		this.failureTolerance = failureTolerance;
	}
	public int getTotalOfMessagesToSend() {
		return totalOfMessagesToSend;
	}
	public void setTotalOfMessagesToSend(int totalOfMessagesToSend) {
		this.totalOfMessagesToSend = totalOfMessagesToSend;
	}
	public boolean isRunning() {
		return isRunning;
	}
	public void setRunning(int isRunning) {
		this.isRunning = isRunning != 0;
	}
	public int getRunning(){
		return isRunning?1:0;
	}
	private boolean isRunning;
}
