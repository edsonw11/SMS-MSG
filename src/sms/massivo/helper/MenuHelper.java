package sms.massivo.helper;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class MenuHelper {
	public static MenuItem createMenuItem(Menu menu, int itemId, int titleRes, int iconRes) {
		return createMenuItem(menu, itemId, titleRes, iconRes, null);
	}

	public static MenuItem createMenuItem(Menu menu, int itemId, int titleRes, int iconRes, OnMenuItemClickListener event) {
		return createMenuItem(menu, 0, itemId, titleRes, iconRes, event);
	}
	
	public static MenuItem createMenuItem(Menu menu, int groupId, int itemId, int titleRes, int iconRes, OnMenuItemClickListener event) {
		MenuItem item = menu.add(groupId, itemId, Menu.NONE, titleRes);
		item.setIcon(iconRes);
		item.setOnMenuItemClickListener(event);
		return item;
	}
}
