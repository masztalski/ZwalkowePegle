package fantomit.zwalkowepegle.di.modules;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
@Singleton
public class EventBusModule {
    @Provides
    @Singleton
    EventBus providesEventBus() {
        return EventBus.getDefault();
    }
}
