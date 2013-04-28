package pl.llp.aircasting.activity;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by ags on 20/04/2013 at 14:31
 */
public class SavingStateTest
{
  private SavingState saving;

  @Before
  public void setUp() throws Exception
  {
    saving = new SavingState();
  }

  @Test
  public void should_indicate_session_is_being_saved() throws Exception
  {
    // given
    saving.markCurrentlySaving(1);

    // when


    // then
    Assertions.assertThat(saving.isSaving()).isTrue();
  }

  @Test
  public void should_clear_saving_after_finishing() throws Exception
  {
    // given
    saving.markCurrentlySaving(1);

    // when
    saving.finished(1);

    // then
    Assertions.assertThat(saving.isSaving()).isFalse();
  }

  @Test
  public void should_indicate_particular_session_being_saved() throws Exception
  {
    // given
    saving.markCurrentlySaving(1000);

    // when


    // then
    Assertions.assertThat(saving.isSaving(1000)).isTrue();
  }

}
