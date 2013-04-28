package pl.llp.aircasting.tracking;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.DatabaseTaskQueue;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by ags on 28/04/2013 at 22:21
 */
@RunWith(InjectedTestRunner.class)
public class ActualNoteTrackerTest
{
  @Inject DatabaseTaskQueue queue;

  ActualNoteTracker tracker;
  private EventBus eventBus;

  @Before
  public void setUp() throws Exception
  {
    eventBus = mock(EventBus.class);
    tracker = new ActualNoteTracker(eventBus, queue);
  }

  @Test
  public void shouldNotifyListeners()
  {
    Note note = new Note();
    tracker.addNote(new Session(), note);

    NoteCreatedEvent expected = new NoteCreatedEvent(note);
    verify(eventBus, atLeastOnce()).post(Mockito.eq(expected));
  }
}
