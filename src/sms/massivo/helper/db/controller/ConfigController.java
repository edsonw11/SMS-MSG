package sms.massivo.helper.db.controller;

import sms.massivo.helper.db.bean.Configuration;
import sms.massivo.helper.db.dao.ConfigurationDao;
import android.content.Context;
import android.util.Log;

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
	}

	public synchronized String getPhone() {
		config = configDao.getOrCreate(null);
		return config.getPhone();
	}

	public synchronized void setPhone(String phone) {
		config = configDao.getOrCreate(null);
		config.setPhone(phone);
		configDao.update(config);
	}

	public synchronized int getFailureTolerance() {
		config = configDao.getOrCreate(null);
		return config.getFailureTolerance();
	}

	public synchronized void setFailureTolerance(int failureTolerance) {
		config = configDao.getOrCreate(null);
		config.setFailureTolerance(failureTolerance);
		configDao.update(config);
	}

	public synchronized int getTotalOfMessagesToSend() {
		config = configDao.getOrCreate(null);
		return config.getTotalOfMessagesToSend();
	}

	public synchronized void setTotalOfMessagesToSend(int totalOfMessagesToSend) {
		config = configDao.getOrCreate(null);
		config.setTotalOfMessagesToSend(totalOfMessagesToSend);
		configDao.update(config);
	}

	public synchronized void setTotalOfSlaves(int totalOfSlaves) {
		Log.i(">>>",""+totalOfSlaves);
		config = configDao.getOrCreate(null);
		Log.i(">>>",config.toString());
		config.setTotalOfSlaves(totalOfSlaves);
		Log.i(">>>",config.toString());
		configDao.update(config);
	}

	public synchronized boolean isRunning() {
		config = configDao.getOrCreate(null);
		return config.isRunning();
	}

	public synchronized void markAsRunning() {
		config = configDao.getOrCreate(null);
		config.setRunning(1);
		config.setStoppedByUser(0);
		configDao.update(config);
	}

	public synchronized void markAsStopped() {
		config = configDao.getOrCreate(null);
		config.setRunning(0);
		configDao.update(config);
	}

	public synchronized void markAsStoppedByUser() {
		config = configDao.getOrCreate(null);
		config.setStoppedByUser(1);
		markAsStopped();
	}

	public synchronized boolean isStoppedByUser() {
		config = configDao.getOrCreate(null);
		return config.isStoppedByUser();
	}

	public int getTotalOfSlaves() {
		config = configDao.getOrCreate(null);
		return config.getTotalOfSlaves();
	}
}
