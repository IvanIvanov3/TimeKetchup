package com.bytebazar.timeketchup.settings;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;


import com.bytebazar.timeketchup.R;

import java.util.ArrayList;
import java.util.List;

class  RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.RingtoneHolder> {

    public interface OnRingtoneClickListener {
        void onRingtoneClick(String uri);
    }

    final private OnRingtoneClickListener listener;
    private List<Ringtone> ringtones = new ArrayList<>();
    private String currentRingtoneUri;
    private String currentTitle = "";

    public RingtoneAdapter(OnRingtoneClickListener listener, String currentRingtoneUri
            , List<Ringtone> ringtones) {

        this.listener = listener;
        this.currentRingtoneUri = currentRingtoneUri;
        this.ringtones = ringtones;
    }

    public String getCurrentRingtoneUri() {
        return currentRingtoneUri;
    }

    public String getCurrentTitle() {
        return currentTitle;
    }

    @Override
    public RingtoneHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.ringtone_item, viewGroup, false);
        return new RingtoneHolder(v);
    }

    @Override
    public void onBindViewHolder(RingtoneHolder holder, int i) {
        holder.bind(ringtones.get(i));
    }


    @Override
    public int getItemCount() {
        return ringtones.size();
    }


    public class RingtoneHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final private RadioButton radioButton;
        final private TextView title;

        public RingtoneHolder(View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.ringtone_chooser);
            title = itemView.findViewById(R.id.ringtone_title);
            itemView.setOnClickListener(this);
        }

        public void bind(Ringtone ringtone) {
            title.setText(ringtone.getTitle());
            radioButton.setChecked(currentRingtoneUri.equals(ringtone.getUri()));
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && listener != null) {
                currentRingtoneUri = ringtones.get(position).getUri();
                currentTitle = String.valueOf(title.getText());
                listener.onRingtoneClick(currentRingtoneUri);
                notifyDataSetChanged();
            }
        }
    }

}
