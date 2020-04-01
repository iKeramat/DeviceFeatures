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
import org.lineageos.settings.device.preferences.SecureSettingCustomSeekBarPreference;
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
    public static final String SPECTRUM_SYSTEM_PROPERTY = "persist.spectrum.profile";
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

    // Fingerprint as button
    public static final String PREF_FINGERPRINT_AS_BUTTON = "fingerprint_as_button";
    public static final String FINGERPRINT_AS_BUTTON_PATH = "/sys/devices/soc/soc:fpc_fpc1020/enable_key_events";

    // Gestures
    private static final String CATEGORY_GESTURES = "gestures";

    // Double tap to wake
    public static final String PREF_DOUBLE_TAP_TO_WAKE = "double_tap_to_wake";
    public static final String DOUBLE_TAP_TO_WAKE_PATH = "/proc/touchpanel/double_tap_enable";

    // Torch
    public static final String PREF_TORCH_BRIGHTNESS = "torch_brightness";
    public static final String TORCH_1_BRIGHTNESS_PATH = "/sys/devices/soc/400f000.qcom," +
            "spmi/spmi-0/spmi0-03/400f000.qcom,spmi:qcom,pmi8994@3:qcom,leds@d300/leds/led:torch_0/max_brightness";
    public static final String TORCH_2_BRIGHTNESS_PATH = "/sys/devices/soc/400f000.qcom," +
            "spmi/spmi-0/spmi0-03/400f000.qcom,spmi:qcom,pmi8994@3:qcom,leds@d300/leds/led:torch_1/max_brightness";

    // onCreatePreferences
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences_xiaomi_parts, rootKey);


        SecureSettingCustomSeekBarPreference TorchBrightness = (SecureSettingCustomSeekBarPreference) findPreference(PREF_TORCH_BRIGHTNESS);
        TorchBrightness.setEnabled(FileUtils.fileWritable(TORCH_1_BRIGHTNESS_PATH) &&
                FileUtils.fileWritable(TORCH_2_BRIGHTNESS_PATH));
        TorchBrightness.setOnPreferenceChangeListener(this);

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
        mSPECTRUM.setValue(FileUtils.getStringProp(SPECTRUM_SYSTEM_PROPERTY, "0"));
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
        if (FileUtils.fileWritable(FINGERPRINT_AS_BUTTON_PATH)) {
            SecureSettingSwitchPreference fingerprint_as_button = (SecureSettingSwitchPreference) findPreference(PREF_FINGERPRINT_AS_BUTTON);
            fingerprint_as_button.setChecked(FileUtils.getFileValueAsBoolean(FINGERPRINT_AS_BUTTON_PATH, false));
            fingerprint_as_button.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(PREF_FINGERPRINT_AS_BUTTON));
        }
        if (FileUtils.fileWritable(DOUBLE_TAP_TO_WAKE_PATH)) {
            SecureSettingSwitchPreference double_tap_to_wake = (SecureSettingSwitchPreference) findPreference(PREF_DOUBLE_TAP_TO_WAKE);
            double_tap_to_wake.setChecked(FileUtils.getFileValueAsBoolean(DOUBLE_TAP_TO_WAKE_PATH, false));
            double_tap_to_wake.setOnPreferenceChangeListener(this);
        } else {
            getPreferenceScreen().removePreference(findPreference(CATEGORY_GESTURES));
        }
    }

    // onPreferenceChange
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        final String key = preference.getKey();
        switch (key) {
            case PREF_TORCH_BRIGHTNESS:
                FileUtils.setValue(TORCH_1_BRIGHTNESS_PATH, (int) value);
                FileUtils.setValue(TORCH_2_BRIGHTNESS_PATH, (int) value);
                break;
                
            case PREF_VIBRATION_STRENGTH:
                double vibrationValue = (int) value / 100.0 * (MAX_VIBRATION - MIN_VIBRATION) + MIN_VIBRATION;
                FileUtils.setValue(VIBRATION_STRENGTH_PATH, vibrationValue);
                break;

            case PREF_SPECTRUM:
                mSPECTRUM.setValue((String) value);
                mSPECTRUM.setSummary(mSPECTRUM.getEntry());
                FileUtils.setStringProp(SPECTRUM_SYSTEM_PROPERTY, (String) value);
                break;

            case PREF_SWAP_BUTTONS:
                FileUtils.setValue(SWAP_BUTTONS_PATH, (boolean) value);
                break;

            case PREF_FINGERPRINT_WAKEUP:
                FileUtils.setValue(FINGERPRINT_WAKEUP_PATH, (boolean) value);
                break;

            case PREF_FINGERPRINT_AS_BUTTON:
                FileUtils.setValue(FINGERPRINT_AS_BUTTON_PATH, (boolean) value);
                break;

            case PREF_DOUBLE_TAP_TO_WAKE:
                FileUtils.setValue(DOUBLE_TAP_TO_WAKE_PATH, (boolean) value);
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
