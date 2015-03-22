package afelion.android.switchy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StateObserverList {
    private Map<Integer, StateObserver> buttonIds = new HashMap<Integer, StateObserver>();
    private Map<String, StateObserver> intentActions = new HashMap<String, StateObserver>();

    public void addObserver(StateObserver observer) {
        buttonIds.put(observer.getButtonId(), observer);
        String[] handledActions = observer.getIntentActions();
        if (handledActions != null) {
            for (String action : handledActions) {
                intentActions.put(action, observer);
            }
        }
    }

    public StateObserver getByButtonId(int buttonId) {
        return buttonIds.get(buttonId);
    }

    public StateObserver getByIntentAction(String action) {
        return intentActions.get(action);
    }

    public Collection<StateObserver> getObservers() {
        return buttonIds.values();
    }

    public void clear() {
        buttonIds.clear();
        intentActions.clear();
    }
}
