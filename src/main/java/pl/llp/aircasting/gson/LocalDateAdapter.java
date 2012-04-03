package pl.llp.aircasting.gson;

import android.util.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/10/12
 * Time: 3:00 PM
 */
public class LocalDateAdapter implements JsonDeserializer<Date> {
    private static final String TAG = LocalDateAdapter.class.getSimpleName();

    // 2012-01-29T10:22:33
    private final SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Date deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            return localFormat.parse(element.getAsString());
        } catch (ParseException e) {
            Log.e(TAG, "", e);
            throw new JsonParseException(e);
        }
    }
}