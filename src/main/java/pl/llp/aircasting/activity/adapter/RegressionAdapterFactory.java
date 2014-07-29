package pl.llp.aircasting.activity.adapter;

import android.content.Context;
import com.google.inject.Inject;
import pl.llp.aircasting.storage.repository.RegressionRepository;

/**
 * Created by marcin on 28/07/14.
 */
public class RegressionAdapterFactory {
    @Inject RegressionRepository regressionRepository;

    public RegressionAdapter create(Context context) {
        return new RegressionAdapter(context, regressionRepository.fetchAll(), regressionRepository);
    }
}
