package sms.massivo.helper.db.dao;

import java.util.ArrayList;
import java.util.List;

import sms.massivo.R;
import sms.massivo.helper.db.Database;
import sms.massivo.helper.db.bean.Configuration;
import sms.massivo.helper.db.table.config;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class ConfigurationDao extends Database<Configuration, config> {
	private final Context context;

	public ConfigurationDao(Context context) {
		super(context, config.class);
		this.context = context;
	}
	
	public ContentValues toContentValues(Configuration bean){
		ContentValues values = new ContentValues();
		values.put(config.phone.name(), bean.getPhone());
		values.put(config.failureTolerance.name(), bean.getFailureTolerance());
		values.put(config.total_messages.name(), bean.getTotalOfMessagesToSend());
		values.put(config.is_running.name(), bean.isRunning());
		return values;
	}
	
	public void update(Configuration bean){
		update(bean, null, null);
	}
	
	public Configuration getOrCreate(Configuration bean) {
		Configuration cfg = getFirst(null);
		if (cfg == null) {
			Configuration b = new Configuration();
			b.setFailureTolerance(context.getResources().getInteger(R.defaultValue.totalFailureTolerance));
			b.setPhone(context.getResources().getText(R.defaultValue.phoneToSend).toString());
			b.setTotalOfMessagesToSend(context.getResources().getInteger(R.defaultValue.totalOfMessagesToSend));
			b.setRunning(0);
			insert(b);
			cfg = getFirst(null);
		}
		return cfg;
	}

	@Override
	public List<Configuration> getAll(String whereClause, String[] whereValues,
			String orderBy) {
		List<Configuration> results = new ArrayList<Configuration>();
		Cursor c = null;
		try {
			c = getReadableDatabase().query(tablename(), columns().split(", "), null, null, null, null, orderBy);
			while (c.moveToNext()) {
				Configuration result = new Configuration();
				result.setFailureTolerance(getColumnInt(c, config.failureTolerance));
				result.setRunning(getColumnInt(c, config.is_running));
				result.setPhone(getColumnString(c, config.phone));
				result.setTotalOfMessagesToSend(getColumnInt(c, config.total_messages));

				results.add(result);
			}
		} finally {
			if (c != null)
				c.close();
		}
		return results;
	}
	
	protected String getColumnString(Cursor c, config column) {
		return c.getString(c.getColumnIndex(column.name()));
	}

	protected int getColumnInt(Cursor c, config column) {
		return c.getInt(c.getColumnIndex(column.name()));
	}


}
