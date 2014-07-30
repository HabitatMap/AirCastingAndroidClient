package pl.llp.aircasting.api;

import com.google.gson.Gson;
import com.google.inject.Inject;
import pl.llp.aircasting.api.data.CreateRegressionResponse;
import pl.llp.aircasting.api.data.DeleteRegressionResponse;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.util.http.HttpResult;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

/**
 * Created by marcin on 21/07/14.
 */
public class RegressionDriver {
    private static final String REGRESSIONS_PATH = "/api/regressions.json";
    private static final String REGRESSION_PATH = "/api/regressions";
    private static final String FORMAT = ".json";
    @Inject Gson gson;

    public HttpResult<CreateRegressionResponse> createRegression(Session session, Sensor target, Sensor reference) {
        return http()
                .post()
                .to(REGRESSIONS_PATH)
                .with("session_uuid", session.getUUID().toString())
                .with("target", gson.toJson(target))
                .with("reference", gson.toJson(reference))
                .into(CreateRegressionResponse.class);
    }

    public HttpResult<Regression[]> index() {
        return http()
                .get()
                .to(REGRESSIONS_PATH)
                .into(Regression[].class);
    }

    public HttpResult<DeleteRegressionResponse> delete(Regression regression) {
        return http()
                .delete()
                .to(REGRESSION_PATH + "/" + regression.getBackendId() + FORMAT)
                .into(DeleteRegressionResponse.class);
    }
}
