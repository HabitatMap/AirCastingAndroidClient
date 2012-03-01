package pl.llp.aircasting.util;

import java.util.List;

public class Search {
    public static <T> int binarySearch(List<? extends T> sortedList, Visitor<T> visitor) {
        int low = 0;
        int high = sortedList.size();

        while (high - low > 1) {
            int mid = (high + low) / 2;
            T value = sortedList.get(mid);

            if (visitor.compareTo(value) >= 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return low;
    }

    public static interface Visitor<T> {
        public int compareTo(T value);
    }
}
