package com.senderman.jlogrep.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;

public class ComparableArrayDeque<T> extends ArrayDeque<T> {

    public ComparableArrayDeque() {
        super();
    }

    public ComparableArrayDeque(int numElements) {
        super(numElements);
    }

    public ComparableArrayDeque(Collection<T> c) {
        super(c);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof ArrayDeque<?> t)
            return Arrays.equals(this.toArray(), t.toArray());
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toArray());
    }
}
