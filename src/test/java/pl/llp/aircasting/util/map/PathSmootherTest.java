package pl.llp.aircasting.util.map;

import com.google.android.maps.GeoPoint;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/27/12
 * Time: 5:05 PM
 */
@RunWith(InjectedTestRunner.class)
public class PathSmootherTest {
    @Inject PathSmoother smoother;
    List<GeoPoint> points;

    @Before
    public void setup() {
        points = newArrayList(
                new GeoPoint(0, 0),
                new GeoPoint(1, 1),
                new GeoPoint(2, 1),
                new GeoPoint(1000, 1000),
                new GeoPoint(-1000, 1000)
        );
    }

    @Test
    public void shouldKeepEndPoints() {
        assertThat(smoother.getSmoothed(points), hasItem(new GeoPoint(0, 0)));
        assertThat(smoother.getSmoothed(points), hasItem(new GeoPoint(-1000, 1000)));
    }

    @Test
    public void shouldKeepCorePoints() {
        assertThat(smoother.getSmoothed(points), hasItem(new GeoPoint(1000, 1000)));
    }

    @Test
    public void shouldRemoveSuperfluousPoints() {
        assertThat(smoother.getSmoothed(points), not(hasItem(new GeoPoint(1, 1))));
        assertThat(smoother.getSmoothed(points), not(hasItem(new GeoPoint(1, 2))));
    }
}
