package pl.llp.aircasting.screens.sessions;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.FormatHelper;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionState;
import pl.llp.aircasting.model.MeasurementStream;
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

/**
 * Created by ags on 05/07/12 at 21:06
 */
public class SessionAdapter extends ArrayAdapter {
    private static final int[] regular_backgrounds = new int[]{R.drawable.session_list_odd, R.drawable.session_list_even};

    private ResourceHelper resourceHelper;
    private Context context;
    private SessionState sessionState;

    List<Session> sessions = newArrayList();

    public SessionAdapter(Context context, SessionState sessionState) {
        super(context, R.layout.session_row, R.id.session_title);

        this.context = context;
        this.sessionState = sessionState;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Session session = sessions.get(position);

        Drawable background = getBackground(position, session.getId());
        view.setBackgroundDrawable(background);

        fillTitle(view, context, session);
        fillIcons(view, session);
        fillStats(view, session);
        fillTypes(view, session);

        return view;
    }

    private void fillIcons(View view, Session session) {
        ImageView imageView = (ImageView) view.findViewById(R.id.session_icon);
        imageView.setImageResource(session.getDrawable());
    }

    private void fillStats(View view, Session session) {
        ((TextView) view.findViewById(R.id.session_time)).setText(FormatHelper.timeText(session));

        view.findViewById(R.id.avg_container).setVisibility(View.GONE);
        view.findViewById(R.id.peak_container).setVisibility(View.GONE);
    }

    private Drawable getBackground(int position, Long sessionId) {
        int id;

        if (sessionState.isSessionBeingViewed(sessionId) || sessionState.isSessionLoading(sessionId)) {
            id = R.drawable.session_list_marked_even;
        } else {
            id = regular_backgrounds[position % regular_backgrounds.length];
        }

        return context.getResources().getDrawable(id);
    }

    private void fillTitle(View view, Context context, Session session) {
        TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);
        if (session.getTitle() != null && session.getTitle().length() > 0) {
            sessionTitle.setText(session.getTitle());
        } else {
            String unnamed = context.getString(R.string.unnamed_session);
            sessionTitle.setText(Html.fromHtml(unnamed));
        }
    }

    private void fillTypes(View view, Session session) {
        TextView dataTypes = (TextView) view.findViewById(R.id.data_types);

        Iterable<String> types = transform(session.getActiveMeasurementStreams(), new Function<MeasurementStream, String>() {
            @Override
            public String apply(MeasurementStream input) {
                return input.getShortType();
            }
        });
        types = Ordering.natural().sortedCopy(types);

        String text = Joiner.on("/").join(types);
        dataTypes.setText(text);
    }

    public void setResourceHelper(ResourceHelper resourceHelper) {
        this.resourceHelper = resourceHelper;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
        super.clear();
        for (Session session : sessions) {
            super.add(session);
        }
        notifyDataSetChanged();
    }

    public Session getSession(int position) {
        return sessions.get(position);
    }
}
