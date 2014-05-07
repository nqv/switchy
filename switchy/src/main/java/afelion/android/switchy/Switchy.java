package afelion.android.switchy;

import android.content.Context;
import android.content.Intent;

// This widget keeps track of two sets of states:
// "3-state": STATE_DISABLED, STATE_ENABLED, STATE_INTERMEDIATE
// "5-state": STATE_DISABLED, STATE_ENABLED, STATE_TURNING_ON, STATE_TURNING_OFF, STATE_UNKNOWN
public class Switchy {
    public static final int STATE_UNKNOWN = 0;
    public static final int STATE_DISABLED = 1;
    public static final int STATE_ENABLED = 2;
    public static final int STATE_TURNING_ON = 3;
    public static final int STATE_TURNING_OFF = 4;
    public static final int STATE_INTERMEDIATE = 5;

    public static void startActivity(Context context, String name) {
        Intent intent = new Intent(name);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
