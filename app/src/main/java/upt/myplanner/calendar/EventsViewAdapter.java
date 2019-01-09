package upt.myplanner.calendar;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import upt.myplanner.R;
import upt.myplanner.calendar.MyEvent;

class EventsViewAdapter extends RecyclerView.Adapter<EventsViewAdapter.ViewHolder> {
    Context context;
    List<MyEvent> events;

    public EventsViewAdapter(Context context, List<MyEvent> events) {
        this.context = context;
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_event, viewGroup, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final MyEvent event = events.get(position);

        viewHolder.name.setText(event.getName());
        viewHolder.descr.setText(event.getDescription());
        viewHolder.date.setText(event.getDay()+"/"+event.getMonth()+"/"+event.getYear());
        viewHolder.end.setText(event.getEnd_time());
        viewHolder.start.setText(event.getStart_time());
        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context,AddEventActivity.class);


                intent.putExtra("year",String.valueOf(event.getYear()));
                intent.putExtra("month",String.valueOf(event.getMonth()));
                intent.putExtra("day",String.valueOf(event.getDay()));
                intent.putExtra("name",String.valueOf(event.getName()));
                intent.putExtra("description",String.valueOf(event.getDescription()));
                intent.putExtra("start",String.valueOf(event.getStart_time()));
                intent.putExtra("end",String.valueOf(event.getEnd_time()));
                intent.putExtra("timestamp",String.valueOf(event.timestamp));
                context.startActivity(intent);
            }
        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("events").document(event.timestamp).delete();
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;
        TextView descr;
        TextView start;
        TextView end;
        TextView date;
        ImageButton delete;
        ImageButton edit;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.eventName);
            descr = itemView.findViewById(R.id.eventDescr);
            start = itemView.findViewById(R.id.eventStart);
            end = itemView.findViewById(R.id.eventEnd);
            date = itemView.findViewById(R.id.eventDate);
            delete = itemView.findViewById(R.id.bDelete);
            edit = itemView.findViewById(R.id.bEdit);
        }
    }
}
