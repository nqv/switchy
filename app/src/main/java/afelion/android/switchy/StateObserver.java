package afelion.android.switchy;

import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public interface StateObserver {
    /**
     * Return the ID of the main large image button for the setting.
     */
    int getButtonId();

    /**
     * Name of Intent actions of interest to this observer.
     */
    String[] getIntentActions();

    void onEnabled(Context context);

    void onDisabled(Context context);

    /**
     * Update internal state from a broadcast state change.
     */
    void onReceive(Context context, Intent intent);

    /**
     * Updates the remote views depending on the state (off, on,
     * turning off, turning on) of the setting.
     */
    void updateView(Context context, RemoteViews views);

    void toggleState(Context context);
}
