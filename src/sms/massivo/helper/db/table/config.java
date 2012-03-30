package sms.massivo.helper.db.table;

public enum config implements DbTable {
	phone("TEXT"),
	failure_tolerance("INTEGER"),
	total_messages("INTEGER"),
	total_of_slaves("INTEGER"),
	is_running("INTEGER"),
	stopped_by_user("INTEGER"),;
	private String type;

	private config(String type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see sms.massivo.helper.db.table.DbTable#type()
	 */
	@Override
	public String type() {
		return type;
	}

	/* (non-Javadoc)
	 * @see sms.massivo.helper.db.table.DbTable#tablename()
	 */
	@Override
	public String tablename() {
		return "CONFIG";
	}

	/* (non-Javadoc)
	 * @see sms.massivo.helper.db.table.DbTable#primaryKeys()
	 */
	@Override
	public String primaryKeys() {
		return "";
	}

	/* (non-Javadoc)
	 * @see sms.massivo.helper.db.table.DbTable#createColumns()
	 */
	@Override
	public String createColumns() {
		StringBuilder sb = new StringBuilder();
		for (config t : values()) {
			sb.append(t.name()).append(" ").append(t.type()).append(", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see sms.massivo.helper.db.table.DbTable#columns()
	 */
	@Override
	public String columns() {
		StringBuilder sb = new StringBuilder();
		for (config t : values()) {
			sb.append(t.name()).append(", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}
}