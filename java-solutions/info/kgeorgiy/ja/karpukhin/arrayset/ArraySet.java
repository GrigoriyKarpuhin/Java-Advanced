package info.kgeorgiy.ja.karpukhin.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T> {
    private final List<T> elements;
    private final Comparator<? super T> comparator;

    public ArraySet() {
        elements = Collections.emptyList();
        comparator = null;
    }

    @Override
    public Iterator<T> iterator() {
        return elements.iterator();
    }

    @Override
    public int size() {
        return elements.size();
    }
    public ArraySet(Collection<? extends T> collection) {
        elements = new ArrayList<>(new TreeSet<>(collection));
        comparator = null;
    }

    public ArraySet(Collection<? extends T> collection, Comparator<? super T> comparator) {
        TreeSet<T> set = new TreeSet<>(comparator);
        set.addAll(collection);
        elements = new ArrayList<>(set);
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    private int getIndex(T element) {
        int index = Collections.binarySearch(elements, element, comparator);
        if (index < 0) {
            index = -index - 1;
        }
        return index;
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return new ArraySet<>(elements.subList(0, getIndex(toElement)), comparator);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return new ArraySet<>(elements.subList(getIndex(fromElement), elements.size()), comparator);
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        if (comparator != null && comparator.compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException("fromElement > toElement");
        }
        return tailSet(fromElement).headSet(toElement);
    }

    @Override
    public T first() {
        return elements.getFirst();
    }

    @Override
    public T last() {
        return elements.getLast();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(elements, (T) o, comparator) >= 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }
}
