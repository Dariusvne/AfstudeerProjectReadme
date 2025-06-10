package com.swisscom.travelmate.engine.shared.util;

import com.swisscom.travelmate.engine.modules.message.model.DispatchResult;
import com.swisscom.travelmate.engine.shared.monitoring.Observer;

import java.util.List;

public class ObserverUtils {
    public static <T> void notifyObservers(Boolean success, T payload, String contextType, List<Observer<T>> observers) {
        for (Observer<T> observer : observers) {
            if (success) {
                observer.onSuccess(payload, contextType);
            } else {
                observer.onFailure(payload, contextType);
            }
        }
    }
}
