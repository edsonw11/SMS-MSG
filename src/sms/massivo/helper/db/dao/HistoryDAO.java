package sms.massivo.helper.db.dao;

import java.util.ArrayList;
import java.util.List;

import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.db.Database;
import sms.massivo.helper.db.bean.DailyReport;
import sms.massivo.helper.db.table.historic;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class HistoryDAO extends Database<DailyReport, historic> {

	private String mySimCardNumber;

	public HistoryDAO(Context context) {
		super(context, historic.class);
		mySimCardNumber = EnvironmentAccessor.getInstance().getSimCardNumber(context);
	}

	@Override
	public ContentValues toContentValues(DailyReport bean) {
		ContentValues values = new ContentValues();
		if (bean.getDay() != null)
			values.put(historic.day.name(), bean.getDay());
		if (bean.getFromSim() != null)
			values.put(historic.from_sim.name(), bean.getFromSim());
		if (bean.getToPhone() != null)
			values.put(historic.to_phone.name(), bean.getToPhone());
		values.put(historic.delivery.name(), bean.getDelivery());
		values.put(historic.canceled.name(), bean.getCanceled());
		values.put(historic.generic_failure.name(), bean.getGenericFailure());
		values.put(historic.no_service.name(), bean.getNoService());
		values.put(historic.null_pdu.name(), bean.getNullPDU());
		values.put(historic.radio_off.name(), bean.getRadioOff());
		values.put(historic.send_successfully.name(), bean.getSendSuccessfully());
		values.put(historic.total_sent.name(), bean.getTotalSent());

		return values;
	}

	public void update(DailyReport bean) {
		String whereClause = String.format("%s = ? and %s = ?", historic.day.name(), historic.from_sim.name());
		update(bean, whereClause, new String[] { bean.getDay(), bean.getFromSim() });
	}

	public void delete(DailyReport bean) {
		String whereClause = String.format("%s = ? and %s = ?", historic.day.name(), historic.from_sim.name());
		delete(whereClause, new String[] { bean.getDay(), bean.getFromSim() });
	}

	@Override
	public List<DailyReport> getAll(String whereClause, String[] whereValues, String orderBy) {
		// onUpgrade(getWritableDatabase(), 1, 4);
		List<DailyReport> results = new ArrayList<DailyReport>();
		Cursor c = null;
		try {
			c = getReadableDatabase().query(tablename(), columns().split(", "), whereClause, whereValues, null, null, orderBy);
			while (c.moveToNext()) {
				DailyReport result = new DailyReport(getColumnString(c, historic.day), mySimCardNumber, getColumnString(c, historic.to_phone));
				result.setCanceled(getColumnInt(c, historic.canceled));
				result.setDelivery(getColumnInt(c, historic.delivery));
				result.setGenericFailure(getColumnInt(c, historic.generic_failure));
				result.setNoService(getColumnInt(c, historic.no_service));
				result.setNullPDU(getColumnInt(c, historic.null_pdu));
				result.setRadioOff(getColumnInt(c, historic.radio_off));
				result.setSendSuccessfully(getColumnInt(c, historic.send_successfully));
				result.setTotalSent(getColumnInt(c, historic.total_sent));

				results.add(result);
			}
		} finally {
			if (c != null)
				c.close();
		}
		return results;
	}

	private String getColumnString(Cursor c, historic column) {
		return c.getString(c.getColumnIndex(column.name()));
	}

	private int getColumnInt(Cursor c, historic column) {
		return c.getInt(c.getColumnIndex(column.name()));
	}

	public DailyReport getOrCreate(DailyReport bean) {
		String whereClause = String.format("%s = ? and %s = ?", historic.day.name(), historic.from_sim.name());
		DailyReport dr = getFirst(whereClause, new String[] { bean.getDay(), bean.getFromSim() }, null);
		if (dr == null) {
			insert(bean);
			dr = getFirst(whereClause, new String[] { bean.getDay(), bean.getFromSim() }, null);
		}
		return dr;
	}

}
