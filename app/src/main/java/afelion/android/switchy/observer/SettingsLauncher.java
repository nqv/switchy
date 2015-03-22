package afelion.android.switchy.observer;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.RemoteViews;

import afelion.android.switchy.R;
import afelion.android.switchy.StateObserver;
import afelion.android.switchy.Switchy;

public class SettingsLauncher implements StateObserver {
    @Override
    public int getButtonId() {
        return R.id.button_settings;
    }

    @Override
    public String[] getIntentActions() {
        return null;
    }

    @Override
    public void onEnabled(Context context) {
        // Do nothing
    }

    @Override
    public void onDisabled(Context context) {
        // Do nothing
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Do nothing
    }

    @Override
    public void updateView(Context context, RemoteViews views) {
        // Do nothing
    }

    @Override
    public void toggleState(Context context) {
        Switchy.startActivity(context, Settings.ACTION_SETTINGS);
    }
}
