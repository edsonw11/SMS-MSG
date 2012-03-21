package sms.massivo.view.report;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import sms.massivo.R;
import sms.massivo.helper.EnvironmentAccessor;
import sms.massivo.helper.db.bean.DailyReport;
import sms.massivo.helper.db.dao.HistoryDAO;
import sms.massivo.helper.db.dao.HistoryDAO.table;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class Report extends ListActivity {
	public static final String TAG = "Report";
	private HistoryDAO historyDAO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.report);
		
		Log.d(TAG, "Abrindo conexão com o banco de dados...");
		historyDAO = new HistoryDAO(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		Log.d(TAG, "Fechando conexão com o banco de dados...");
		historyDAO.close();
	}
	
	@Override
	protected void onResume() {
		Log.i(TAG, "Obtendo dados da base...");

		String whereClause = String.format("%s = ?", table.from_sim.name());
		String[] whereValues = new String[] { EnvironmentAccessor.getInstance().getSimCardNumber(this) };
		DailyReport dailyReport = historyDAO.getFirst(whereClause, whereValues, String.format("%s desc", HistoryDAO.table.day.name()));

		Log.i(TAG, "Adicionando dados na lista...");
		List<String> values = new ArrayList<String>();
		for (Field f : DailyReport.class.getDeclaredFields()) {
			try {
				if (Modifier.isTransient(f.getModifiers()))
					continue;
				f.setAccessible(true);
				values.add(String.format("%s: %s", f.getName(), f.get(dailyReport)));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}

		Log.i(TAG, "Preparando adaptador...");
		ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.report_line, R.line_report.label, values);
		setListAdapter(adapter);
		Log.i(TAG, "Lista carregada com êxito");
		super.onResume();
	}
}
