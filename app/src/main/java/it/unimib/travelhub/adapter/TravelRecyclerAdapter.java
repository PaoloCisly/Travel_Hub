package it.unimib.travelhub.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import it.unimib.travelhub.R;
import it.unimib.travelhub.model.Travels;

public class TravelRecyclerAdapter extends RecyclerView.Adapter<TravelRecyclerAdapter.ViewHolder>{

    /**
     * Interface to associate a click listener with
     * a RecyclerView item.
     */
    public interface OnItemClickListener {
        void onTravelsItemClick(Travels travels);
    }

    private final List<Travels> travelsList;
    private final OnItemClickListener onItemClickListener;

    public TravelRecyclerAdapter(List<Travels> travelsList, OnItemClickListener onItemClickListener) {
        this.travelsList = travelsList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.travel_list_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(travelsList.get(position));
    }

    @Override
    public int getItemCount() {
        if (travelsList != null) {
            return travelsList.size();
        }
        return 0;
    }

    /**
     * Custom DestinationsViewHolder to bind data to the RecyclerView items.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private final TextView textViewTitle;
        private final TextView textViewStartDate;
        private final TextView textViewDestinations;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.travel_title);
            textViewStartDate = itemView.findViewById(R.id.travel_date);
            textViewDestinations = itemView.findViewById(R.id.travel_destinations);
            itemView.setOnClickListener(this);
        }

        @SuppressLint("SimpleDateFormat")
        public void bind(Travels travels) {
            textViewTitle.setText(travels.getTitle());
            textViewStartDate.setText(new SimpleDateFormat("dd/MM/yyyy").format(travels.getStartDate()));
            String travelDestinations = (travels.getDestinations().size() - 1) + " " + itemView.getContext().getString(R.string.travel_segment_number);
            textViewDestinations.setText(travelDestinations);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onTravelsItemClick(travelsList.get(getBindingAdapterPosition()));
        }
    }
}
