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
package pl.llp.aircasting.activity;

import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;
import pl.llp.aircasting.util.Constants;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import com.google.inject.Inject;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static pl.llp.aircasting.helper.TextViewHelper.stripUnderlines;

public class AboutActivity extends RoboActivity
{
  public static final String HEADING = "heading";

  @Inject MainMenu mainMenu;
  @Inject LayoutInflater layoutInflater;

  @InjectView(R.id.about) ExpandableListView about;

  @Inject SyncBroadcastReceiver syncBroadcastReceiver;

  private String[] headings;
  private String[] contents;

  public void initializeSections() {
    headings = new String[]{
        getString(R.string.about_description),
        getString(R.string.monitor_air_quality),
        getString(R.string.monitor_heart_rate),
        getString(R.string.aircasting_luminescence),
        getString(R.string.hardware_developers),
        getString(R.string.connect_external_device),
        getString(R.string.about_profile),
        getString(R.string.about_start),
        getString(R.string.about_view_data),
        getString(R.string.about_note),
        getString(R.string.about_stop),
        getString(R.string.about_contribute),
        getString(R.string.about_view_sessions),
        getString(R.string.about_open_session),
        getString(R.string.about_heat_legend),
        getString(R.string.about_adjust_microphone),
        getString(R.string.disable_maps),
        getString(R.string.about_habitat_map),
        getString(R.string.about_open_source),
        getString(R.string.about_thanks),
        getString(R.string.about_funders),
        getString(R.string.about_feedback),
        getString(R.string.about_version)
    };
    contents = new String[]{
        getString(R.string.about_description_content),
        getString(R.string.monitor_air_quality_content),
        getString(R.string.monitor_heart_rate_content),
        getString(R.string.aircasting_luminescence_content),
        getString(R.string.hardware_developers_content),
        getString(R.string.connect_external_device_content),
        getString(R.string.about_profile_content),
        getString(R.string.about_start_content),
        getString(R.string.about_view_data_content),
        getString(R.string.about_note_content),
        getString(R.string.about_stop_content),
        getString(R.string.about_contribute_content),
        getString(R.string.about_view_sessions_content),
        getString(R.string.about_open_session_content),
        getString(R.string.about_heat_legend_content),
        getString(R.string.about_adjust_microphone_content),
        getString(R.string.disable_maps_content),
        getString(R.string.about_habitat_map_content),
        getString(R.string.about_open_source_content),
        getString(R.string.about_thanks_content),
        getString(R.string.about_funders_content),
        getString(R.string.about_feedback_content),
        getVersion()
    };
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.about);

    initializeSections();
    initializeAbout();
  }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(syncBroadcastReceiver);
    }

    private void initializeAbout() {
        ExpandableListAdapter adapter = new AboutAdapter();

        about.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mainMenu.create(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mainMenu.handleClick(this, item);
    }

    private List<Map<String, String>> headings() {
        ArrayList<Map<String, String>> result = new ArrayList<Map<String, String>>();

        for (String heading : headings) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put(HEADING, heading);
            result.add(map);
        }

        return result;
    }

    private String getVersion() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(Constants.TAG, "Error while fetching app version", e);
            return "?";
        }
    }

    private class AboutAdapter extends SimpleExpandableListAdapter {
        public AboutAdapter() {
            super(AboutActivity.this, headings(), R.layout.about_heading, new String[]{HEADING}, new int[]{R.id.heading}, null, 0, null, null);
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.about_content, null);
            }

            String text = contents[groupPosition];
            Spanned spanned = Html.fromHtml(text);
            spanned = stripUnderlines(spanned);

            TextView view = (TextView) convertView.findViewById(R.id.content);
            view.setText(spanned);
            view.setMovementMethod(LinkMovementMethod.getInstance());

            return convertView;
        }
    }
}
