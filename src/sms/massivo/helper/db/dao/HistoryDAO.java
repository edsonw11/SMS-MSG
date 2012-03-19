package sms.massivo.helper.db.dao;

import java.util.ArrayList;
import java.util.List;

import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.db.Database;
import sms.massivo.helper.db.bean.DailyReport;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HistoryDAO extends Database<DailyReport> {

	private String myPhoneNumber;

	enum table {
		day("TEXT"),
		from_phone("TEXT"),
		to_phone("TEXT"),
		send_successfully("INTEGER"),
		generic_failure("INTEGER"),
		no_service("INTEGER"),
		null_pdu("INTEGER"),
		radio_off("INTEGER"),
		delivery("INTEGER"),
		canceled("INTEGER"),
		total_sent("INTEGER");
		private String type;

		private table(String type) {
			this.type = type;
		}

		public String type() {
			return type;
		}

		public static String tablename() {
			return "HISTORIC";
		}

		public static String primaryKeys() {
			return String.format("%s,%s", table.day.name(), table.from_phone.name());
		}

		public static String createColumns() {
			StringBuilder sb = new StringBuilder();
			for (table t : values()) {
				sb.append(t.name()).append(" ").append(t.type()).append(", ");
			}
			sb.setLength(sb.length() - 2);
			return sb.toString();
		}

		public static String columns() {
			StringBuilder sb = new StringBuilder();
			for (table t : values()) {
				sb.append(t.name()).append(", ");
			}
			sb.setLength(sb.length() - 2);
			return sb.toString();
		}
	}

	public HistoryDAO(Context context) {
		super(context);
		myPhoneNumber = EnvironmentAccessor.getMyPhoneNumber(context);
		setTablename(table.tablename());
		setColumns(table.columns());
		setCreateColumns(table.createColumns());
		setPrimaryKeys(table.primaryKeys());
	}

	@Override
	public ContentValues toContentValues(DailyReport bean) {
		ContentValues values = new ContentValues();
		if (bean.getDay() != null)
			values.put(table.day.name(), bean.getDay());
		if (bean.getFromPhone() != null)
			values.put(table.from_phone.name(), bean.getFromPhone());
		if (bean.getToPhone() != null)
			values.put(table.to_phone.name(), bean.getToPhone());
		values.put(table.delivery.name(), bean.getDelivery());
		values.put(table.canceled.name(), bean.getCanceled());
		values.put(table.generic_failure.name(), bean.getGenericFailure());
		values.put(table.no_service.name(), bean.getNoService());
		values.put(table.null_pdu.name(), bean.getNullPDU());
		values.put(table.radio_off.name(), bean.getRadioOff());
		values.put(table.send_successfully.name(), bean.getSendSuccessfully());
		values.put(table.total_sent.name(), bean.getTotalSent());

		return values;
	}

	public void update(DailyReport bean) {
		update(bean, "day = ?", new String[] { bean.getDay() });
	}

	public void delete(DailyReport bean) {
		delete("day = ?", new String[] { bean.getDay() });
	}

	@Override
	public List<DailyReport> getAll(String whereClause, String[] whereValues, String orderBy) {
		// onUpgrade(getWritableDatabase(), 1, 4);
		List<DailyReport> results = new ArrayList<DailyReport>();
		Cursor c = getReadableDatabase().query(table.tablename(), table.columns().split(", "), whereClause, whereValues, null, null, orderBy);

		while (c.moveToNext()) {
			DailyReport result = new DailyReport(getColumnString(c, table.day), myPhoneNumber, getColumnString(c, table.to_phone));
			result.setCanceled(getColumnInt(c, table.canceled));
			result.setDelivery(getColumnInt(c, table.delivery));
			result.setGenericFailure(getColumnInt(c, table.generic_failure));
			result.setNoService(getColumnInt(c, table.no_service));
			result.setNullPDU(getColumnInt(c, table.null_pdu));
			result.setRadioOff(getColumnInt(c, table.radio_off));
			result.setSendSuccessfully(getColumnInt(c, table.send_successfully));
			result.setTotalSent(getColumnInt(c, table.total_sent));

			results.add(result);
		}
		c.close();

		return results;
	}

	private String getColumnString(Cursor c, table column) {
		return c.getString(c.getColumnIndex(column.name()));
	}

	private int getColumnInt(Cursor c, table column) {
		return c.getInt(c.getColumnIndex(column.name()));
	}

	public DailyReport getOrCreate(DailyReport bean) {
		DailyReport dr = getFirst("day = ? and from_phone = ?", new String[] { bean.getDay(), bean.getFromPhone() }, null);
		if (dr == null) {
			insert(bean);
			dr = getFirst("day = ? and from_phone = ?", new String[] { bean.getDay(), bean.getFromPhone() }, null);
		}
		return dr;
	}

}
