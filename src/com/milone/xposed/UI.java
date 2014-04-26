package com.milone.xposed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class UI extends Activity {
	ListView list; // The UI's ListView
	HashMap<String, String> processMatcher; // Hashmap to match package names
											// (com.android.whatever) to app
											// name (Dialer)
	String[] lv_items; // Contains all the app names
	boolean multi_mode; // Switch for if editing normally or multi mode
	private ProgressDialog pDialog; // Progress dialog for app list loading

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ui);
		list = (ListView) findViewById(R.id.listApps);

		//Async task below
		new LoadAppList().execute();

		//Handle List Clicks differently depending on mode
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// If the UI is in multi mode, just act like a normal check
				// list, otherwise...
				if (!multi_mode) {
					// Undo the UI checking or unchecking the box and bring up
					// advanced options
					CheckedTextView item = (CheckedTextView) view;
					list.setItemChecked(position, !item.isChecked());
					Intent intent = new Intent(UI.this, UI_AppChanges.class);
					intent.putExtra("name", lv_items[position]);
					intent.putExtra("package",
							processMatcher.get(lv_items[position]));
					intent.putExtra("position", position);
					startActivityForResult(intent, 0);
				}

			}

		});

	}

	// Edit the listview to reflect enabling or disabling in advanced options
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Returning cleanly from advanced options for app
		if (resultCode == RESULT_OK && requestCode == 0) {
			boolean checked = data.getBooleanExtra("enabled", false);
			int position = data.getIntExtra("position", -1);
			if (position > -1) {
				list.setItemChecked(position, checked);
			}
		}

		//Returning cleanly from multi mode options activity 
		if (resultCode == RESULT_OK && requestCode == 1) {
			// clear the list and set multi mode flag and UI
			for (int i = 0; i < list.getCount(); i++) {
				list.setItemChecked(i, false);
			}
			Button butAboveList = (Button) findViewById(R.id.butAboveList);
			butAboveList.setText("Apply Settings To Checked Apps");
			multi_mode = true;

		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.listoptions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		SharedPreferences prefs = getSharedPreferences("translucent",
				Context.MODE_WORLD_READABLE);
		SharedPreferences.Editor prefsEditr = prefs.edit();

		switch (item.getItemId()) {
			// Select All
		case R.id.action_all:
			for (int i = 0; i < list.getCount(); i++) {
				list.setItemChecked(i, true);
				prefsEditr.putBoolean(processMatcher.get(lv_items[i]), true);
			}
			prefsEditr.commit();
			return true;

			// Clear All
		case R.id.action_none:
			for (int i = 0; i < list.getCount(); i++) {
				list.setItemChecked(i, false);
				prefsEditr.putBoolean(processMatcher.get(lv_items[i]), false);
			}
			prefsEditr.commit();
			return true;

			// Load AppChanges Activity w/ global parameters
		case R.id.action_global:
			Intent intent = new Intent(UI.this, UI_AppChanges.class);
			intent.putExtra("name", "Multiple Edit");
			intent.putExtra("package", "global");
			startActivityForResult(intent, 1);
			return true;
			
			// Export the settings
		case R.id.action_export:
			if (multi_mode) {
				Toast.makeText(this,
						"Cannot perform export while in Multi-Edit Mode",
						Toast.LENGTH_SHORT).show();
				return true;
			}
			try {
				exportCSV();
			} catch (Exception e) {
				Toast.makeText(this, "Error. Log printed out to Xposed.",
						Toast.LENGTH_SHORT).show();
				XposedBridge.log(e.toString());
			}
			return true;
			
			// Import the settings
		case R.id.action_import:
			if (multi_mode) {
				Toast.makeText(this,
						"Cannot perform import while in Multi-Edit Mode",
						Toast.LENGTH_SHORT).show();
				return true;
			}
			try {
				importCSV();
			} catch (Exception e) {
				Toast.makeText(this, "Error. Log printed out to Xposed.",
						Toast.LENGTH_SHORT).show();
				XposedBridge.log(e.toString());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Import the values from the CSV and restart the app
	public void importCSV() throws Exception {
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard, "ForceTranslucentKitKat.csv");
		InputStream in = new FileInputStream(file);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		SharedPreferences prefs = getSharedPreferences("translucent",
				Context.MODE_WORLD_READABLE);
		String line = reader.readLine(); // skip the top line (our headers)
		SharedPreferences.Editor prefsEditr = prefs.edit();

		while ((line = reader.readLine()) != null) {
			String name_package = line.split(",")[1];
			prefsEditr.putBoolean(name_package,
					Boolean.parseBoolean(line.split(",")[2]));
			prefsEditr.putBoolean(name_package + "|nav",
					Boolean.parseBoolean(line.split(",")[3]));
			prefsEditr.putBoolean(name_package + "|status",
					Boolean.parseBoolean(line.split(",")[4]));
			prefsEditr.putInt(name_package + "|top",
					Integer.parseInt(line.split(",")[5]));
			prefsEditr.putInt(name_package + "|bottom",
					Integer.parseInt(line.split(",")[6]));
		}
		prefsEditr.commit();
		in.close();
		Toast.makeText(this, "Settings loaded from file, reloading..",
				Toast.LENGTH_SHORT).show();
		this.recreate();

	}

	public void exportCSV() throws Exception {
		SharedPreferences prefs = getSharedPreferences("translucent",
				Context.MODE_WORLD_READABLE);
		File sdcard = Environment.getExternalStorageDirectory();
		File file = new File(sdcard, "ForceTranslucentKitKat.csv");
		FileWriter out = new FileWriter(file);
		String entire = "App Name,Package Name,Enabled,NavBar,StatusBar,TopPadding,BottomPadding,\n";
		for (int i = 0; i < list.getCount(); i++) {
			entire = entire + lv_items[i] + ","
					+ processMatcher.get(lv_items[i]) + ",";
			entire = entire
					+ prefs.getBoolean(processMatcher.get(lv_items[i]), false)
					+ ",";
			entire = entire
					+ prefs.getBoolean(
							processMatcher.get(lv_items[i]) + "|nav", false)
					+ ",";
			entire = entire
					+ prefs.getBoolean(processMatcher.get(lv_items[i])
							+ "|status", false) + ",";
			entire = entire
					+ prefs.getInt(processMatcher.get(lv_items[i]) + "|top", 0)
					+ ",";
			entire = entire
					+ prefs.getInt(processMatcher.get(lv_items[i]) + "|bottom",
							0) + ",\n";
		}
		out.write(entire);
		out.close();
		Toast.makeText(this,
				"Settings exported to file - ForceTranslucentKitKat.csv",
				Toast.LENGTH_LONG).show();

	}

	// Handle the UI button click
	public void butAboveListClick(View v) {
		// If multi edit is being used, this button acts as the save button
		// Apply the settings to all checked apps, them restart to return to normal mode
		if (multi_mode) {
			SparseBooleanArray checked = list.getCheckedItemPositions();
			SharedPreferences prefs = getSharedPreferences("translucent",
					Context.MODE_WORLD_READABLE);
			SharedPreferences.Editor prefsEditr = prefs.edit();

			//get the saved settings for "global" package name
			boolean global_enabled = prefs.getBoolean("global", false);
			boolean global_nav = prefs.getBoolean("global|nav", false);
			boolean global_status = prefs.getBoolean("global|status", false);
			int global_top = prefs.getInt("global|top", 0);
			int global_bottom = prefs.getInt("global|bottom", 0);

			//Apply them to all checked items
			for (int i = 0; i < list.getAdapter().getCount(); i++) {
				if (checked.get(i)) {

					prefsEditr.putBoolean(processMatcher.get(lv_items[i])
							+ "|nav", global_nav);
					prefsEditr.putBoolean(processMatcher.get(lv_items[i])
							+ "|status", global_status);
					prefsEditr.putInt(processMatcher.get(lv_items[i]) + "|top",
							global_top);
					prefsEditr.putInt(processMatcher.get(lv_items[i])
							+ "|bottom", global_bottom);
					prefsEditr.putBoolean(processMatcher.get(lv_items[i]),
							global_enabled);
				}
				prefsEditr.commit();
			}
			Toast.makeText(this, "Settings saved to selected apps!",
					Toast.LENGTH_LONG).show();
			this.recreate();

		} else {
			// Otherwise load my app store =)
			Toast.makeText(this, "Thanks for checking my apps out!",
					Toast.LENGTH_SHORT).show();
			final Intent marketIntent = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://play.google.com/store/apps/developer?id=Stephen+Milone"));
			startActivity(marketIntent);
		}
	}

	// Async Task to load app list:
	private class LoadAppList extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			// Vars to hold info
			ArrayList<String> stringArrayList = new ArrayList<String>();
			processMatcher = new HashMap<String, String>();

			// Package manager and loop to find all packages that have a
			// launchable Intent
			final PackageManager pm = getPackageManager();
			List<ApplicationInfo> packages = pm
					.getInstalledApplications(PackageManager.GET_META_DATA);
			for (ApplicationInfo packageInfo : packages) {
				if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {

					// Put the package name and app name in the processMatcher
					// hashmap
					processMatcher.put(pm.getApplicationLabel(packageInfo)
							.toString(), packageInfo.packageName);
					// Put the app name in the string array
					stringArrayList.add(pm.getApplicationLabel(packageInfo)
							.toString());
				}
			}
			// Sort the app list and prep the list items var.
			Collections.sort(stringArrayList);
			lv_items = stringArrayList.toArray(new String[stringArrayList
					.size()]);
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			// Take the list items var and put it in the ListView
			list.setAdapter(new ArrayAdapter<String>(UI.this,
					android.R.layout.simple_list_item_multiple_choice, lv_items));
			list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

			// Cross check list with SharedPrefs and mark those enabled apps as
			// so
			@SuppressWarnings("deprecation")
			SharedPreferences prefs = getSharedPreferences("translucent",
					Context.MODE_WORLD_READABLE);

			for (int i = 0; i < list.getCount(); i++) {
				if (prefs.getBoolean(processMatcher.get(lv_items[i]), false)) {
					list.setItemChecked(i, true);
				}
			}

			// get rid of the dialog, we're ready to rock.
			pDialog.dismiss();

		}

		@Override
		protected void onPreExecute() {
			// simple spinner as loading the apps list is intensive process
			pDialog = ProgressDialog.show(UI.this, "", "Loading app list...",
					true);
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}
}
