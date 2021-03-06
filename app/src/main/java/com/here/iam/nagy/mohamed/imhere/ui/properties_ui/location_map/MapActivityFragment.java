package com.here.iam.nagy.mohamed.imhere.ui.properties_ui.location_map;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.here.iam.nagy.mohamed.imhere.R;
import com.here.iam.nagy.mohamed.imhere.firebase_data.UserDataFirebaseMap;
import com.here.iam.nagy.mohamed.imhere.helper_classes.Constants;
import com.here.iam.nagy.mohamed.imhere.helper_classes.Utility;
import com.here.iam.nagy.mohamed.imhere.location_service.UserLocation;
import com.here.iam.nagy.mohamed.imhere.location_service.UserLocationCallback;
import com.here.iam.nagy.mohamed.imhere.ui.ViewAppHolder;
import com.here.iam.nagy.mohamed.imhere.user_account.account_property.objects.FlagsMarkers;
import com.here.iam.nagy.mohamed.imhere.user_account.account_property.objects.UserAccount;

/**
 * A placeholder fragment containing a simple view.
 */
public class MapActivityFragment extends Fragment
    implements OnMapReadyCallback, GoogleMap.OnMapClickListener, UserLocationCallback {
    // Zoom 0 .. 21
    private UserDataFirebaseMap userDataFirebaseMap;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserLocation userLocation = new UserLocation(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map_, container, false);

        /// get user data
        final String USER_LINK_FIREBASE = getActivity().getIntent().getExtras().getString(Constants.USER_EXTRA);

        // set firebase
        userDataFirebaseMap = new UserDataFirebaseMap(USER_LINK_FIREBASE, getContext());

        MapFragment mapFragment = (MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.map_frame);


        mapFragment.getMapAsync(this);

        googleApiClient.connect();

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("user is ", userDataFirebaseMap.getUserLinkFirebase());

        userDataFirebaseMap.setupUserMap(googleMap);

        setMarkerWindowInfo(googleMap);
    }

    private void setMarkerWindowInfo(GoogleMap googleMap){


        // deleting flag.

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(Utility.networkIsConnected(getContext())) {
                    if (marker.getTitle().equals(Constants.USER_FLAGS))
                        userDataFirebaseMap.deleteUserFlag();
                }else{
                    Toast.makeText(getContext(),
                            "Check your network connection",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                View infoView = null;

                if(marker.getTitle().equals(Constants.PUBLIC_FLAGS) ||
                        marker.getTitle().equals(Constants.FRIENDS_FLAGS) ||
                        marker.getTitle().equals(Constants.USER_FLAGS)) {

                    infoView =
                            getActivity().getLayoutInflater().inflate(R.layout.window_flag, null);

                    ViewAppHolder.FlagWindowViewHolder infoWindowHolder =
                            new ViewAppHolder.FlagWindowViewHolder(infoView);

                    FlagsMarkers flagsMarkers = (FlagsMarkers) marker.getTag();

                    infoWindowHolder.FLAG_WINDOW_TITLE.setText(flagsMarkers.getTitle());
                    infoWindowHolder.FLAG_WINDOW_CREATED_NAME.setText(flagsMarkers.getUserCreatedName());
                    infoWindowHolder.FLAG_WINDOW_LIKES_NUMBER.setText(String.valueOf(flagsMarkers.getLikesNumber()));

                    // flag type.
                    switch (marker.getTitle()){

                        case Constants.PUBLIC_FLAGS:

                            infoWindowHolder.FLAG_WINDOW_TYPE.setText("Public Flag");
                            infoWindowHolder.FLAG_WINDOW_DELETE_TEXT_VIEW.setVisibility(View.GONE);
                            break;

                        case Constants.FRIENDS_FLAGS:

                            infoWindowHolder.FLAG_WINDOW_TYPE.setText("Friend Flag");
                            infoWindowHolder.FLAG_WINDOW_DELETE_TEXT_VIEW.setVisibility(View.GONE);
                            break;

                        case Constants.USER_FLAGS:

                            infoWindowHolder.FLAG_WINDOW_TYPE.setText("Your Flag");
                            infoWindowHolder.FLAG_WINDOW_DELETE_TEXT_VIEW.setVisibility(View.VISIBLE);
                            break;

                    }
                }else if(marker.getTitle().equals(Constants.USERS)){

                    infoView =
                            getActivity().getLayoutInflater().inflate(R.layout.window_users,null);

                    UserAccount userAccount = (UserAccount) marker.getTag();

                    ViewAppHolder.UsersWindowViewHolder usersWindowViewHolder =
                            new ViewAppHolder.UsersWindowViewHolder(infoView);

                    if(userAccount.getUserImage() == null){
                        usersWindowViewHolder.USER_IMAGE_WINDOW_IMAGE_VIEW
                                .setImageDrawable(getActivity().getDrawable(R.drawable.me));
                    }else{
                        Glide.with(getActivity()).load(userAccount.getUserImage())
                                .into(usersWindowViewHolder.USER_IMAGE_WINDOW_IMAGE_VIEW);
                    }

                    usersWindowViewHolder.USER_NAME_WINDOW_TEXT_VIEW.setText(userAccount.getUserName());

                    switch (marker.getTitle()){

                        case Constants.USERS:
                            if(Utility.encodeUserEmail(userAccount.getUserEmail())
                                    .equals(userDataFirebaseMap.getUserLinkFirebase())){
                                usersWindowViewHolder.USER_STATE_WINDOW_TEXT_VIEW.setText("ME");
                            }else{
                                usersWindowViewHolder.USER_STATE_WINDOW_TEXT_VIEW.setText("FRIEND");
                            }
                            break;

                        case Constants.EVENT_MEMBER:
                            String text ="EVENT MEMBER : " + marker.getSnippet();

                            usersWindowViewHolder.USER_STATE_WINDOW_TEXT_VIEW
                                    .setText(text);

                            break;

                        case Constants.HELP:
                            usersWindowViewHolder.USER_STATE_WINDOW_TEXT_VIEW
                                    .setText("NEED HELP");
                    }

                }

                return infoView;
            }
        });
    }

    @Override
    public void onStop() {
        userDataFirebaseMap.detachMapListeners();
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(Utility.networkIsConnected(getContext())) {
            userDataFirebaseMap.attachMapListeners();
            googleApiClient.connect();
        }else{
            Toast.makeText(getContext(),"Check your network connection",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMapClick(LatLng latLng) {
        Log.e("map is click", "done");

    }

    @Override
    public void createGoogleClient(GoogleApiClient.ConnectionCallbacks connectionCallbacks, GoogleApiClient.OnConnectionFailedListener connectionFailedListener) {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addOnConnectionFailedListener(connectionFailedListener)
                .addConnectionCallbacks(connectionCallbacks)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(LocationListener locationListener) {
        LocationRequest locationRequest = new LocationRequest()
                .setInterval(Constants.ACTIVITY_MODE)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
        }catch(SecurityException e){

        }
    }

    @Override
    public void onConnectionSuspended() {

    }

    @Override
    public void onConnectionFailed() {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(Utility.networkIsConnected(getContext())) {
            userDataFirebaseMap.updateUserLocation(location);
        }else{
            Toast.makeText(getContext(),"Your location can't update, please check your network connection",Toast.LENGTH_SHORT).show();
        }
    }

}


