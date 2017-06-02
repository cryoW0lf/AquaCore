package pw.eisphoenix.aquacore.service;

import pw.eisphoenix.aquacore.dependency.Injectable;

import java.util.*;

/**
 * Year: 2017
 *
 * @author Eisphoenix
 */
@Injectable
public final class ObserverService {
    private Map<Class<?>, Observable> observables = new HashMap<>();

    public final boolean notify(final Class<?> key, final Object object) {
        if (!observables.containsKey(key)) {
            return false;
        }
        observables.get(key).notifyObservers(object);
        return true;
    }

    public final Observable createObservable(final Class<?> key) {
        Observable observable = getObservable(key);
        if (observable != null) {
            return getObservable(key);
        }
        observable = new Observable();
        observables.put(key, observable);
        return observable;
    }

    public final Observable getObservable(final Class<?> key) {
        return observables.get(key);
    }

    public final boolean deleteObservable(final Class<?> key) {
        return observables.remove(key) != null;
    }

    public final void addObserver(final Class<?> key, final Observer observer) {
        createObservable(key).addObserver(observer);
    }

    public final boolean deleteObserver(final Class<?> key, final Observer observer) {
        final Observable observable = getObservable(key);
        if (observable == null) {
            return false;
        }
        observable.deleteObserver(observer);
        return true;
    }
}
