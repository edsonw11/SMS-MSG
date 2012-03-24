package sms.massivo.helper;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class MenuHelper {
	public static MenuItem createMenuItem(Menu menu, int itemId, int titleRes, int iconRes) {
		return createMenuItem(menu, itemId, titleRes, iconRes, null);
	}

	public static MenuItem createMenuItem(Menu menu, int itemId, int titleRes, int iconRes, OnMenuItemClickListener event) {
		MenuItem item = menu.add(0, itemId, Menu.NONE, titleRes);
		item.setIcon(iconRes);
		item.setOnMenuItemClickListener(event);
		return item;
	}
}
