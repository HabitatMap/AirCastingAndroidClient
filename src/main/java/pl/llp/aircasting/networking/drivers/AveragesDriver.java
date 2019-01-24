/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.networking.drivers;

import com.google.gson.reflect.TypeToken;
import pl.llp.aircasting.model.internal.Region;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.networking.httpUtils.HttpResult;

import java.lang.reflect.Type;
import java.util.List;

import static java.lang.String.valueOf;
import static pl.llp.aircasting.networking.httpUtils.HttpBuilder.http;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/3/11
 * Time: 11:58 AM
 */
public class AveragesDriver {
  private static final String AVERAGES_PATH = "/api/averages.json";

  Type listType = new TypeToken<List<Region>>() {
  }.getType();

  public HttpResult<Iterable<Region>> index(Sensor sensor, double west, double north, double east, double south,
                                            int gridSizeX, int gridSizeY) {
    return http()
        .get()
        .from(AVERAGES_PATH)
        .with("q[west]", valueOf(west))
        .with("q[north]", valueOf(north))
        .with("q[east]", valueOf(east))
        .with("q[south]", valueOf(south))
        .with("q[grid_size_x]", valueOf(gridSizeX))
        .with("q[grid_size_y]", valueOf(gridSizeY))
        .with("q[sensor_name]", sensor.getSensorName())
        .with("q[measurement_type]", sensor.getMeasurementType())
        .into(listType);
  }
}
