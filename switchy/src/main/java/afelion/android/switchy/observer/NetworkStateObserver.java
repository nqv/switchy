package afelion.android.switchy.observer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.lang.reflect.Method;

import afelion.android.switchy.Switchy;
import afelion.android.switchy.R;

public class NetworkStateObserver extends BaseStateObserver {
    private static final String TAG = "NetworkStateObserver";

    @Override
    public int getButtonId() {
        return R.id.button_network;
    }

    @Override
    public String[] getIntentActions() {
        return new String[] { ConnectivityManager.CONNECTIVITY_ACTION };
    }

    /**
     * Receive {@link android.net.ConnectivityManager#CONNECTIVITY_ACTION}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        setCurrentState(context, getCurrentState(context));
    }

    @Override
    protected int getCurrentState(Context context) {
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (networkInfo == null) {
            return Switchy.STATE_UNKNOWN;
        }
        return fromNetworkState(networkInfo.getState());
    }

    @Override
    protected void requestStateChange(final Context context, final boolean on) {
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Method method = ConnectivityManager.class.getDeclaredMethod(
                            "setMobileDataEnabled", boolean.class);
                    method.setAccessible(true);
                    method.invoke(connectivityManager, on);
                } catch (Exception e) {
                    Log.e(TAG, "setMobileDataEnabled: ", e);
                }
                return null;
            }
        }.execute();
    }

    private static int fromNetworkState(NetworkInfo.State state) {
        if (state == NetworkInfo.State.CONNECTED) {
            return Switchy.STATE_ENABLED;
        }
        if (state == NetworkInfo.State.DISCONNECTED) {
            return Switchy.STATE_DISABLED;
        }
        if (state == NetworkInfo.State.CONNECTING) {
            return Switchy.STATE_TURNING_ON;
        }
        if (state == NetworkInfo.State.DISCONNECTING) {
            return Switchy.STATE_TURNING_OFF;
        }
        return Switchy.STATE_UNKNOWN;
    }
}
