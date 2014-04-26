package com.milone.xposed;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.NumberPicker;
import android.widget.Switch;

public class UI_AppChanges extends Activity {
	SharedPreferences prefs; // Shared prefs
	String package_name; // Choosen app's title
	int position; // position on List from UI.java - sent here in an intent,
					// needs to send it back too

	// UI elements
	NumberPicker bottom;
	NumberPicker top;
	CheckBox chkNav;
	CheckBox chkStatus;
	Switch chkEnabled;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appchanges_ui);

		// Set title to the app we're changing and grab it's package name and
		// list position
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		package_name = intent.getStringExtra("package");
		position = intent.getIntExtra("position", -1);
		this.setTitle(name);

		// Ui Elements
		bottom = (NumberPicker) findViewById(R.id.npBottom);
		top = (NumberPicker) findViewById(R.id.npTop);
		chkNav = (CheckBox) findViewById(R.id.chkNav);
		chkStatus = (CheckBox) findViewById(R.id.chkStatus);
		chkEnabled = (Switch) findViewById(R.id.chkEnabled);
		ActionBar actionBar = getActionBar();

		// Shared Prefs
		prefs = getSharedPreferences("translucent", Context.MODE_WORLD_READABLE);

		// Set action bar navigation back
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Set UI Elements based on previous saved sessions for this app (or
		// global if in multi mode)
		top.setMaxValue(256);
		top.setMinValue(0);
		top.setValue(prefs.getInt(package_name + "|top", 0));

		bottom.setMaxValue(256);
		bottom.setMinValue(0);
		bottom.setValue(prefs.getInt(package_name + "|bottom", 0));

		chkNav.setChecked(prefs.getBoolean(package_name + "|nav", true));
		chkStatus.setChecked(prefs.getBoolean(package_name + "|status", true));
		chkEnabled.setChecked(prefs.getBoolean(package_name, false));

		// Enable or disable the 4 options based on if the main switch
		chkNav.setEnabled(chkEnabled.isChecked());
		chkStatus.setEnabled(chkEnabled.isChecked());
		bottom.setEnabled(chkEnabled.isChecked());
		top.setEnabled(chkEnabled.isChecked());

		// Number pickers like to show the keyboard when you do
		// .setValue(#)...don't get it, this hides it
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// Enable or disable the 4 options based on if the main switch
		chkEnabled.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				chkNav.setEnabled(isChecked);
				chkStatus.setEnabled(isChecked);
				bottom.setEnabled(isChecked);
				top.setEnabled(isChecked);

			}

		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			// close this activity and return to the list with new info (see
			// also onBackPressed)
			Intent resultIntent = new Intent();
			resultIntent.putExtra("position", position);
			resultIntent.putExtra("enabled", chkEnabled.isChecked());
			setResult(RESULT_OK, resultIntent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		// Save everything to SharedPrefs
		SharedPreferences.Editor prefsEditr = prefs.edit();
		prefsEditr.putBoolean(package_name + "|nav", chkNav.isChecked());
		prefsEditr.putBoolean(package_name + "|status", chkStatus.isChecked());
		prefsEditr.putInt(package_name + "|top", top.getValue());
		prefsEditr.putInt(package_name + "|bottom", bottom.getValue());
		prefsEditr.putBoolean(package_name, chkEnabled.isChecked());
		prefsEditr.commit();
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("position", position);
		resultIntent.putExtra("enabled", chkEnabled.isChecked());
		setResult(RESULT_OK, resultIntent);
		super.onBackPressed();

	}

}
