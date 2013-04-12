package pl.llp.aircasting.repository;

import org.fest.assertions.Assertions;
import org.junit.Before;

/**
 * Created by ags on 24/03/2013 at 22:38
 */
public class DatabaseTaskQueueAssertions extends Assertions
{
  private DatabaseTaskQueue queue;

  @Before
  public void setUp() throws Exception
  {
    queue = new DatabaseTaskQueue();
  }

  public DatabaseTaskQueueAssertions(DatabaseTaskQueue queue)
  {
    this.queue = queue;
  }

  public static DatabaseTaskQueueAssertions assertThat(DatabaseTaskQueue queue)
  {
    return new DatabaseTaskQueueAssertions(queue);
  }

  public DatabaseTaskQueueAssertions hasSize(int expected)
  {
    assertThat(queue.writables).hasSize(expected);
    return this;
  }
}
