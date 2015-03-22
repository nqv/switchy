package afelion.android.switchy.observer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import afelion.android.switchy.Switchy;
import afelion.android.switchy.R;

public class LocationStateObserver extends BaseStateObserver {
    private static final String TAG = "LocationStateObserver";

    @Override
    public int getButtonId() {
        return R.id.button_location;
    }

    @Override
    public String[] getIntentActions() {
        return new String[] { LocationManager.MODE_CHANGED_ACTION };
    }

    /**
     * Receive {@link android.location.LocationManager#MODE_CHANGED_ACTION}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        setCurrentState(context, getCurrentState(context));
    }

    @Override
    protected int getCurrentState(Context context) {
        final ContentResolver resolver = context.getContentResolver();

        int locationMode = Settings.Secure.getInt(resolver,
                Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
        switch (locationMode) {
            case Settings.Secure.LOCATION_MODE_BATTERY_SAVING:
            case Settings.Secure.LOCATION_MODE_OFF:
                return Switchy.STATE_DISABLED;
        }
        return Switchy.STATE_ENABLED;
    }

    // No permission to write to Secure Settings
    @Override
    public void toggleState(Context context) {
        Switchy.startActivity(context, Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Override
    protected void requestStateChange(final Context context, final boolean on) {
    }
}
