package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.FormatHelper;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.valueOf;

/**
 * Created by ags on 05/07/12 at 21:06
 */
public class SessionAdapter extends ArrayAdapter
{
  private static final int[] backgrounds = new int[]{R.drawable.session_list_odd, R.drawable.session_list_even};

  private ResourceHelper resourceHelper;
  private Context context;
  Sensor sensor;
  List<Session> sessions = newArrayList();

  public SessionAdapter(Context context)
  {
    super(context, R.layout.session_row, R.id.session_title);
    this.context = context;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent)
  {
    View view = super.getView(position, convertView, parent);

    Session session = sessions.get(position);

    Drawable background = evenOddBackground(position);
    view.setBackgroundDrawable(background);

    fillTitle(view, context, session);
    fillStats(view, session);
    fillTypes(view, session);

    return view;
  }

  private void fillStats(View view, Session session) {
    ((TextView) view.findViewById(R.id.session_time)).setText(FormatHelper.timeText(session));

    if (sensor == null) {
      view.findViewById(R.id.avg_container).setVisibility(View.GONE);
      view.findViewById(R.id.peak_container).setVisibility(View.GONE);
    } else {
      view.findViewById(R.id.avg_container).setVisibility(View.VISIBLE);
      view.findViewById(R.id.peak_container).setVisibility(View.VISIBLE);

      String name = sensor.getSensorName();
      MeasurementStream stream = session.getStream(name);
      int peak = (int) stream.getPeak();
      int avg = (int) stream.getAvg();

      String type = stream.getShortType();
      ((TextView) view.findViewById(R.id.session_peak)).setText(valueOf(peak) + " " + type);
      ((TextView) view.findViewById(R.id.session_average)).setText(valueOf(avg) + " " + type);

      updateImage((ImageView) view.findViewById(R.id.session_average_marker), avg);
      updateImage((ImageView) view.findViewById(R.id.session_peak_marker), peak);
    }
  }

  private Drawable evenOddBackground(int position)
  {
    int id = backgrounds[position % backgrounds.length];
    return context.getResources().getDrawable(id);
  }

  private void fillTitle(View view, Context context, Session session)
  {
    TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);
    if (session.getTitle() != null && session.getTitle().length() > 0)
    {
      sessionTitle.setText(session.getTitle());
    }
    else
    {
      String unnamed = context.getString(R.string.unnamed_session);
      sessionTitle.setText(Html.fromHtml(unnamed));
    }
  }

  private void updateImage(ImageView view, double value)
  {
    Drawable bullet = resourceHelper.getBulletAbsolute(sensor, value);
    view.setBackgroundDrawable(bullet);
  }

  private void fillTypes(View view, Session session)
  {
    TextView dataTypes = (TextView) view.findViewById(R.id.data_types);

    if (sensor == null)
    {
      dataTypes.setVisibility(View.VISIBLE);

      Iterable<String> types = transform(session
                                             .getActiveMeasurementStreams(), new Function<MeasurementStream, String>()
      {
        @Override
        public String apply(MeasurementStream input)
        {
          return input.getShortType();
        }
      });
      types = Ordering.natural().sortedCopy(types);

      String text = Joiner.on("/").join(types);
      dataTypes.setText(text);
    }
    else
    {
      dataTypes.setVisibility(View.GONE);
    }
  }

  public void setResourceHelper(ResourceHelper resourceHelper)
  {
    this.resourceHelper = resourceHelper;
  }

  public void setSessions(List<Session> sessions)
  {
    this.sessions = sessions;
    super.clear();
    for (Session session : sessions)
    {
      super.add(session);
    }
    notifyDataSetChanged();
  }
}
