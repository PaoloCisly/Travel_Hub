package it.unimib.travelhub.adapter;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import it.unimib.travelhub.R;
import it.unimib.travelhub.model.TravelSegment;
import it.unimib.travelhub.model.Travels;
import it.unimib.travelhub.ui.travels.TravelActivity;

public class TravelSegmentRecyclerAdapter extends RecyclerView.Adapter<TravelSegmentRecyclerAdapter.ViewHolder> {


    public interface OnMoreButtonClickListener {
        void onButtonMoreItemClick(TravelSegment travelSegment, TextView seg_more_button);
    }
    private static OnMoreButtonClickListener onMoreButtonClickListener = null;

    List<TravelSegment> data;
    Travels travel;
    private static final int VIEW_TYPE_TOP = 0;
    private static final int VIEW_TYPE_MIDDLE = 1;
    private static final int VIEW_TYPE_BOTTOM = 2;


    public TravelSegmentRecyclerAdapter(OnMoreButtonClickListener onItemClickListener, List<TravelSegment> data, Travels travel) {
        onMoreButtonClickListener = onItemClickListener;
        this.data = data;
        this.travel = travel;
    }
    @NonNull
    @Override
    public TravelSegmentRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.travel_segment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TravelSegmentRecyclerAdapter.ViewHolder holder, final int position) {
        TravelSegment item = data.get(position);

        CardView segment_card = holder.itemView.findViewById(R.id.item_segment_card);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) segment_card.getLayoutParams();
        if (((TravelActivity) holder.itemView.getContext()).isTravelCreator) {
            holder.itemView.findViewById(R.id.item_segment_more).setVisibility(View.VISIBLE);
        }

        switch(holder.getItemViewType()) {
            case VIEW_TYPE_TOP:
                params.setMargins(0, 0, 0, 0);
                break;
            case VIEW_TYPE_MIDDLE:
            case VIEW_TYPE_BOTTOM:
                params.setMargins(0, 12, 0, 0);
                break;
        }
        segment_card.setLayoutParams(params);
        holder.bind(item, travel);
    }

    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_TYPE_TOP;}
        else if(position == data.size() - 1) {
            return VIEW_TYPE_BOTTOM;
        }
        return VIEW_TYPE_MIDDLE;
    }

    @Override
    public int getItemCount() {
        if (data != null) {
            return data.size();
        }
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView segment_card;
        TextInputEditText seg_location;
        TextInputEditText seg_date_from;
        TextInputEditText seg_date_to;
        TextInputEditText seg_description;
        TextView seg_expand;
        View seg_icon;
        TextView seg_more_button;
        final Calendar myCalendar= Calendar.getInstance();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            segment_card = itemView.findViewById(R.id.item_segment_card);
            seg_location = itemView.findViewById(R.id.item_segment_location);
            seg_date_from = itemView.findViewById(R.id.item_segment_start_date);
            seg_date_to = itemView.findViewById(R.id.item_segment_end_date);
            seg_description = itemView.findViewById(R.id.item_segment_description);
            seg_icon = itemView.findViewById(R.id.item_segment_icon);
            seg_expand = itemView.findViewById(R.id.item_segment_expand);
            seg_more_button = itemView.findViewById(R.id.item_segment_more);
        }

        public void bind(TravelSegment travelSegment, Travels travel) {

            seg_location.setText(travelSegment.getLocation());

            if (travelSegment.getDateFrom() != null && travelSegment.getDateTo() != null) {
                @SuppressLint("SimpleDateFormat") String dateFrom = new SimpleDateFormat("dd/MM/yyyy").format(travelSegment.getDateFrom());
                @SuppressLint("SimpleDateFormat") String dateTo = new SimpleDateFormat("dd/MM/yyyy").format(travelSegment.getDateTo());
                seg_date_from.setText(dateFrom);
                seg_date_to.setText(dateTo);
                long currentTime = System.currentTimeMillis();
                if (currentTime >= travelSegment.getDateFrom().getTime() && currentTime <= travelSegment.getDateTo().getTime()) {
                    seg_icon.setBackgroundResource(R.drawable.baseline_location_on_24);
                    //seg_icon.getBackground().setTint(itemView.getResources().getColor(R.color.orange));
                } else if (currentTime > travelSegment.getDateTo().getTime()) {
                    seg_icon.setBackgroundResource(R.drawable.baseline_location_on_24);
                    //seg_icon.getBackground().setTint(itemView.getResources().getColor(R.color.grey_separator));
                } else {
                    seg_icon.setBackgroundResource(R.drawable.baseline_location_on_24);
                    //seg_icon.getBackground().setTint(itemView.getResources().getColor(R.color.primaryVariantColor));
                }
            }else{
                seg_date_from.setText(R.string.travel_segment_no_date);
                seg_date_to.setText(R.string.travel_segment_no_date);
            }

            if (travelSegment.getDescription() == null || travelSegment.getDescription().isEmpty()) {
                seg_description.setText(R.string.travel_no_description);
            } else {
                seg_description.setText(travelSegment.getDescription());
            }
            ConstraintLayout seg_details = itemView.findViewById(R.id.item_segment_details);
            segment_card.setOnClickListener(v -> {
                Log.d("TravelSegmentRecyclerAdapter", "onClick: " + seg_description.getVisibility());
                if (seg_details.getVisibility() == View.GONE) {
                    seg_details.setVisibility(View.VISIBLE);
                    seg_expand.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_keyboard_arrow_up_24, 0);
                } else {
                    seg_details.setVisibility(View.GONE);
                    seg_expand.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_keyboard_arrow_down_24, 0);
                }
            });
            seg_more_button.setOnClickListener(v -> onMoreButtonClickListener.onButtonMoreItemClick(travelSegment, seg_more_button));
            edit_travel_segment(travelSegment, travel);


        }

        private void edit_travel_segment(TravelSegment travelSegment, Travels travel) {
            TravelActivity travelActivity = (TravelActivity) itemView.getContext();
            seg_location.setOnLongClickListener(v -> {
                if (travelActivity.enableEdit) {
                    seg_location.setFocusableInTouchMode(true);
                    seg_location.setFocusable(true);
                    seg_location.requestFocus();
                    seg_location.setOnFocusChangeListener((v1, hasFocus) -> {
                        if (!hasFocus) {
                            seg_location.setFocusable(false);
                        }
                    });
                }
                return true;
            });
            seg_location.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    travelSegment.setLocation(s.toString());
                    travelActivity.showEditButton();
                }
                @Override
                public void afterTextChanged(Editable s) {
                    seg_location.setFocusable(false);
                }
            });

            seg_description.setOnLongClickListener(v -> {
                if (travelActivity.enableEdit){
                    seg_description.setFocusableInTouchMode(true);
                    seg_description.setFocusable(true);
                    seg_description.requestFocus();
                    seg_description.setOnFocusChangeListener((v1, hasFocus) -> {
                        if (!hasFocus) {
                            seg_description.setFocusable(false);
                        }
                    });
                }

                return true;
            });
            seg_description.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    travelSegment.setDescription(s.toString());
                    travelActivity.showEditButton();
                }
                @Override
                public void afterTextChanged(Editable s) {
                    seg_description.setFocusable(false);
                }
            });

            DatePickerDialog datePickerDialogFromDate = new DatePickerDialog(itemView.getContext());
            datePickerDialogFromDate.getDatePicker().setMinDate(travel.getStartDate().getTime());
            datePickerDialogFromDate.getDatePicker().setMaxDate(travel.getEndDate().getTime());

            datePickerDialogFromDate.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                String date = updateLabel(seg_date_from);
                travelSegment.setDateFrom(parseStringToDate(date + " 00:00:00"));
            });

            DatePickerDialog datePickerDialogToDate = new DatePickerDialog(itemView.getContext());
            datePickerDialogToDate.getDatePicker().setMinDate(travel.getStartDate().getTime());
            datePickerDialogToDate.getDatePicker().setMaxDate(travel.getEndDate().getTime());

            datePickerDialogToDate.setOnDateSetListener((view, year, month, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR,year);
                myCalendar.set(Calendar.MONTH,month);
                myCalendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                String date = updateLabel(seg_date_to);
                travelSegment.setDateTo(parseStringToDate(date + " 23:59:59"));
            });

            seg_date_from.setOnLongClickListener(v ->
            {
                if(travelActivity.enableEdit){
                    datePickerDialogFromDate.show();
                    //new DatePickerDialog(itemView.getContext(), date1 ,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    travelActivity.showEditButton();
                }
                return true;
            });

            seg_date_to.setOnLongClickListener(v ->
            {
                if(travelActivity.enableEdit){
                    datePickerDialogToDate.show();
                    //new DatePickerDialog(itemView.getContext(), date2,myCalendar.get(Calendar.YEAR),myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    travelActivity.showEditButton();
                }
                return true;
            });
        }

        private Date parseStringToDate(String date){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", itemView.getResources().getConfiguration().getLocales().get(0));
            Date parsedDate = null;
            try {
                parsedDate = sdf.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return parsedDate;
        }
        private String updateLabel(EditText editText) {
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat, itemView.getResources().getConfiguration().getLocales().get(0));
            String s = sdf.format(myCalendar.getTime());
            editText.setText(s);
            return s;
        }
    }


}
