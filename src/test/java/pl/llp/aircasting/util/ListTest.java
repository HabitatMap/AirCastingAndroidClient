package pl.llp.aircasting.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(InjectedTestRunner.class)
public class ListTest {
    @Test
    public void shouldContainTheTail() {
        Iterable<Integer> result = Lists.getLast(newArrayList(1, 2, 3, 4), 2);

        assertThat(result, hasItem(3));
        assertThat(result, hasItem(4));
    }

    @Test
    public void shouldOnlyContainTheTail() {
        Iterable<Integer> result = Lists.getLast(newArrayList(1, 2, 3, 4), 2);

        assertThat(result, not(hasItem(1)));
        assertThat(result, not(hasItem(2)));
    }

    @Test
    public void shouldReturnTheWholeListIfItIsSmall() {
        Iterable<Integer> result = Lists.getLast(newArrayList(1), 2);
        
        assertThat(result,  hasItem(1));
    }
}
