package fantomit.zwalkowepegle.di;

import com.google.inject.AbstractModule;

import de.greenrobot.event.EventBus;

public class EventBusModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EventBus.class).toInstance(EventBus.getDefault());
    }
}
