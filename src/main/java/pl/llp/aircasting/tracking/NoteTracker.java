package pl.llp.aircasting.tracking;

import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;

/**
 * Created by ags on 03/14/13 at 23:31
 */
public interface NoteTracker
{
  void addNote(Session session, Note note);

  void deleteNote(Session sessionId, Note note);
}
