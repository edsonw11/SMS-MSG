package sms.massivo.helper.db.controller;

import sms.massivo.helper.db.bean.Configuration;
import sms.massivo.helper.db.dao.ConfigurationDao;
import android.content.Context;

public class ConfigController {
	private static ConfigController instance;

	public static ConfigController getInstance(Context c) {
		if (instance == null) {
			instance = new ConfigController(c);
		}
		return instance;
	}

	private ConfigurationDao configDao;
	private Configuration config;

	private ConfigController(Context c) {
		configDao = new ConfigurationDao(c);
		config = configDao.getOrCreate(new Configuration());
	}

	public synchronized String getPhone() {
		return config.getPhone();
	}

	public synchronized void setPhone(String phone) {
		config.setPhone(phone);
		configDao.update(config);
	}

	public synchronized int getFailureTolerance() {
		return config.getFailureTolerance();
	}

	public synchronized void setFailureTolerance(int failureTolerance) {
		config.setFailureTolerance(failureTolerance);
		configDao.update(config);
	}

	public synchronized int getTotalOfMessagesToSend() {
		return config.getTotalOfMessagesToSend();
	}

	public synchronized void setTotalOfMessagesToSend(int totalOfMessagesToSend) {
		config.setTotalOfMessagesToSend(totalOfMessagesToSend);
		configDao.update(config);
	}

	public synchronized void setTotalOfSlaves(int totalOfSlaves) {
		config.setTotalOfSlaves(totalOfSlaves);
		configDao.update(config);
	}

	public synchronized boolean isRunning() {
		return config.isRunning();
	}

	public synchronized void markAsRunning() {
		config.setRunning(1);
		config.setStoppedByUser(0);
		configDao.update(config);
	}

	public synchronized void markAsStopped() {
		config.setRunning(0);
		configDao.update(config);
	}

	public synchronized void markAsStoppedByUser() {
		config.setStoppedByUser(1);
		markAsStopped();
	}

	public synchronized boolean isStoppedByUser() {
		return config.isStoppedByUser();
	}

	public int getTotalOfSlaves() {
		return config.getTotalOfSlaves();
	}
}
