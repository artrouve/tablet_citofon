package com.handsriver.concierge.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.handsriver.concierge.R;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Created by Created by alain_r._trouve_silva after 07-01-17.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupToolbar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupToolbar() {
        ViewGroup rootView = (ViewGroup)findViewById(R.id.action_bar_root); //id from appcompat

        if (rootView != null) {
            View view = getLayoutInflater().inflate(R.layout.app_bar, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || ParkingPreferenceFragment.class.getName().equals(fragmentName)
                || ParcelPreferenceFragment.class.getName().equals(fragmentName)
                || VisitsPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general_building);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_apikey_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_gateway_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_building_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_URL_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_URL_ANPR_key)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ParkingPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general_parking);
            setHasOptionsMenu(true);

            findPreference(getString(R.string.pref_id_mark_exit_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    if(!(Boolean) newValue){

                        SwitchPreference markFineExit = (SwitchPreference) findPreference(getString(R.string.pref_id_automatic_fine_key));
                        markFineExit.setChecked(false);
                        SwitchPreference markAutoFine = (SwitchPreference) findPreference(getString(R.string.pref_automatic_fine_key));
                        markAutoFine.setChecked(false);
                        SwitchPreference markAlertFine = (SwitchPreference) findPreference(getString(R.string.pref_send_email_fine_key));
                        markAlertFine.setChecked(false);
                    }

                    return true;
                }
            });

            findPreference(getString(R.string.pref_automatic_fine_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue){
                        SwitchPreference mark = (SwitchPreference) findPreference(getString(R.string.pref_id_automatic_fine_key));
                        mark.setChecked(false);
                        EditTextPreference hours = (EditTextPreference) findPreference(getString(R.string.pref_id_max_time_parking_key));
                        hours.setText(String.valueOf(1));
                        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_max_time_parking_key)));
                    }
                    else{
                        SwitchPreference mark = (SwitchPreference) findPreference(getString(R.string.pref_send_email_alert_key));
                        mark.setChecked(false);
                    }
                    return true;
                }
            });

            findPreference(getString(R.string.pref_id_automatic_fine_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue){
                        SwitchPreference mark = (SwitchPreference) findPreference(getString(R.string.pref_automatic_fine_key));
                        mark.setChecked(false);
                        EditTextPreference hours = (EditTextPreference) findPreference(getString(R.string.pref_id_max_time_parking_key));
                        hours.setText(String.valueOf(1));
                        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_max_time_parking_key)));
                    }
                    return true;
                }
            });

            findPreference(getString(R.string.pref_send_email_fine_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue){
                        EditTextPreference hours = (EditTextPreference) findPreference(getString(R.string.pref_id_time_delay_parking_key));
                        hours.setText(String.valueOf(30));
                        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_time_delay_parking_key)));
                    }
                    return true;
                }
            });

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_parking_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_max_time_parking_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_send_email_field_key)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_id_time_delay_parking_key)));

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ParcelPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general_parcel);
            setHasOptionsMenu(true);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class VisitsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general_visits);
            setHasOptionsMenu(true);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_OPTIONAL_FILED_VISITS_key)));



        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


}
