package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.sensor.BluetoothSocketReader;

import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by ags on 01/10/12 at 17:00
 */
public class LineDataReader implements BluetoothSocketReader {
    final BluetoothSocket socket;
    final String address;

    ExternalSensorParser parser = new ExternalSensorParser();
    EventBus eventBus;

    public LineDataReader(BluetoothSocket socket, String address) {
        this.socket = socket;
        this.address = address;
    }

    public void read() throws IOException {
        InputStream stream = socket.getInputStream();
        final Reader finalReader = new InputStreamReader(stream);

        CharStreams.readLines(
                inputSupplier(finalReader),
                lineProcessor());
    }

    void process(String line) {
        try {
            SensorEvent event = parser.parse(line);
            event.setAddress(getAddress());
            eventBus.post(event);
        } catch (ParseException e) {
            Logger.e("External sensor error", e);
        }
    }

    private LineProcessor<Void> lineProcessor() {
        return new LineProcessor<Void>() {
            @Override
            public boolean processLine(String line) throws IOException {
                process(line);
                return !Thread.interrupted();
            }

            @Override
            public Void getResult() {
                return null;
            }
        };
    }

    private InputSupplier<Reader> inputSupplier(final Reader finalReader) {
        return new InputSupplier<Reader>() {
            @Override
            public Reader getInput() throws IOException {
                return finalReader;
            }
        };
    }

    @Override
    public void setBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public String getAddress() {
        return address;
    }
}
