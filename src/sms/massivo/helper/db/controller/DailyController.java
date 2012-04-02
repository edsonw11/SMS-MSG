package sms.massivo.helper.db.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sms.massivo.helper.db.bean.DailyReport;
import sms.massivo.helper.db.dao.HistoryDAO;
import android.content.Context;

public class DailyController {
	private static Map<String, DailyController> controllers = new HashMap<String, DailyController>();;

	public static DailyController getInstance(Context c, String simCard, String phone) {
		String key = simCard + '|' + phone;
		DailyController dc = controllers.get(key);

		if (dc == null) {
			dc = new DailyController(c, simCard, phone);
			controllers.put(key, dc);
		}

		return dc;
	}

	private final HistoryDAO historyDao;
	private DailyReport dailyReport;
	private final String simCard;
	private final String phone;

	private DailyController(Context c, String simCard, String phone) {
		this.simCard = simCard;
		this.phone = phone;
		historyDao = new HistoryDAO(c);
	}

	public synchronized int getTotalSent() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		return dailyReport.getTotalSent();
	}

	public synchronized int getTotalOfFailures() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		return dailyReport.getTotalOfFailures();
	}

	public synchronized void incTotalSent() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incTotalSent();
		historyDao.update(dailyReport);
	}

	public synchronized void incDelivery() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incDelivery();
		historyDao.update(dailyReport);
	}

	public synchronized void incCanceled() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incCanceled();
		historyDao.update(dailyReport);
	}

	public synchronized void incSendSuccessfully() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incSendSuccessfully();
		historyDao.update(dailyReport);
	}

	public synchronized void incGenericFailure() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incGenericFailure();
		historyDao.update(dailyReport);
	}

	public void incNoService() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incNoService();
		historyDao.update(dailyReport);
	}

	public synchronized void incNullPDU() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incNullPDU();
		historyDao.update(dailyReport);
	}

	public synchronized void incRadioOff() {
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(), simCard, phone));
		dailyReport.incRadioOff();
		historyDao.update(dailyReport);
	}

	public synchronized void close() {
		historyDao.close();
		for (String key : controllers.keySet()) {
			DailyController dc = controllers.get(key);
			if (dc.equals(this)) {
				controllers.remove(key);
				return;
			}
		}
	}
}
