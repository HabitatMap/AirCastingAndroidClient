package pl.llp.aircasting.gson;

import pl.llp.aircasting.util.Constants;

import android.util.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalDateAdapter implements JsonDeserializer<Date>
{
    // 2012-01-29T10:22:33
    private final SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Date deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            return localFormat.parse(element.getAsString());
        } catch (ParseException e) {
            Log.e(Constants.TAG, "", e);
            throw new JsonParseException(e);
        }
    }
}