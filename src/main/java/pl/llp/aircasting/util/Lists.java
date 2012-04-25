package pl.llp.aircasting.util;

import java.util.List;

import static com.google.common.collect.Iterables.skip;

public class Lists {
    public static <T> Iterable<T> getLast(List<T> list, int n) {
        int toSkip = list.size() - n;
        return skip(list, Math.max(toSkip, 0));
    }
}
