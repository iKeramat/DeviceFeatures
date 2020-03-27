/*
 * Copyright (C) 2018 The Xiaomi-SDM660 Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package org.lineageos.settings.device;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.preference.PreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import org.lineageos.settings.device.kcal.KCalSettingsActivity;
import org.lineageos.settings.device.preferences.SecureSettingListPreference;
import org.lineageos.settings.device.preferences.SecureSettingSwitchPreference;
import org.lineageos.settings.device.preferences.VibrationSeekBarPreference;

public class DeviceSettings extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    // Vibration
    public static final String PREF_VIBRATION_STRENGTH = "vibration_strength";
    public static final String VIBRATION_STRENGTH_PATH = "/sys/class/timed_output/vibrator/vtg_level";
    public static final int MIN_VIBRATION = 12;
    public static final int MAX_VIBRATION = 127;

    // Display
    private static final String CATEGORY_DISPLAY = "display";
    private static final String PREF_DEVICE_DOZE = "device_doze";
    private static final String PREF_DEVICE_KCAL = "device_kcal";
    private static final String DEVICE_DOZE_PACKAGE_NAME = "org.lineageos.settings.doze";

    // Spectrum
    public static final String PREF_SPECTRUM = "spectrum";
    public static final String SPECTRUM_PATH = "/sys/devices/virtual/thermal/thermal_message/sconfig";
    private SecureSettingListPreference mSPECTRUM;


    // Buttons
    private static final String CATEGORY_BUTTONS = "buttons";

    // Swap buttons
    public static final String PREF_SWAP_BUTTONS = "swapbuttons";
    public static final String SWAP_BUTTONS_PATH = "/proc/touchpanel/reversed_keys_enable";

    // Fingerprint
    // Fingerprint Wakeup
    public static final String PREF_FINGERPRINT_WAKEUP = "fingerprint_wakeup";
    public static final String FINGERPRINT_WAKEUP_PATH = "/sys/devices/soc/soc:fpc_fpc1020/enable_wakeup";

    // onCreatePreferences
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_xiaomi_parts, rootKey);

        VibrationSeekBarPreference vibrationStrength = (VibrationSeekBarPreference) findPreference(PREF_VIBRATION_STRENGTH);
        vibrationStrength.setEnabled(FileUtils.fileWritable(VIBRATION_STRENGTH_PATH));
        vibrationStrength.setOnPreferenceChangeListener(this);

        PreferenceCategory displayCategory = (PreferenceCategory) findPreference(CATEGORY_DISPLAY);
        if (isAppNotInstalled(DEVICE_DOZE_PACKAGE_NAME)) {
            displayCategory.removePreference(findPreference(PREF_DEVICE_DOZE));
        }

        Preference kcal = findPreference(PREF_DEVICE_KCAL);

        kcal.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity().getApplicationContext(), KCalSettingsActivity.class);
            startActivity(intent);
            return true;
        });

        mSPECTRUM = (SecureSettingListPreference) findPreference(PREF_SPECTRUM);
        mSPECTRUM.setValue(FileUtils.getValue(SPECTRUM_PATH));
        mSPECTRUM.setSummary(mSPECTRUM.getEntry());
        mSPECTRUM.setOnPreferenceChangeListener(this);

        if (FileUtils.fileWritable(SWAP_BUTTONS_PATH)) {
            SecureSettingSwitchPreference swapbuttons = (SecureSettingSwitchPreference) findPreference(PREF_SWAP_BUTTONS);
            swapbuttons.setChecked(FileUtils.getFileValueAsBoolean(SWAP_BUTTONS_PATH, false));
            swapbuttons.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(CATEGORY_BUTTONS));
        }
        if (FileUtils.fileWritable(FINGERPRINT_WAKEUP_PATH)) {
            SecureSettingSwitchPreference fingerprint_wakeup = (SecureSettingSwitchPreference) findPreference(PREF_FINGERPRINT_WAKEUP);
            fingerprint_wakeup.setChecked(FileUtils.getFileValueAsBoolean(FINGERPRINT_WAKEUP_PATH, false));
            fingerprint_wakeup.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PREF_FINGERPRINT_WAKEUP));
        }
    }

    // onPreferenceChange
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_VIBRATION_STRENGTH:
                double vibrationValue = (int) value / 100.0 * (MAX_VIBRATION - MIN_VIBRATION) + MIN_VIBRATION;
                FileUtils.setValue(VIBRATION_STRENGTH_PATH, vibrationValue);
                break;

            case PREF_SPECTRUM:
                mSPECTRUM.setValue((String) value);
                mSPECTRUM.setSummary(mSPECTRUM.getEntry());
                FileUtils.setValue(SPECTRUM_PATH, (String) value);
                break;

            case PREF_SWAP_BUTTONS:
                FileUtils.setValue(SWAP_BUTTONS_PATH, (boolean) value);
                break;

            case PREF_FINGERPRINT_WAKEUP:
                FileUtils.setValue(FINGERPRINT_WAKEUP_PATH, (boolean) value);
                break;

            default:
                break;
        }
        return true;
    }

    // isAppNotInstalled
    private boolean isAppNotInstalled(String uri) {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }
}
