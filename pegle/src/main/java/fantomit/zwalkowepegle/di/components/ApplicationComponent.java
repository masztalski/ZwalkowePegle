package fantomit.zwalkowepegle.di.components;

import javax.inject.Singleton;

import dagger.Component;
import fantomit.zwalkowepegle.MainActivity;
import fantomit.zwalkowepegle.RiverStations;
import fantomit.zwalkowepegle.Settings;
import fantomit.zwalkowepegle.StationDetails;
import fantomit.zwalkowepegle.di.modules.ApplicationModule;
import fantomit.zwalkowepegle.di.modules.EventBusModule;
import fantomit.zwalkowepegle.di.modules.NetworkModule;
import fantomit.zwalkowepegle.di.modules.RepositoryModule;
import fantomit.zwalkowepegle.di.modules.WebServiceModule;
import fantomit.zwalkowepegle.dialogs.ChooseWojewodztwoDialog;
import fantomit.zwalkowepegle.dialogs.ConfirmDeleteDialog;
import fantomit.zwalkowepegle.dialogs.EditCustomStationDialog;
import fantomit.zwalkowepegle.dialogs.NoteDialog;
import fantomit.zwalkowepegle.gcm.MyGcmListenerService;
import fantomit.zwalkowepegle.gcm.RegistrationIntentService;
import fantomit.zwalkowepegle.services.FavsDownloadService;

@Singleton
@Component(modules = {ApplicationModule.class, EventBusModule.class, NetworkModule.class, RepositoryModule.class, WebServiceModule.class})
public interface ApplicationComponent {
    void inject(MainActivity activity);

    void inject(RiverStations activity);

    void inject(Settings activity);

    void inject(StationDetails activity);

    void inject(NoteDialog dialog);

    void inject(EditCustomStationDialog dialog);

    void inject(ConfirmDeleteDialog dialog);

    void inject(ChooseWojewodztwoDialog dialog);

    void inject(FavsDownloadService service);

    void inject(RegistrationIntentService service);

    void inject(MyGcmListenerService service);
}
