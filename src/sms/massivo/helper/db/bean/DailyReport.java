package sms.massivo.helper.db.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyReport {
	private static transient final String DATE_FORMAT = "yyyy-MM-dd";
	private int canceled;
	private String day;
	private int delivery;
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

	public int getCanceled() {
		return canceled;
	}

	public String getDay() {
		return day;
	}

	public int getDelivery() {
		return delivery;
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

	public void incCanceled() {
		this.canceled++;
	}

	public void incDelivery() {
		this.delivery++;
	}

	public void incGenericFailure() {
		this.genericFailure++;
	}

	public void incNoService() {
		this.noService++;
	}

	public void incNullPDU() {
		this.nullPDU++;
	}

	public void incRadioOff() {
		this.radioOff++;
	}

	public void incSendSuccessfully() {
		this.sendSuccessfully++;
	}

	public void setCanceled(int canceled) {
		this.canceled = canceled;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public void setDelivery(int delivery) {
		this.delivery = delivery;
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
}
