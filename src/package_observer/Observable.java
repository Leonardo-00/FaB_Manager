package package_observer;

public abstract class Observable {
    public abstract void addObserver(Observer o);
    public abstract void removeObserver(Observer o);
    protected abstract void notifyObservers();
}
