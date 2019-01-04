package upt.myplanner.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import upt.myplanner.R;

public class PopupAdapter implements GoogleMap.InfoWindowAdapter {
    private View popup=null;
    private LayoutInflater inflater=null;
    private HashMap<String, Uri> images=null;
    private Context ctxt=null;
    private int iconWidth=-1;
    private int iconHeight=-1;
    private Marker lastMarker=null;

    public PopupAdapter(Context ctxt, LayoutInflater inflater,
                 HashMap<String, Uri> images) {
        this.ctxt=ctxt;
        this.inflater=inflater;
        this.images=images;


        iconWidth=
                ctxt.getResources().getDimensionPixelSize(R.dimen.marker_icon_width);
        iconHeight=
                ctxt.getResources().getDimensionPixelSize(R.dimen.marker_icon_height);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }
    private String getLocationName(Double lat,Double lon) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(ctxt, Locale.getDefault());

        try {
            if(lon != null && lat!=null) {
                addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                return addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName();
            }
            else {
                return "No-location";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "No-location";
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (popup == null) {
            popup=inflater.inflate(R.layout.popup_marker, null);
        }

        if (lastMarker == null
                || !lastMarker.getId().equals(marker.getId())) {
            lastMarker=marker;

            TextView tv=(TextView)popup.findViewById(R.id.marker_title);

            tv.setText(marker.getTitle());


//            Uri image=images.get(marker.getId());
//            ImageView icon=(ImageView)popup.findViewById(R.id.marker_icon);
//
//            if (image == null) {
//                icon.setVisibility(View.GONE);
//            }
//            else {
//                Picasso.with(ctxt).load(image)
//                        .resize(iconWidth,iconHeight)
//                        .placeholder(R.drawable.placeholder_img)
//                        .error(R.drawable.error_img)
//                        .into(icon, new MarkerCallback(marker));
//            }
        }

        return(popup);
    }

    static class MarkerCallback implements Callback {
        Marker marker=null;

        MarkerCallback(Marker marker) {
            this.marker=marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null) {
                if(marker.isInfoWindowShown())
                    marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }
    }
}