package pl.llp.aircasting.storage;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by ags on 03/16/13 at 19:33
 */
@RunWith(InjectedTestRunner.class)
public class DatabaseWriterIntentServiceTest
{
  @Inject DatabaseWriterService service;
  @Inject DatabaseTaskQueue taskService;

  WritableDatabaseTask task;
  Intent intent = new Intent();

  @Test
  public void should_execute_task() throws Exception
  {
    // given
    final AtomicInteger value = new AtomicInteger();
    task = new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        value.incrementAndGet();
        return null;
      }
    };
    taskService.add(task);

    // when
    service.unitOfAction();

    // then
    assertThat(value.get()).isEqualTo(1);
  }

}
