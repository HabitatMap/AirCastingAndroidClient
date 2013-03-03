package pl.llp.aircasting.sensor.bioharness;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by ags on 03/03/13 at 10:25
 */
public class PreviousValueTrackerTest
{
  private RepeatedValueTracker tracker;

  @Before
  public void setUp() throws Exception
  {
    tracker = new RepeatedValueTracker();
  }

  @Test
  public void should_mark_first_value_as_new() throws Exception
  {
    // then
    assertThat(tracker.isNew(1)).isTrue();
  }

  @Test
  public void should_mark_repeated_value_as_ole() throws Exception
  {
    // when
    assertThat(tracker.isNew(1)).isTrue();

    // then
    assertThat(tracker.isNew(1)).isFalse();
  }

  @Test
  public void should_mark_new_value_as_new() throws Exception
  {
    // when
    assertThat(tracker.isNew(1)).isTrue();

    // then
    assertThat(tracker.isNew(2)).isTrue();
  }
}
