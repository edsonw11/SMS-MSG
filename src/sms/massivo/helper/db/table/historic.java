package sms.massivo.helper.db.table;

public enum historic implements DbTable {
	day("TEXT"),
	from_sim("TEXT"),
	to_phone("TEXT"),
	send_successfully("INTEGER"),
	generic_failure("INTEGER"),
	no_service("INTEGER"),
	null_pdu("INTEGER"),
	radio_off("INTEGER"),
	counter("INTEGER"),
	total_sent("INTEGER");
	private String type;

	private historic(String type) {
		this.type = type;
	}

	public String type() {
		return type;
	}

	public String tablename() {
		return "HISTORIC";
	}

	public String primaryKeys() {
		return String.format("%s,%s", historic.day.name(), historic.from_sim.name());
	}

	public String createColumns() {
		StringBuilder sb = new StringBuilder();
		for (historic t : values()) {
			sb.append(t.name()).append(" ").append(t.type()).append(", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}

	public String columns() {
		StringBuilder sb = new StringBuilder();
		for (historic t : values()) {
			sb.append(t.name()).append(", ");
		}
		sb.setLength(sb.length() - 2);
		return sb.toString();
	}
}