package pl.llp.aircasting.storage;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static pl.llp.aircasting.storage.DatabaseTaskQueueAssertions.assertThat;

/**
 * Created by ags on 24/03/2013 at 22:35
 */
@RunWith(InjectedTestRunner.class)
public class DatabaseTaskQueueTest
{
  @Inject DatabaseTaskQueue queue;

  @Test
  public void should_start_with_an_empty_queue() throws Exception
  {
    // given

    // when

    // then
    assertThat(queue).hasSize(0);
  }

  @Test
  public void should_add_tasks_into_queue() throws Exception
  {
    // given
    WritableDatabaseTask task = mock(WritableDatabaseTask.class);

    // when
    queue.add(task);

    // then
    assertThat(queue).hasSize(1);
  }
}

