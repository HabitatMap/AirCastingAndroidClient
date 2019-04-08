package pl.llp.aircasting.screens.dashboard.views;

import android.content.Context;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;
import java.util.Map;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.adapters.CurrentStreamsRecyclerAdapter;
import pl.llp.aircasting.screens.dashboard.adapters.DashboardPagerAdapter;
import pl.llp.aircasting.screens.dashboard.adapters.ViewingStreamsRecyclerAdapter;

import static pl.llp.aircasting.screens.dashboard.adapters.DashboardPagerAdapter.BLUETOOTH_TAB;
import static pl.llp.aircasting.screens.dashboard.adapters.DashboardPagerAdapter.OTHER_TAB;

public class DashboardViewMvcImpl implements DashboardViewMvc, View.OnClickListener, DashboardViewMvc.Listener {
    private final View mRootView;
    private final LayoutInflater mInflater;
    private final View mEmptyLayout;
    private final ViewPager mViewPager;
    private final View mStreamsLayout;
    private final TabLayout mTabsLayout;
    private final View mMicrophoneButton;
    private final View mSensorsButton;
    private final View mAirbeam2ConfigButton;
    private final DashboardPagerAdapter mPagerAdapter;
    private final CurrentStreamsRecyclerAdapter mCurrentRecyclerAdapter;
    private final ViewingStreamsRecyclerAdapter mViewingRecyclerAdapter;
    private final AppCompatActivity mContext;

    private Listener mListener;

    public DashboardViewMvcImpl(AppCompatActivity context, ViewGroup parent, ResourceHelper resourceHelper) {
        mContext = context;
        mInflater = mContext.getLayoutInflater();
        mRootView = mInflater.inflate(R.layout.dashboard, parent, false);
        mViewPager = findViewById(R.id.dashboard_view_pager);
        mStreamsLayout = findViewById(R.id.streams_layout);
        mTabsLayout = findViewById(R.id.dashboard_tabs_layout);
        mTabsLayout.setVisibility(View.GONE);
        mEmptyLayout = findViewById(R.id.layout_empty);

        mMicrophoneButton = findViewById(R.id.dashboard_microphone);
        mSensorsButton = findViewById(R.id.dashboard_sensors);
        mAirbeam2ConfigButton = findViewById(R.id.configure_airbeam2);

        if (mMicrophoneButton != null) { mMicrophoneButton.setOnClickListener(this); }
        if (mSensorsButton != null) { mSensorsButton.setOnClickListener(this); }
        if (mAirbeam2ConfigButton != null) { mAirbeam2ConfigButton.setOnClickListener(this); }

        Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mCurrentRecyclerAdapter = new CurrentStreamsRecyclerAdapter(mInflater, this, resourceHelper, vibrator);
        mViewingRecyclerAdapter = new ViewingStreamsRecyclerAdapter(mInflater, this, resourceHelper, vibrator);
        mPagerAdapter = new DashboardPagerAdapter(mContext, mCurrentRecyclerAdapter, mViewingRecyclerAdapter);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.dashboard_tabs_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    @Override
    public void registerListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void unregisterListener() {
        mListener = null;
    }

    @Override
    public void bindSensorData(List data) {
        if (!data.isEmpty()) {
            mEmptyLayout.setVisibility(View.GONE);
            mStreamsLayout.setVisibility(View.VISIBLE);
        } else if (mViewingRecyclerAdapter.getItemCount() == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mStreamsLayout.setVisibility(View.GONE);
        }

        mCurrentRecyclerAdapter.bindData(data);

        if (!data.isEmpty() && mViewingRecyclerAdapter.getItemCount() == 0) {
            selectActiveTab(BLUETOOTH_TAB);
        } else if (data.isEmpty() && mViewingRecyclerAdapter.getItemCount() != 0) {
            selectActiveTab(OTHER_TAB);
        }

        showTabs();
    }

    private void showTabs() {
        if (adaptersPopulated()) {
            mTabsLayout.setVisibility(View.VISIBLE);
        } else {
            mTabsLayout.setVisibility(View.GONE);
        }
    }

    private boolean adaptersPopulated() {
        return mCurrentRecyclerAdapter.getItemCount() != 0 && mViewingRecyclerAdapter.getItemCount() != 0;
    }

    private void selectActiveTab(int tabToSelect) {
        TabLayout.Tab tab = mTabsLayout.getTabAt(tabToSelect);
        tab.select();
    }

    @Override
    public void bindRecentMeasurements(Map<String, Double> recentMeasurementsData) {
        mCurrentRecyclerAdapter.bindNowValues(recentMeasurementsData);
    }

    @Override
    public void bindChartData(Map liveCharts) {
        mCurrentRecyclerAdapter.bindChartData(liveCharts);
    }

    @Override
    public void bindViewingSensorsData(List data) {
        if (!data.isEmpty()) {
            mEmptyLayout.setVisibility(View.GONE);
            mStreamsLayout.setVisibility(View.VISIBLE);
        } else if (mCurrentRecyclerAdapter.getItemCount() == 0) {
            mEmptyLayout.setVisibility(View.VISIBLE);
            mStreamsLayout.setVisibility(View.GONE);
        }

        mViewingRecyclerAdapter.bindData(data);

        if (!data.isEmpty() && mCurrentRecyclerAdapter.getItemCount() == 0) {
            selectActiveTab(OTHER_TAB);
        } else if (data.isEmpty() && mCurrentRecyclerAdapter.getItemCount() != 0) {
            selectActiveTab(BLUETOOTH_TAB);
        }

        showTabs();
    }

    @Override
    public void bindStaticChartData(Map staticCharts) {
        mViewingRecyclerAdapter.bindChartData(staticCharts);
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    @Override
    public void onClick(View v) {
        onDashboardButtonClicked(v);
    }

    @Override
    public void onDashboardButtonClicked(View view) {
        mListener.onDashboardButtonClicked(view);
    }

    @Override
    public void onStreamClicked(View view) {
        mListener.onStreamClicked(view);
    }

    @Override
    public void onItemSwipe(int position, Map dataItem, boolean noStreamsLeft, int direction, int itemType) {
        mListener.onItemSwipe(position, dataItem, noStreamsLeft, direction, itemType);
    }

    public void itemRemoved(int position) {
        mViewingRecyclerAdapter.removeItem(position);
    }

    public void cancelSwipe(int position) {
        mViewingRecyclerAdapter.notifyItemChanged(position);
    }
}
