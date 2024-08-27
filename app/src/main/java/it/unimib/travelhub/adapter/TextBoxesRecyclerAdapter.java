package it.unimib.travelhub.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import it.unimib.travelhub.R;

public class TextBoxesRecyclerAdapter extends RecyclerView.Adapter<TextBoxesRecyclerAdapter.DestinationsViewHolder> {

    List<String> textBoxesHints;
    List<String> textBoxesTexts;
    String hint;
    OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onKeyPressed(int position, String text);
    }

    private static final String TAG = TextBoxesRecyclerAdapter.class.getSimpleName();

    public List<String> getDestinationsTexts() {
        return textBoxesTexts;
    }

    public List<String> getTextBoxesHints() {
        return textBoxesHints;
    }


    public TextBoxesRecyclerAdapter(List<String> textBoxesHints, List<String> destinationsTexts , OnItemClickListener onItemClickListener) {
        this.textBoxesHints = textBoxesHints;
        this.textBoxesTexts = destinationsTexts;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public DestinationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.text_input_item, parent, false);
        return new DestinationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationsViewHolder holder, int position) {
        TextInputLayout textInputLayout = holder.getTextInputLayout();
        TextInputEditText textInputEditText = holder.getTextInputEditText();
        textInputLayout.setVisibility(View.VISIBLE);
        textInputEditText.setVisibility(View.VISIBLE);
        textInputEditText.setText(textBoxesTexts.get(position));

        if(textBoxesHints != null) {
            textInputLayout.setHint(textBoxesHints.get(position));
        }else{
            textInputLayout.setHint(hint);
        }

    }

    @Override
    public int getItemCount() {
        if(textBoxesHints == null) {
            return 0;
        }
        return textBoxesHints.size();
    }

    public class DestinationsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, TextWatcher{
        private final TextInputLayout textInputLayout;
        private final TextInputEditText textInputEditText;
        private final Button button;

        public DestinationsViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "new view");
            button = itemView.findViewById(R.id.cancel_input_button);
            textInputLayout = itemView.findViewById(R.id.txt_layout_in_recyclerview);
            textInputEditText = itemView.findViewById(R.id.txt_edit_in_recyclerview);
            textInputEditText.addTextChangedListener(this);
            button.setOnClickListener(this);
        }

        public TextInputLayout getTextInputLayout() {
            return textInputLayout;
        }

        public TextInputEditText getTextInputEditText() {
            return textInputEditText;
        }

        public Button getButton() {
            return button;
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener != null) {
                int position = getBindingAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onItemClick(position);
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(onItemClickListener != null) {
                int position = getBindingAdapterPosition();
                if(position != RecyclerView.NO_POSITION) {
                    onItemClickListener.onKeyPressed(position, s.toString());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}

