package sms.massivo.helper.db.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import android.util.Log;

public class Configuration {
	public static final String TAG = "Configuration";
	private int failureTolerance;
	private boolean isRunning;
	private String phone;
	private boolean stoppedByUser;
	private int totalOfMessagesToSend;
	private int totalOfSlaves;

	public int getFailureTolerance() {
		return failureTolerance;
	}

	public String getPhone() {
		return phone;
	}

	public int getRunning() {
		return isRunning ? 1 : 0;
	}

	public int getStoppedByUser() {
		return stoppedByUser ? 1 : 0;
	}

	public int getTotalOfMessagesToSend() {
		return totalOfMessagesToSend;
	}

	public int getTotalOfSlaves() {
		return totalOfSlaves;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public boolean isStoppedByUser() {
		return stoppedByUser;
	}

	public void setFailureTolerance(int failureTolerance) {
		this.failureTolerance = failureTolerance;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setRunning(int isRunning) {
		this.isRunning = isRunning != 0;
	}

	public void setStoppedByUser(int stoppedByUser) {
		this.stoppedByUser = stoppedByUser != 0;
	}

	public void setTotalOfMessagesToSend(int totalOfMessagesToSend) {
		this.totalOfMessagesToSend = totalOfMessagesToSend;
	}

	public void setTotalOfSlaves(int totalOfSlaves) {
		this.totalOfSlaves = totalOfSlaves;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Configuration[");
		for (Field f : Configuration.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			f.setAccessible(true);
			sb.append(f.getName()).append(": ");
			try {
				sb.append(f.get(this));
			} catch (Exception e) {
				Log.w(TAG, e);
			}
			sb.append(", ");
		}
		sb.setLength(sb.length() - 2);
		sb.append("]");
		return sb.toString();
	}
}
