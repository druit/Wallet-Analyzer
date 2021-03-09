package gr.ict.wallet_analyzer.helpers;

public class ListeningVariable<E> {
    private E object;
    private ChangeListener<E> listener;

    public ListeningVariable(Class<E> clazz) {
        try {
            this.object = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public ListeningVariable(E object) {
        this.object = object;
    }

    public E getObject() {
        return object;
    }

    public void setObject(E object) {
        this.object = object;
        if (listener != null) listener.onChange(object);
    }

    public ChangeListener<E> getListener() {
        return listener;
    }

    public void setListener(ChangeListener<E> listener) {
        this.listener = listener;
    }

    public interface ChangeListener<E> {
        void onChange(E object);
    }
}
