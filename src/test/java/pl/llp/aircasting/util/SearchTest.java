package pl.llp.aircasting.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class SearchTest {
    List<Integer> list;
    Search.Visitor<Integer> visitor;
    int target;

    @Before
    public void setup() {
        list = newArrayList(10, 15, 16, 20, 29);
        visitor = new Search.Visitor<Integer>() {
            @Override
            public int compareTo(Integer value) {
                return new Integer(target).compareTo(value);
            }
        };
    }

    @Test
    public void shouldFindTheExactValue(){
        target = 20;
        assertThat(Search.binarySearch(list, visitor), equalTo(3));
    }

    @Test
    public void shouldFindTheClosestSmallerValueIfTheExactOneDoesntExist() {
        target = 19;
        assertThat(Search.binarySearch(list, visitor), equalTo(2));
    }
}
