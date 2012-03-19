package sms.massivo.helper.db.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyReport {
	private int canceled;
	private String day;
	private int delivery;
	private String fromPhone;
	private int genericFailure;
	private int noService;
	private int nullPDU;
	private int radioOff;
	private int sendSuccessfully;
	private String toPhone;
	private int totalSent;

	public DailyReport(Date date, String fromPhone, String toPhone) {
		day(date);
		this.fromPhone = fromPhone;
		this.toPhone = toPhone;
	}

	public DailyReport(String date, String fromPhone, String toPhone) {
		this.day = date;
		this.fromPhone = fromPhone;
		this.toPhone = toPhone;
	}

	public void day(Date dt) {
		day = new SimpleDateFormat("yyyy-MM-dd").format(dt);
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

	public String getFromPhone() {
		return fromPhone;
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
		this.totalSent++;
		this.canceled++;
	}

	public void incDelivery() {
		this.totalSent++;
		this.delivery++;
	}

	public void incGenericFailure() {
		this.totalSent++;
		this.genericFailure++;
	}

	public void incNoService() {
		this.totalSent++;
		this.noService++;
	}

	public void incNullPDU() {
		this.totalSent++;
		this.nullPDU++;
	}

	public void incRadioOff() {
		this.totalSent++;
		this.radioOff++;
	}

	public void incSendSuccessfully() {
		this.totalSent++;
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

	public void setFromPhone(String fromPhone) {
		this.fromPhone = fromPhone;
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
}
