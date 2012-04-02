package sms.massivo.helper.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sms.massivo.helper.db.table.DbTable;
import sms.massivo.helper.db.table.config;
import sms.massivo.helper.db.table.historic;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class Database<BeanType, DbTableType extends DbTable> extends SQLiteOpenHelper {
	private static final String TAG = "Database";
	public static final String DB_NAME = "SMSMassivo";
	public static final int DB_VERSION = 10;

	private static final List<Class<? extends DbTable>> tables = new ArrayList<Class<? extends DbTable>>();
	private final DbTable table;

	static {
		tables.add(historic.class);
		tables.add(config.class);
	}

	public Database(Context context, Class<DbTableType> table) {
		super(context, DB_NAME, null, DB_VERSION);
		this.table = table.getEnumConstants()[0];
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		for (Class<? extends DbTable> tc : tables) {
			DbTable dbTable = tc.getEnumConstants()[0];

			Log.i(TAG, "Criando tabela " + dbTable.tablename());
			String ddl;
			if (dbTable.primaryKeys() != null && dbTable.primaryKeys().length() > 0) {
				ddl = String.format("CREATE TABLE %s (%s, primary key(%s));", dbTable.tablename(), dbTable.createColumns(), dbTable.primaryKeys());
			} else {
				ddl = String.format("CREATE TABLE %s (%s);", dbTable.tablename(), dbTable.createColumns());
			}
			db.execSQL(ddl);
			Log.i(TAG, "Tabela criada com êxito");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (Class<? extends DbTable> tc : tables) {
			DbTable dbTable = tc.getEnumConstants()[0];
			Log.w(TAG, String.format("Atualizando tabela %s da versão %d para %d...", dbTable.tablename(), oldVersion, newVersion));
			String ddl = "DROP TABLE IF EXISTS " + dbTable.tablename();
			db.execSQL(ddl);
		}
		onCreate(db);
		Log.w(TAG, "Tabela atualizada com êxito");
	}

	public abstract ContentValues toContentValues(BeanType bean);

	public void insert(BeanType bean) {
		Log.i(TAG, String.format("Inserindo dados em %s: %s", table.tablename(), bean));
		getWritableDatabase().insert(table.tablename(), null, toContentValues(bean));
	}

	public void update(BeanType bean, String whereClause, String[] whereParams) {
		Log.i(TAG, String.format("Atualizando dados em %s[where '%s': params '%s']: %s", table.tablename(), whereClause, Arrays.toString(whereParams), bean));
		getWritableDatabase().update(table.tablename(), toContentValues(bean), whereClause, whereParams);
	}

	public void delete(String whereClause, String[] whereParams) {
		Log.i(TAG, String.format("Excluindo dados em %s[%s: %s]s", table.tablename(), whereClause, whereParams));
		getWritableDatabase().delete(table.tablename(), whereClause, whereParams);
	}

	public abstract List<BeanType> getAll(String whereClause, String[] whereValues, String orderBy);

	public List<BeanType> getAll(String orderBy) {
		return getAll(null, null, orderBy);
	}

	public BeanType getFirst(String whereClause, String[] whereValues, String orderBy) {
		List<BeanType> results = getAll(whereClause, whereValues, orderBy);
		if (results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	public BeanType getFirst(String orderBy) {
		return getFirst(null, null, orderBy);
	}

	protected String tablename(){
		return table.tablename();
	}
	
	protected String columns(){
		return table.columns();
	}
}
