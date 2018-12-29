package upt.myplanner.photo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import upt.myplanner.R;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    Context context;
    List<PhotoPost> MainImageUploadInfoList;

    public RecyclerViewAdapter(Context context, List<PhotoPost> TempList) {

        this.MainImageUploadInfoList = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PhotoPost pItem = MainImageUploadInfoList.get(position);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        try {
            if(pItem.longitude != null && pItem.latitude!=null) {
                addresses = geocoder.getFromLocation(Double.valueOf(pItem.latitude), Double.valueOf(pItem.longitude), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                holder.tLocation.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getCountryName());
            }
            else {
                holder.tLocation.setText("No location");
            }
        } catch (IOException e) {
            e.printStackTrace();
            holder.tLocation.setText("No location");
        }

        holder.timestamp.setText(pItem.timestamp);
        holder.tDescription.setText(pItem.description);
        Log.d(PhotoActivity.TAG,pItem.downloadImgPath);
        Picasso.with(context)
                .load(pItem.downloadImgPath) // thumnail url goes here
                .resize(200,200)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.img, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });


    }

    @Override
    public int getItemCount() {

        return MainImageUploadInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView tDescription;
        TextView tLocation;
        TextView timestamp;
        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.postImage);
            tDescription = (TextView) itemView.findViewById(R.id.postDescription);
            timestamp = (TextView)itemView.findViewById(R.id.tTimestamp);
            tLocation = (TextView)itemView.findViewById(R.id.tLocationItem);
        }
    }
}