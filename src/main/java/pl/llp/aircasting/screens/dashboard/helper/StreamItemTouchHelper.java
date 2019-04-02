package pl.llp.aircasting.screens.dashboard.helper;

import android.support.v7.widget.RecyclerView;

public interface StreamItemTouchHelper {
    void onItemMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPosition, int toPosition);

    void finishDrag(RecyclerView.ViewHolder viewHolder);

    void onItemSwipe(RecyclerView.ViewHolder viewHolder);
}
