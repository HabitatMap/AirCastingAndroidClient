package pl.llp.aircasting.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;

public class SimpleSensorAdapter extends ArrayAdapter<Sensor> {
    public SimpleSensorAdapter(Context context, int textViewResourceId, Sensor[] objects) {
        super(context, textViewResourceId, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Sensor sensor = getItem(position);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(sensor.getMeasurementType() + " - " + sensor.getSensorName());

        return view;
    }
}