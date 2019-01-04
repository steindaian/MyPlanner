package upt.myplanner.friends;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import upt.myplanner.R;
import upt.myplanner.photo.PhotoActivity;

public class RequestsViewAdapter extends RecyclerView.Adapter<upt.myplanner.friends.RequestsViewAdapter.ViewHolder> {
    private final FirebaseFirestore db;
    Context context;
    List<MyUser> MainImageUploadInfoList;
    String Uid;
    List<String> userReqIds;
    List<String> userFriendIds;

    public RequestsViewAdapter(Context context, List<MyUser> TempList,String Uid,FirebaseFirestore db) {

        this.MainImageUploadInfoList = TempList;
        this.db = db;
        this.context = context;

        this.Uid = Uid;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final MyUser pItem = MainImageUploadInfoList.get(position);

        holder.tName.setText(pItem.name);
        Picasso.with(context)
                .load(pItem.downloadImgPath) // thumnail url goes here
                .resize(50,50)
                .centerCrop()
                .placeholder(R.drawable.img_person)
                .error(R.drawable.img_person)
                .into(holder.img);

        holder.approveReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //update req user
                try {
                    if (pItem.friends == null) pItem.friends = new ArrayList<String>();
                    pItem.friends.add(Uid);
                    db.collection("users").document(pItem.uid).update("friends", pItem.friends);//add listener
                    //update me
                    userReqIds.remove(pItem.uid);
                    if (userFriendIds == null) userFriendIds = new ArrayList<String>();
                    userFriendIds.add(pItem.uid);
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put("friends", userFriendIds);
                    map.put("requests", userReqIds);
                    db.collection("users").document(Uid).update(map);
                }
                catch (Exception e) {
                    Log.e("Exception","HERE");
                    e.printStackTrace();
                }

            }
        });

        holder.cancelReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userReqIds.remove(pItem.uid);
                db.collection("users").document(Uid).update("requests",userReqIds);
            }
        });
    }

    @Override
    public int getItemCount() {

        return MainImageUploadInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView tName;
        ImageButton approveReq;
        ImageButton cancelReq;
        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.reqImg);
            tName = (TextView) itemView.findViewById(R.id.reqName);
            approveReq = (ImageButton) itemView.findViewById(R.id.reqAddImg);
            cancelReq = (ImageButton) itemView.findViewById(R.id.reqCancelImg);

        }
    }
}