package pl.llp.aircasting.screens.dashboard.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import pl.llp.aircasting.R;

public class StreamItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final StreamItemTouchHelperAdapter mAdapter;
    private final Context mContext;
    private final Drawable mDeleteIcon;
    private final Drawable mHideIcon;
    private final ColorDrawable mDeleteBackground;
    private final ColorDrawable mHideBackground;

    public StreamItemTouchHelperCallback(StreamItemTouchHelperAdapter adapter, Context context) {
        mAdapter = adapter;
        mContext = context;
        mDeleteIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_stream_delete);
        mHideIcon = ContextCompat.getDrawable(mContext, R.drawable.clear_dashboard_icon);

        mDeleteBackground = new ColorDrawable(mContext.getResources().getColor(R.color.dashboard_delete_stram_bckgd));
        mHideBackground = new ColorDrawable(mContext.getResources().getColor(R.color.list_viewing_session_even));
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

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            float dX,
                            float dY,
                            int actionState,
                            boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        ColorDrawable background = mHideBackground;
        Drawable icon = mHideIcon;

        background.setBounds(0, 0, 0, 0);
        background.draw(c);

        int iconMarginVertical = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconMarginHorizontal = (itemView.getHeight() - icon.getIntrinsicWidth()) / 4;
        int iconTop = itemView.getTop() + iconMarginVertical;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        //right
        if (dX > 0) {
            int iconLeft = itemView.getLeft() + iconMarginHorizontal;
            int iconRight = itemView.getLeft() + iconMarginHorizontal + icon.getIntrinsicWidth();

            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getLeft(), itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
        } else if (dX < 0) {
            icon = mDeleteIcon;
            background = mDeleteBackground;

            int iconLeft = itemView.getRight() - iconMarginHorizontal - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMarginHorizontal;

            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds((int) (itemView.getRight() + dX), itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else {
            icon.setBounds(0, 0, 0, 0);
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }
}
