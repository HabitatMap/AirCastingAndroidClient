package pl.llp.aircasting.sensor.external;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.ExternalSensorEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorParserTest {
    @Inject
    ExternalSensorParser parser;

    @Test
    public void shouldParseLines() throws ParseException {
        ExternalSensorEvent actual = parser.parse("Temp;degrees;C;12.5");
        ExternalSensorEvent expected = new ExternalSensorEvent("Temp", "degrees", "C", 12.5);

        assertThat(actual, equalTo(expected));
    }

    @Test(expected = ParseException.class)
    public void shouldThrowExceptionsForMalformedLines() throws ParseException {
        parser.parse("some string");
    }

    @Test(expected = ParseException.class)
    public void shouldThrowExceptionsForMalformedValues() throws ParseException {
        parser.parse("temp;degrees;C;text");
    }
}
