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
package pl.llp.aircasting.guice;

import pl.llp.aircasting.model.SchemaMigrator;
import pl.llp.aircasting.util.http.HttpBuilder;
import pl.llp.aircasting.view.overlay.NoteOverlay;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.Geocoder;
import android.telephony.TelephonyManager;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.inject.Scopes;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import roboguice.config.AbstractAndroidModule;
import roboguice.inject.SharedPreferencesName;
import roboguice.inject.SystemServiceProvider;

public class AirCastingModule extends AbstractAndroidModule
{
    @Override
    protected void configure() {
        requestStaticInjection(HttpBuilder.class);

        bind(Gson.class).toProvider(GsonProvider.class).in(Scopes.SINGLETON);

        bind(HttpClient.class).to(DefaultHttpClient.class);

        bind(SchemaMigrator.class).toProvider(DBHelperProvider.class);

        bind(NoteOverlay.class).toProvider(NoteOverlayProvider.class);
        
        bind(Geocoder.class).toProvider(GeocoderProvider.class);
        
        bind(EventBus.class).in(Scopes.SINGLETON);

        bindConstant().annotatedWith(SharedPreferencesName.class).to("pl.llp.aircasting_preferences");
        
        bind(BluetoothAdapter.class).toProvider(BluetoothAdapterProvider.class);

        bind(TelephonyManager.class).toProvider(new SystemServiceProvider<TelephonyManager>(Context.TELEPHONY_SERVICE));
    }
}
