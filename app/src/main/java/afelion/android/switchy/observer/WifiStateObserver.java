package afelion.android.switchy.observer;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Method;

import afelion.android.switchy.Switchy;
import afelion.android.switchy.R;

public class WifiStateObserver extends BaseStateObserver {
    private static final String TAG = "WifiStateTracker";

    @Override
    public int getButtonId() {
        return R.id.button_wifi;
    }

    @Override
    public String[] getIntentActions() {
        return new String[] { WifiManager.WIFI_STATE_CHANGED_ACTION };
    }

    /**
     * Receive {@link android.net.wifi.WifiManager#WIFI_STATE_CHANGED_ACTION}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
        setCurrentState(context, fromWifiState(state));
    }

    @Override
    public int getCurrentState(Context context) {
        final WifiManager wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return Switchy.STATE_UNKNOWN;
        }
        return fromWifiState(wifiManager.getWifiState());
    }

    @Override
    protected void requestStateChange(final Context context, final boolean on) {
        final WifiManager wifiManager = (WifiManager)
                context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.d(TAG, "No wifiManager.");
            return;
        }

        // Actually request the wifi change and persistent
        // settings write off the UI thread, as it can take a
        // user-noticeable amount of time, especially if there's
        // disk contention.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                // Disable tethering if enabling Wifi
                if (on) {
                    try {
                        // API has not been open yet
                        Method method = WifiManager.class.getDeclaredMethod("getWifiApState");
                        method.setAccessible(true);
                        int wifiApState = (Integer) method.invoke(wifiManager);
                        final int WIFI_AP_STATE_ENABLING = WifiManager.class
                                .getDeclaredField("WIFI_AP_STATE_ENABLING").getInt(null);
                        final int WIFI_AP_STATE_ENABLED = WifiManager.class
                                .getDeclaredField("WIFI_AP_STATE_ENABLED").getInt(null);
                        if (wifiApState == WIFI_AP_STATE_ENABLING ||
                                wifiApState == WIFI_AP_STATE_ENABLED) {
                            method = WifiManager.class.getDeclaredMethod(
                                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
                            method.setAccessible(true);
                            method.invoke(wifiManager, null, false);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "getWifiApState: ", e);
                    }
                }
                wifiManager.setWifiEnabled(on);
                return null;
            }
        }.execute();
    }

    /**
     * Converts WifiManager's state values into our
     * Wifi/Bluetooth-common state values.
     */
    private static int fromWifiState(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_DISABLED:
                return Switchy.STATE_DISABLED;
            case WifiManager.WIFI_STATE_ENABLED:
                return Switchy.STATE_ENABLED;
            case WifiManager.WIFI_STATE_DISABLING:
                return Switchy.STATE_TURNING_OFF;
            case WifiManager.WIFI_STATE_ENABLING:
                return Switchy.STATE_TURNING_ON;
            default:
                return Switchy.STATE_UNKNOWN;
        }
    }
}
