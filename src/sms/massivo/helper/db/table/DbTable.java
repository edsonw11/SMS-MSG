package sms.massivo.helper.db.table;

public interface DbTable {

	public abstract String type();

	public abstract String tablename();

	public abstract String primaryKeys();

	public abstract String createColumns();

	public abstract String columns();

}