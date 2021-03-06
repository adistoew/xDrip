package com.eveningoutpost.dexdrip.utils;

import android.app.PendingIntent;
import android.content.Intent;

import com.eveningoutpost.dexdrip.Home;
import com.eveningoutpost.dexdrip.Models.JoH;
import com.eveningoutpost.dexdrip.Models.UserError;
import com.eveningoutpost.dexdrip.UtilityModels.NotificationChannels;
import com.eveningoutpost.dexdrip.xdrip;

import static com.eveningoutpost.dexdrip.Models.JoH.cancelNotification;
import static com.eveningoutpost.dexdrip.Models.JoH.showNotification;

/**
 * Created by jamorham on 26/01/2017.
 */

public class CheckBridgeBattery {

    private static final String TAG = CheckBridgeBattery.class.getSimpleName();
    private static final String PREFS_ITEM = "bridge_battery";
    private static final String PARAKEET_PREFS_ITEM = "parakeet_battery";
    private static final int NOTIFICATION_ITEM = 541;
    private static final int PARAKEET_NOTIFICATION_ITEM = 542;
    private static int last_level = -1;
    private static int last_parakeet_level = -1;
    private static boolean notification_showing = false;
    private static boolean parakeet_notification_showing = false;
    private static int threshold = 20;
    private static final int repeat_seconds = 1200;

    public static boolean checkBridgeBattery() {

        boolean lowbattery = false;

        if (!Home.getPreferencesBooleanDefaultFalse("bridge_battery_alerts")) return false;

        try {
            threshold = Integer.parseInt(Home.getPreferencesStringWithDefault("bridge_battery_alert_level", "30"));
        } catch (NumberFormatException e) {
            UserError.Log.e(TAG, "Got error parsing alert level");
        }

        final int this_level = Home.getPreferencesInt("bridge_battery", -1);
        if ((this_level > 0) && (threshold > 0)) {
            if ((this_level < threshold) && (this_level < last_level)) {
                if (JoH.pratelimit("bridge-battery-warning", repeat_seconds)) {
                    notification_showing = true;
                    lowbattery = true;
                    final PendingIntent pendingIntent = android.app.PendingIntent.getActivity(xdrip.getAppContext(), 0, new Intent(xdrip.getAppContext(), Home.class), android.app.PendingIntent.FLAG_UPDATE_CURRENT);
                    showNotification("Low bridge battery", "Bridge battery dropped to: " + this_level + "%",
                            pendingIntent, NOTIFICATION_ITEM, NotificationChannels.LOW_BRIDGE_BATTERY_CHANNEL, true, true, null, null, null);
                }
            } else {
                if (notification_showing) {
                    cancelNotification(NOTIFICATION_ITEM);
                    notification_showing = false;
                }
            }
            last_level = this_level;
        }
        return lowbattery;
    }


    public static void checkParakeetBattery() {

        if (!Home.getPreferencesBooleanDefaultFalse("bridge_battery_alerts")) return;

        try {
            threshold = Integer.parseInt(Home.getPreferencesStringWithDefault("bridge_battery_alert_level", "30"));
        } catch (NumberFormatException e) {
            UserError.Log.e(TAG, "Got error parsing alert level");
        }

        final int this_level = Home.getPreferencesInt(PARAKEET_PREFS_ITEM, -1);
        if ((this_level > 0) && (threshold > 0)) {
            if ((this_level < threshold) && (this_level < last_parakeet_level)) {
                if (JoH.pratelimit("parakeet-battery-warning", repeat_seconds)) {
                    parakeet_notification_showing = true;
                    final PendingIntent pendingIntent = android.app.PendingIntent.getActivity(xdrip.getAppContext(), 0, new Intent(xdrip.getAppContext(), Home.class), android.app.PendingIntent.FLAG_UPDATE_CURRENT);
                    showNotification("Low Parakeet battery", "Parakeet battery dropped to: " + this_level + "%",
                            pendingIntent, PARAKEET_NOTIFICATION_ITEM, NotificationChannels.LOW_BRIDGE_BATTERY_CHANNEL, true, true, null, null, null);
                }
            } else {
                if (parakeet_notification_showing) {
                    cancelNotification(PARAKEET_NOTIFICATION_ITEM);
                    parakeet_notification_showing = false;
                }
            }
            last_parakeet_level = this_level;
        }
    }


    public static void testHarness() {
        if (Home.getPreferencesInt(PREFS_ITEM, -1) < 1)
            Home.setPreferencesInt(PREFS_ITEM, 60);
        Home.setPreferencesInt(PREFS_ITEM, Home.getPreferencesInt(PREFS_ITEM, 0) - (int) (JoH.tsl() % 15));
        UserError.Log.d(TAG, "Bridge battery: " + Home.getPreferencesInt(PREFS_ITEM, 0));
        checkBridgeBattery();
    }


}
