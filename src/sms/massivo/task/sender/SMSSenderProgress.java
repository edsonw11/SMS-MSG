package sms.massivo.task.sender;

public class SMSSenderProgress {
	private Integer progress;
	private String message;

	public SMSSenderProgress(Integer progress, String message) {
		super();
		this.progress = progress;
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}
}
