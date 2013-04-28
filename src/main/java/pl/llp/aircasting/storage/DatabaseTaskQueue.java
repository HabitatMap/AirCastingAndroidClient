package pl.llp.aircasting.storage;

import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import com.google.inject.Singleton;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.collect.Queues.newConcurrentLinkedQueue;

@Singleton
public class DatabaseTaskQueue
{
  final ConcurrentLinkedQueue<WritableDatabaseTask> writables = newConcurrentLinkedQueue();

  public WritableDatabaseTask getFirst()
  {
    return writables.remove();
  }

  public boolean somethingAvailable()
  {
    return !writables.isEmpty();
  }

  public void add(WritableDatabaseTask task)
  {
    writables.add(task);
  }
}
