package pl.llp.aircasting.screens.dashboard.helper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import pl.llp.aircasting.R;

public class StreamItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final StreamItemTouchHelperAdapter mAdapter;

    public StreamItemTouchHelperCallback(StreamItemTouchHelperAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return mAdapter.isLongPressDragEnabled();
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return mAdapter.isItemSwipeEnabled();
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        long fromSensorSessionId = (long) viewHolder.itemView.getTag(R.id.session_id_tag);
        long toSensorSessionId = (long) target.itemView.getTag(R.id.session_id_tag);

        if (fromSensorSessionId == toSensorSessionId) {
            mAdapter.onItemMove(viewHolder, target, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemSwipe(viewHolder.getAdapterPosition(), direction);
    }
}
