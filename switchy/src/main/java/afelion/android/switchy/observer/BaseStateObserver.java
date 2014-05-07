package afelion.android.switchy.observer;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import afelion.android.switchy.Switchy;
import afelion.android.switchy.R;
import afelion.android.switchy.StateObserver;

/**
 * The state machine for a setting's toggling, tracking reality
 * versus the user's intent.
 *
 * This is necessary because reality moves relatively slowly
 * (turning on &amp; off radio drivers), compared to user's
 * expectations.
 */
public abstract class BaseStateObserver implements StateObserver {
    private static final String TAG = "BaseStateTracker";

    // Is the state in the process of changing?
    private boolean inTransition = false;
    private Boolean actualState = null;     // initially not set
    private Boolean intendedState = null;   // initially not set

    // Did a toggle request arrive while a state update was
    // already in-flight?  If so, the intendedState needs to be
    // requested when the other one is done, unless we happened to
    // arrive at that state already.
    private boolean deferredStateChangeRequestNeeded = false;

    @Override
    public void onEnabled(Context context) {
        // Do nothing
    }

    @Override
    public void onDisabled(Context context) {
        // Do nothing
    }

    /**
     * User pressed a button to change the state.  Something
     * should immediately appear to the user afterwards, even if
     * we effectively do nothing.  Their press must be heard.
     */
    @Override
    public void toggleState(Context context) {
        int currentState = getSimpleState(context);
        boolean newState = false;

        switch (currentState) {
            case Switchy.STATE_ENABLED:
                newState = false;
                break;
            case Switchy.STATE_DISABLED:
                newState = true;
                break;
            case Switchy.STATE_INTERMEDIATE:
                if (intendedState != null) {
                    newState = !intendedState;
                }
                break;
        }
        intendedState = newState;
        if (inTransition) {
            // We don't send off a transition request if we're
            // already transitioning.  Makes our state tracking
            // easier, and is probably nicer on lower levels.
            // (even though they should be able to take it...)
            deferredStateChangeRequestNeeded = true;
        } else {
            inTransition = true;
            requestStateChange(context, newState);
        }
    }

    @Override
    public void updateView(Context context, RemoteViews views) {
        int color;

        switch (getSimpleState(context)) {
            case Switchy.STATE_DISABLED:
                color = R.color.state_disabled;
                break;
            case Switchy.STATE_ENABLED:
                color = R.color.state_enabled;
                break;
            case Switchy.STATE_INTERMEDIATE:
                // In the transitional state, the bottom green bar
                // shows the tri-state (on, off, transitioning), but
                // the top dark-gray-or-bright-white logo shows the
                // user's intent.  This is much easier to see in
                // sunlight.
                if (isTurningOn()) {
                    color = R.color.state_turning_on;
                } else {
                    color = R.color.state_turning_off;
                }
                break;
            default:
                color = R.color.state_unknown;
                break;
        }
        views.setInt(getButtonId(), "setBackgroundResource", color);
    }

    /**
     * If we're in a transition mode, this returns true if we're
     * transitioning towards being enabled.
     */
    protected boolean isTurningOn() {
        return intendedState != null && intendedState;
    }

    /**
     * Sets the value that we're now in.  To be called from onActualStateChange.
     *
     * @param newState one of STATE_DISABLED, STATE_ENABLED, STATE_TURNING_ON,
     *                 STATE_TURNING_OFF, STATE_UNKNOWN
     */
    protected void setCurrentState(Context context, int newState) {
        final boolean wasInTransition = inTransition;
        switch (newState) {
            case Switchy.STATE_DISABLED:
                inTransition = false;
                actualState = false;
                break;
            case Switchy.STATE_ENABLED:
                inTransition = false;
                actualState = true;
                break;
            case Switchy.STATE_TURNING_ON:
                inTransition = true;
                actualState = false;
                break;
            case Switchy.STATE_TURNING_OFF:
                inTransition = true;
                actualState = true;
                break;
        }

        if (wasInTransition && !inTransition) {
            if (deferredStateChangeRequestNeeded) {
                Log.v(TAG, "processing deferred state change");
                if (actualState != null && intendedState != null &&
                        intendedState.equals(actualState)) {
                    Log.v(TAG, "... but intended state matches, so no changes.");
                } else if (intendedState != null) {
                    inTransition = true;
                    requestStateChange(context, intendedState);
                }
                deferredStateChangeRequestNeeded = false;
            }
        }
    }

    /**
     * Returns simplified state value from underlying 5-state.
     *
     * @param context
     * @return STATE_{ENABLED,DISABLED,INTERMEDIATE,UNKNOWN}
     */
    protected int getSimpleState(Context context) {
        if (inTransition) {
            // If we know we just got a toggle request recently
            // (which set inTransition), don't even ask the
            // underlying interface for its state.  We know we're
            // changing.  This avoids blocking the UI thread
            // during UI refresh post-toggle if the underlying
            // service state accessor has coarse locking on its
            // state (to be fixed separately).
            return Switchy.STATE_INTERMEDIATE;
        }
        switch (getCurrentState(context)) {
            case Switchy.STATE_DISABLED:
                return Switchy.STATE_DISABLED;
            case Switchy.STATE_ENABLED:
                return Switchy.STATE_ENABLED;
            default:
                return Switchy.STATE_UNKNOWN;
        }
    }

    /**
     * Gets underlying actual state.
     *
     * @param context
     * @return Constants.STATE_*.
     */
    protected abstract int getCurrentState(Context context);

    /**
     * Actually make the desired change to the underlying radio API.
     */
    protected abstract void requestStateChange(final Context context, final boolean on);
}
