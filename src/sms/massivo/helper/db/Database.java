package sms.massivo.helper.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class Database<T> extends SQLiteOpenHelper {
	private static final String TAG = "Database";
	public static final String DB_NAME = "SMSMassivo";
	public static final int DB_VERSION = 6;
	private String tablename;
	private String createColumns;
	private String columns;
	private String primaryKeys;

	public Database(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Criando tabela "+ tablename);
		String ddl = String.format("CREATE TABLE %s (%s, primary key(%s));", tablename, createColumns, primaryKeys);
		db.execSQL(ddl);
		Log.i(TAG, "Tabela criada com êxito");
	}

	protected void setTablename(String tablename) {
		this.tablename = tablename;
	}

	protected void setCreateColumns(String createColumns) {
		this.createColumns = createColumns;
	}
	
	protected void setPrimaryKeys(String primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	protected void setColumns(String columns) {
		this.columns = columns;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, String.format("Atualizando tabela %s da versão %d para %d...", tablename, oldVersion, newVersion));
		String ddl = "DROP TABLE IF EXISTS " + tablename;
		db.execSQL(ddl);
		onCreate(db);
		Log.w(TAG, "Tabela atualizada com êxito");
	}

	public abstract ContentValues toContentValues(T bean);

	public void insert(T bean) {
		Log.i(TAG,String.format("Inserindo dados em %s: %s", tablename, bean));
		getWritableDatabase().insert(tablename, null, toContentValues(bean));
	}

	public void update(T bean, String whereClause, String[] whereParams) {
		Log.i(TAG,String.format("Atualizando dados em %s[%s: %s]: %s", tablename, whereClause, whereParams, bean));
		getWritableDatabase().update(tablename, toContentValues(bean), whereClause, whereParams);
	}

	public void delete(String whereClause, String[] whereParams) {
		Log.i(TAG,String.format("Excluindo dados em %s[%s: %s]s", tablename, whereClause, whereParams));
		getWritableDatabase().delete(tablename, whereClause, whereParams);
	}

	public abstract List<T> getAll(String whereClause, String[] whereValues, String orderBy);

	public List<T> getAll(String orderBy) {
		return getAll(null, null, orderBy);
	}

	public T getFirst(String whereClause, String[] whereValues, String orderBy) {
		List<T> results = getAll(whereClause, whereValues, orderBy);
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	public T getFirst(String orderBy) {
		return getFirst(null, null, orderBy);
	}
}
