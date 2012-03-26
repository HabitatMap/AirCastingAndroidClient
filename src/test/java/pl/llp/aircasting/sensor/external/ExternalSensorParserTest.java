package pl.llp.aircasting.sensor.external;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorParserTest {
    @Inject
    ExternalSensorParser parser;

    @Test
    public void shouldParseLines() throws ParseException {
        SensorEvent actual = parser.parse("awesome sensor;Temp;degrees;C;1;2;3;4;5;12.5");
        SensorEvent expected = new SensorEvent("awesome sensor", "Temp", "degrees", "C", 1, 2, 3, 4, 5, 12.5);

        assertThat(actual, equalTo(expected));
    }

    @Test(expected = ParseException.class)
    public void shouldThrowExceptionsForMalformedLines() throws ParseException {
        parser.parse("1;2;3;4;5;6;7");
    }

    @Test(expected = ParseException.class)
    public void shouldThrowExceptionsForMalformedValues() throws ParseException {
        parser.parse("awesome sensor;temp;degrees;C;1;2;3;4;5;text");
    }

    @Test(expected = ParseException.class)
    public void shouldThrowExceptionsForMalformedThresholds() throws ParseException {
        parser.parse("awesome sensor;temp;degrees;C;1;2;text;4;5;10");
    }
}
