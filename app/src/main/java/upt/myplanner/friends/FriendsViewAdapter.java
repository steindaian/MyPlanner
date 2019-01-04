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
import java.util.List;
import java.util.Locale;

import upt.myplanner.R;
import upt.myplanner.photo.PhotoActivity;

public class FriendsViewAdapter extends RecyclerView.Adapter<upt.myplanner.friends.FriendsViewAdapter.ViewHolder> {
    Context context;
    List<MyUser> MainImageUploadInfoList;
    String Uid;

    public FriendsViewAdapter(Context context, List<MyUser> TempList,String Uid) {

        this.MainImageUploadInfoList = TempList;

        this.context = context;

        this.Uid = Uid;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final MyUser pItem = MainImageUploadInfoList.get(position);
        boolean sent = false;
        if(pItem.isFriend==false) {
            if(pItem.requests!=null && pItem.requests.contains(Uid)) {
                holder.addFriend.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.sent_img));
                sent = true;
            }
            else
                holder.addFriend.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.addfriendimg));
            holder.addFriend.setVisibility(View.VISIBLE);
        }
        else holder.addFriend.setVisibility(View.GONE);

        final boolean finalSent = sent;
        holder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalSent == true) return;
                if(pItem.requests==null) pItem.requests = new ArrayList<String>();
                pItem.requests.add(Uid);
                FirebaseFirestore.getInstance().collection("users").document(pItem.uid).update("requests",pItem.requests).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Log.d(context.getPackageName(),"Friend Request sent");
                            Toast.makeText(context,"Friend Request sent",Toast.LENGTH_LONG).show();
                            holder.addFriend.setImageBitmap(BitmapFactory.decodeResource(context.getResources(),
                                    R.drawable.sent_img));
                        }
                        else {
                            Log.e(context.getPackageName(),"Error on sending friend request");
                            task.getException().printStackTrace();
                        }
                    }
                });
            }
        });

        holder.tName.setText(pItem.name);
        Picasso.with(context)
                .load(pItem.downloadImgPath) // thumnail url goes here
                .resize(50,50)
                .centerCrop()
                .placeholder(R.drawable.placeholder_img)
                .error(R.drawable.img_person)
                .into(holder.img);
    }

    @Override
    public int getItemCount() {

        return MainImageUploadInfoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img;
        TextView tName;
        ImageButton addFriend;
        public ViewHolder(View itemView) {
            super(itemView);
            img = (ImageView) itemView.findViewById(R.id.friendImg);
            tName = (TextView) itemView.findViewById(R.id.friendName);
            addFriend = (ImageButton) itemView.findViewById(R.id.friendAddImg);

        }
    }
}