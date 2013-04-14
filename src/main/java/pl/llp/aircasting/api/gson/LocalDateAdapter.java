package pl.llp.aircasting.api.gson;

import pl.llp.aircasting.android.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocalDateAdapter implements JsonDeserializer<Date>, JsonSerializer<Date>
{
    // 2012-01-29T10:22:33
    private final SimpleDateFormat localFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Date deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        try {
            return localFormat.parse(element.getAsString());
        } catch (ParseException e) {
            Logger.e("Problem parsing date", e);
            throw new JsonParseException(e);
        }
    }

  @Override
  public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context)
  {
    return new JsonPrimitive(localFormat.format(src));
  }
}