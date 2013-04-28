package pl.llp.aircasting.event.session;

import pl.llp.aircasting.model.Note;

public class NoteCreatedEvent
{
  private final Throwable origin;
  private Note note;

  public NoteCreatedEvent(Note note)
  {
    this.origin = new Throwable();
    this.note = note;
  }

  public Note getNote()
  {
    return note;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NoteCreatedEvent event = (NoteCreatedEvent) o;

    if (note != null ? !note.equals(event.note) : event.note != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return note != null ? note.hashCode() : 0;
  }
}
