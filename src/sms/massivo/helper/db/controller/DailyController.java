package sms.massivo.helper.db.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sms.massivo.helper.db.bean.DailyReport;
import sms.massivo.helper.db.dao.HistoryDAO;
import android.content.Context;

public class DailyController {
	private static Map<String, DailyController> controllers = new HashMap<String, DailyController>();;

	public static DailyController getInstance(Context c, String simCard,
			String phone) {
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

	private DailyController(Context c, String simCard, String phone) {
		historyDao = new HistoryDAO(c);
		dailyReport = historyDao.getOrCreate(new DailyReport(new Date(),
				simCard, phone));
	}

	public synchronized int getTotalSent() {
		return dailyReport.getTotalSent();
	}

	public synchronized int getTotalOfFailures() {
		return dailyReport.getTotalOfFailures();
	}

	public synchronized void incTotalSent() {
		dailyReport.incTotalSent();
		historyDao.update(dailyReport);
	}

	public synchronized void incDelivery() {
		dailyReport.incDelivery();
		historyDao.update(dailyReport);
	}

	public synchronized void incCanceled() {
		dailyReport.incCanceled();
		historyDao.update(dailyReport);
	}

	public synchronized void incSendSuccessfully() {
		dailyReport.incSendSuccessfully();
		historyDao.update(dailyReport);
	}

	public synchronized void incGenericFailure() {
		dailyReport.incGenericFailure();
		historyDao.update(dailyReport);
	}

	public void incNoService() {
		dailyReport.incNoService();
		historyDao.update(dailyReport);
	}

	public synchronized void incNullPDU() {
		dailyReport.incNullPDU();
		historyDao.update(dailyReport);
	}

	public synchronized void incRadioOff() {
		dailyReport.incRadioOff();
		historyDao.update(dailyReport);
	}

	public synchronized void close() {
		historyDao.close();
		for(String key : controllers.keySet()){
			DailyController dc = controllers.get(key);
			if(dc.equals(this)){
				controllers.remove(key);
				return;
			}
		}
	}
}
