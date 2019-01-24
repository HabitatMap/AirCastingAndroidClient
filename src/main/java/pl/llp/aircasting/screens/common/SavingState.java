package pl.llp.aircasting.screens.common;

import java.util.HashSet;

import static com.google.common.collect.Sets.newHashSet;

/**
* Created by ags on 20/04/2013 at 14:31
*/
public class SavingState
{
  public static final HashSet<Long> ids = newHashSet();

  public boolean isSaving()
  {
    return !ids.isEmpty();
  }

  public void markCurrentlySaving(long sessionId)
  {
    ids.add(sessionId);
  }

  public void finished(long sessionId)
  {
    ids.remove(sessionId);
  }

  public boolean isSaving(long id)
  {
    return ids.contains(id);
  }
}
