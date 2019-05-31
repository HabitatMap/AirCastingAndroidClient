package pl.llp.aircasting.screens.sessions.shareSession;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;

public class ShareSessionViewMvcImpl implements BaseViewMvc, View.OnClickListener {
    public View mRootView;

    private final View mShareFile;
    private final View mShareLink;
    private final RecyclerView mSelectSensor;
    private View.OnClickListener mListener;

    public ShareSessionViewMvcImpl(AppCompatActivity context, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        mRootView = inflater.inflate(R.layout.share_session, parent, false);
        mShareFile = findViewById(R.id.share_file);
        mShareLink = findViewById(R.id.share_link);
        mSelectSensor = findViewById(R.id.select_sensor);

        mShareFile.setOnClickListener(this);
        mShareLink.setOnClickListener(this);
    }

    public void registerListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public void bindData(List data) {

    }

    public void toggleLink(boolean isLocationless) {
        if (isLocationless) {
            mShareLink.setVisibility(View.GONE);
        } else {
            mShareLink.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    @Override
    public void onClick(View v) {
        mListener.onClick(v);
    }
}
