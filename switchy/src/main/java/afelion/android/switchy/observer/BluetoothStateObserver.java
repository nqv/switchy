package afelion.android.switchy.observer;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import afelion.android.switchy.Switchy;
import afelion.android.switchy.R;

public class BluetoothStateObserver extends BaseStateObserver {
    private static final String TAG = "BluetoothStateTracker";
    private static final BluetoothAdapter ADAPTER = BluetoothAdapter.getDefaultAdapter();

    @Override
    public int getButtonId() {
        return R.id.button_bluetooth;
    }

    @Override
    public String[] getIntentActions() {
        return new String[] { BluetoothAdapter.ACTION_STATE_CHANGED };
    }

    /**
     * Receive {@link android.bluetooth.BluetoothAdapter#ACTION_STATE_CHANGED}
     * @param context
     * @param intent
     * @return
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
        setCurrentState(context, fromBluetoothState(state));
    }

    @Override
    protected int getCurrentState(Context context) {
        if (ADAPTER == null) {
            return Switchy.STATE_UNKNOWN;
        }
        return fromBluetoothState(ADAPTER.getState());
    }

    @Override
    protected void requestStateChange(final Context context, final boolean on) {
        if (ADAPTER == null) {
            Log.d(TAG, "No LocalBluetoothManager");
            return;
        }
        // Actually request the Bluetooth change and persistent
        // settings write off the UI thread, as it can take a
        // user-noticeable amount of time, especially if there's
        // disk contention.
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
                if (on) {
                    ADAPTER.enable();
                } else {
                    ADAPTER.disable();
                }
                return null;
            }
        }.execute();
    }

    private static int fromBluetoothState(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_OFF:
                return Switchy.STATE_DISABLED;
            case BluetoothAdapter.STATE_ON:
                return Switchy.STATE_ENABLED;
            case BluetoothAdapter.STATE_TURNING_ON:
                return Switchy.STATE_TURNING_ON;
            case BluetoothAdapter.STATE_TURNING_OFF:
                return Switchy.STATE_TURNING_OFF;
            default:
                return Switchy.STATE_UNKNOWN;
        }
    }
}
