package com.company.fylisten.utils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {

    public static <T> List<T> getSnapshot(@NonNull Collection<T> other){
        //WeakHashMap可以保证拿到的不会是空，这里还是做健壮性判断
        List<T> result = new ArrayList<>(other.size());
        for (T item : other) {
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}
