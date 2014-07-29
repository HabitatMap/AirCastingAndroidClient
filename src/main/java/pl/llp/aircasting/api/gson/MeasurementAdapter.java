package pl.llp.aircasting.api.gson;

import pl.llp.aircasting.model.Measurement;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

public class MeasurementAdapter implements JsonDeserializer<Measurement>, JsonSerializer<Measurement>
{
  LocalDateAdapter dateAdapter = new LocalDateAdapter();

  @Override
  public Measurement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
  {
    JsonObject object = json.getAsJsonObject();
    double latitude = asDouble(object, "latitude");
    double longitude = asDouble(object, "longitude");
    double value = asDouble(object, "value");
    int millisecondPart = asInt(object, "milliseconds");
    int timezoneOffset = asInt(object, "timezone_offset");
    double measuredValue = asDouble(object, "measured_value");
    Date date = dateAdapter.deserialize(object.get("time"), Date.class, context);

    long milliseconds = date.getTime();
    milliseconds += millisecondPart;
    date = new Date(milliseconds);

    Measurement measurement = new Measurement(latitude, longitude, value, measuredValue, date);
    measurement.setTimeZoneOffsetMinutes(timezoneOffset);
    return measurement;
  }

  private double asDouble(JsonObject object, String name)
  {
    if (object.isJsonNull()) return 0;

    JsonElement element = object.get(name);
    if (element != null && !element.isJsonNull())
    {
      return element.getAsDouble();
    }
    else
    {
      return 0;
    }
  }

  private int asInt(JsonObject object, String name)
  {
    JsonElement element = object.get(name);
    if (element != null)
    {
      return element.getAsInt();
    }
    else
    {
      return 0;
    }
  }

  @Override
  public JsonElement serialize(Measurement src, Type typeOfSrc, JsonSerializationContext context)
  {
    JsonObject result = new JsonObject();

    result.addProperty("longitude", src.getLongitude());
    result.addProperty("latitude", src.getLatitude());
    result.addProperty("time", context.serialize(src.getTime()).getAsString());
    result.addProperty("timezone_offset", src.getTimeZoneOffsetMinutes());
    result.addProperty("milliseconds", src.getMilliseconds());
    result.addProperty("measured_value", src.getMeasuredValue());
    result.addProperty("value", src.getValue());

    return result;
  }
}
