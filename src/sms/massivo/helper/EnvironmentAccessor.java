package sms.massivo.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import sms.massivo.helper.db.controller.ConfigController;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class EnvironmentAccessor {
	public static final String TAG = "EnvironmentAccessor";
	private static EnvironmentAccessor instance;

	public static EnvironmentAccessor getInstance() {
		if (instance == null) {
			instance = new EnvironmentAccessor();
		}
		return instance;
	}

	private Map<Class<Activity>, Activity> activities = Collections.synchronizedMap(new HashMap<Class<Activity>, Activity>());
	private Map<Class<Service>, Service> services = Collections.synchronizedMap(new HashMap<Class<Service>, Service>());

	private EnvironmentAccessor() {
		Log.d(TAG, "Acesso ao ambiente criado");
	}

	public String getSimCardNumber(Context context) {
		TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		// return tMgr.getLine1Number();
		return tMgr.getSimSerialNumber();
	}

	public void playRingtone(Context context) {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		}
		if (alert == null) {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		}
		Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), alert);
		if (r != null) {
			r.play();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			r.stop();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void add(Activity activity) {
		if (activity == null)
			return;
		Log.i(TAG, String.format("Atividade adicionada ao ambiente: %s", activity.getClass().getName()));
		activities.put((Class<Activity>) activity.getClass(), activity);
	}

	@SuppressWarnings("unchecked")
	public <A extends Activity> A get(Class<A> activityClass) {
		A a = (A) activities.get(activityClass);
		if (a == null)
			Log.w(TAG, String.format("Atividade %s não encontrada. Retorno nulo", activityClass.getName()));
		else
			Log.d(TAG, String.format("Obtida atividade %s", activityClass.getName()));
		return a;
	}

	public void remove(Activity activity) {
		Log.i(TAG, String.format("Atividade removida do ambiente: %s", activity.getClass().getName()));
		activities.remove(activity);
	}

	@SuppressWarnings("unchecked")
	public void add(Service service) {
		if (service == null)
			return;
		Log.i(TAG, String.format("Serviço adicionado ao ambiente: %s", service.getClass().getName()));
		services.put((Class<Service>) service.getClass(), service);
	}

	@SuppressWarnings("unchecked")
	public <S extends Service> S get(Class<S> serviceClass) {
		S s = (S) services.get(serviceClass);
		if (s == null)
			Log.w(TAG, String.format("Serviço %s não encontrado. Retorno nulo", serviceClass.getName()));
		else
			Log.d(TAG, String.format("Obtido serviço %s", serviceClass.getName()));
		return s;
	}

	public void remove(Service service) {
		Log.i(TAG, String.format("Serviço removido do ambiente: %s", service.getClass().getName()));
		services.remove(service);
	}
}
