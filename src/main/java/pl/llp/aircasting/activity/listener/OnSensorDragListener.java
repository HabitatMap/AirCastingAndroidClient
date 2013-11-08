package pl.llp.aircasting.activity.listener;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.view.SensorsGridView;

public class OnSensorDragListener extends SensorsGridView.OnDragListener {
    private int origHeight;
    private SensorsGridView gridView;
    private View innerView;
    private View containerView;
    private EventBus eventBus;

    public OnSensorDragListener(SensorsGridView gridView, View innerView, View containerView) {
        this.gridView = gridView;
        this.innerView = innerView;
        this.containerView = containerView;
    }

    @Override
    public void onEnter(View view) {
        setContainerWeight(2f);
        setOrigHeight();
        setInnerViewHeight((int) (origHeight * 1.5));
        setGridPaddingTop((int) (origHeight * 1.5) + 4);
    }

    @Override
    public void onLeave(View view) {
        setContainerWeight(1f);
        setInnerViewHeight(origHeight);
        if (!gridView.isInListenArea())
            setGridPaddingTop(origHeight);
    }

    private void setGridPaddingTop(int padding) {
        gridView.setPadding(gridView.getPaddingLeft(), padding, gridView.getPaddingRight(), gridView.getPaddingBottom());
    }

    private void setOrigHeight() {
        origHeight = innerView.getLayoutParams().height;
    }

    private void setInnerViewHeight(int height) {
        ViewGroup.LayoutParams areaParams = innerView.getLayoutParams();
        areaParams.height = height;
        innerView.setLayoutParams(areaParams);
    }

    private void setContainerWeight(float weight) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) containerView.getLayoutParams();
        params.weight = weight;
        containerView.setLayoutParams(params);
    }
}