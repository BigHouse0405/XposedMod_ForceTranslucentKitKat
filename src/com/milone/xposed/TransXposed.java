package com.milone.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.app.Activity;
import android.view.View;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class TransXposed implements IXposedHookLoadPackage {
	XSharedPreferences prefs = new XSharedPreferences("com.milone.xposed",
			"translucent");

	public void handleLoadPackage(final LoadPackageParam lpparam)
			throws Throwable {

		// Needed for Xposed to read changes without a reboot
		prefs.makeWorldReadable();
		prefs.reload();

		// If the app isn't checked in the main list, nothing to do, get out
		if (!prefs.getBoolean(lpparam.packageName, false))
			return;

		// Otherwise lets find the class and enter it's Resume function
		Class<?> ActivityClass = XposedHelpers.findClass(
				"android.app.Activity", null);

		findAndHookMethod(ActivityClass, "performResume", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				// Get the activity, root layout and action bar
				Activity activity = (Activity) param.thisObject;
				View rootLayer = activity.getWindow().getDecorView()
						.findViewById(android.R.id.content);
				// ActionBar actionBar = activity.getActionBar();

				// Apply options based on SharedPreferences
				// Padding - right and left are 0, while top and bottom are read
				// from prefs
				rootLayer.setPadding(0,
						prefs.getInt(lpparam.packageName + "|top", 0), 0,
						prefs.getInt(lpparam.packageName + "|bottom", 0));

				// Status bar
				if (prefs.getBoolean(lpparam.packageName + "|status", true))
					activity.getWindow().addFlags(
							WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

				// Nav bar
				if (prefs.getBoolean(lpparam.packageName + "|nav", true))
					activity.getWindow()
							.addFlags(
									WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			}
		});

	}
}