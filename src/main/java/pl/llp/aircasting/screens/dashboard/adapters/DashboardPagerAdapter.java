package pl.llp.aircasting.screens.dashboard.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperAdapter;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperCallback;

public class DashboardPagerAdapter extends PagerAdapter {
    public static final int BLUETOOTH_TAB = 0;
    public static final int OTHER_TAB = 1;

    private Context mContext;
    private ArrayList<StreamRecyclerAdapter> mAdapters = new ArrayList();

    public DashboardPagerAdapter(Context context,
                                 CurrentStreamsRecyclerAdapter currentStreamsRecyclerAdapter,
                                 ViewingStreamsRecyclerAdapter viewingStreamsRecyclerAdapter) {
        mContext = context;
        mAdapters.add(currentStreamsRecyclerAdapter);
        mAdapters.add(viewingStreamsRecyclerAdapter);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        DashboardPages dashboardPage = DashboardPages.values()[position];
        ViewGroup layout = (ViewGroup) inflater.inflate(dashboardPage.getLayout(), container, false);

        RecyclerView recyclerView = layout.findViewById(dashboardPage.getRecyclerView());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.Adapter adapter = (RecyclerView.Adapter) mAdapters.get(position);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new StreamItemTouchHelperCallback((StreamItemTouchHelperAdapter) adapter, mContext);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        container.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int layout, Object view) {
        container.removeView((View) view);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(DashboardPages.values()[position].mTitle);
    }

    private enum DashboardPages {
        CURRENT(R.id.current_streams_recycler_view, R.layout.current_streams_page, R.string.bluetooth),
        VIEWING(R.id.viewing_streams_recycler_view, R.layout.viewing_streams_page, R.string.other);

        private int mRecyclerView;
        private int mLayout;
        private int mTitle;

        DashboardPages(int recyclerView, int layout, int title) {
            mRecyclerView = recyclerView;
            mLayout = layout;
            mTitle = title;
        }

        public int getRecyclerView() {
            return mRecyclerView;
        }

        public int getLayout() {
            return mLayout;
        }

        public int getTitle() {
            return mTitle;
        }
    }
}
