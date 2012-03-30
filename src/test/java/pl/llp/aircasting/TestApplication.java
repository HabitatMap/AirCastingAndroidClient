package pl.llp.aircasting;

import com.google.inject.Module;
import pl.llp.aircasting.guice.AirCastingApplication;

import java.util.List;

public class TestApplication extends AirCastingApplication {
    @Override
    protected void addApplicationModules(List<Module> modules) {
        super.addApplicationModules(modules);
        modules.add(new TestModule());
    }
}
