package sms.massivo.helper;

import android.app.Activity;
import android.content.Context;

public class ContextHelper {
	@SuppressWarnings("unchecked")
	public static <T extends Context> T getBaseContext(Activity act){
		Activity base = act;
		while(base.getParent() != null){
			base = base.getParent();
		}
		return (T) base;
	}
}
