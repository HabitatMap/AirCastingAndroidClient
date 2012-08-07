package pl.llp.aircasting.helper;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class CSVHelperTest
{
  private CSVHelper helper;

  @Before
  public void setUp() throws Exception
  {
    helper = new CSVHelper();
  }

  @Test
  public void should_includeLegalCharactersInSessionFileName() throws Exception
  {
    // given
    String s = helper.fileName("az_AZ09-!@#asd");

    // when

    // then
    assertEquals("az_az09-asd.csv", s);
  }

  @Test
  public void should_failSafeToDefaultNameForIllegalNames() throws Exception
  {
    // given
    String s = helper.fileName("@#");

    // when

    // then
    assertEquals(CSVHelper.SESSION_TEMP_FILE, s);
  }

  @Test
  public void should_failSafeToDefaultNameForEmptyNames() throws Exception
  {
    // given
    String s = helper.fileName("");

    // when

    // then
    assertEquals(CSVHelper.SESSION_TEMP_FILE, s);
  }
}
