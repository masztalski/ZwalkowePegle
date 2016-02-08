package fantomit.zwalkowepegle.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fantomit.zwalkowepegle.R;
import fantomit.zwalkowepegle.Statics;

public class MapDialog extends AppCompatDialogFragment implements OnMapReadyCallback {

    private static final float ZOOM = 12F;
    private float longitude;
    private float langitude;
    private String name;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.map_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            longitude = getArguments().getFloat(Statics._LONGITUDE);
            langitude = getArguments().getFloat(Statics._LANGITUDE);
            name = getArguments().getString(Statics._STATION_NAME);
        }
        SupportMapFragment mapFragment = new SupportMapFragment();
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.map, mapFragment).commit();
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (langitude != 0.0 && longitude != 0.0) {
            LatLng position = new LatLng(langitude, longitude);
            Marker marker = googleMap.addMarker(new MarkerOptions().position(position).title(name));
            marker.showInfoWindow();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, ZOOM));
        } else {
            Toast.makeText(getActivity(), "Brak danych o lokalizacji", Toast.LENGTH_SHORT).show();
        }
    }

}
