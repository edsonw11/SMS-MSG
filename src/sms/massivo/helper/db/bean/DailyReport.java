package sms.massivo.helper.db.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class DailyReport {
	private static transient final String TAG = "DailyReport";
	private static transient final String DATE_FORMAT = "yyyy-MM-dd";
	private String day;
	private int counter;
	private String fromSim;
	private int genericFailure;
	private int noService;
	private int nullPDU;
	private int radioOff;
	private int sendSuccessfully;
	private String toPhone;
	private int totalSent;

	public DailyReport(Date date, String fromSim, String toPhone) {
		day(date);
		this.fromSim = fromSim;
		this.toPhone = toPhone;
	}

	public DailyReport(String date, String fromSim, String toPhone) {
		this.day = date;
		this.fromSim = fromSim;
		this.toPhone = toPhone;
	}

	public int getTotalOfFailures() {
		return genericFailure + noService + nullPDU + radioOff;
	}

	public void day(Date dt) {
		day = new SimpleDateFormat(DATE_FORMAT).format(dt);
	}

	public String getDay() {
		return day;
	}

	public int getCounter() {
		return counter;
	}

	public String getFromSim() {
		return fromSim;
	}

	public int getGenericFailure() {
		return genericFailure;
	}

	public int getNoService() {
		return noService;
	}

	public int getNullPDU() {
		return nullPDU;
	}

	public int getRadioOff() {
		return radioOff;
	}

	public int getSendSuccessfully() {
		return sendSuccessfully;
	}

	public String getToPhone() {
		return toPhone;
	}

	public int getTotalSent() {
		return totalSent;
	}

	public int nextCounter() {
		synchronized (this) {
			return this.counter++;
		}
	}

	public void incGenericFailure() {
		synchronized (this) {
			this.genericFailure++;
		}
	}

	public void incNoService() {
		synchronized (this) {
			this.noService++;
		}
	}

	public void incNullPDU() {
		synchronized (this) {
			this.nullPDU++;
		}
	}

	public void incRadioOff() {
		synchronized (this) {
			this.radioOff++;
		}
	}

	public void incSendSuccessfully() {
		synchronized (this) {
			this.sendSuccessfully++;
		}
	}

	public void setDay(String day) {
		this.day = day;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void setFromSim(String fromSim) {
		this.fromSim = fromSim;
	}

	public void setGenericFailure(int genericFailure) {
		this.genericFailure = genericFailure;
	}

	public void setNoService(int noService) {
		this.noService = noService;
	}

	public void setNullPDU(int nullPDU) {
		this.nullPDU = nullPDU;
	}

	public void setRadioOff(int radioOff) {
		this.radioOff = radioOff;
	}

	public void setSendSuccessfully(int sendSuccessfully) {
		this.sendSuccessfully = sendSuccessfully;
	}

	public void setToPhone(String toPhone) {
		this.toPhone = toPhone;
	}

	public void setTotalSent(int totalSent) {
		this.totalSent = totalSent;
	}

	public void incTotalSent() {
		this.totalSent++;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DailyReport[");
		for (Field f : DailyReport.class.getDeclaredFields()) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			f.setAccessible(true);
			sb.append(f.getName()).append(": ");
			try {
				Object value = f.get(this);
				sb.append(value);
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
