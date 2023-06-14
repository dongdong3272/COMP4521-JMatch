package edu.hkust.jmatch.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.hkust.jmatch.KeywordMatchActivity;
import edu.hkust.jmatch.MainActivity;
import edu.hkust.jmatch.R;
import edu.hkust.jmatch.backend.Global;

public class MatchRecordAdapter extends RecyclerView.Adapter<MatchRecordAdapter.MyViewHolder> {
    private List<MatchRecord> recordList;
    private Context mContext;

    public MatchRecordAdapter(Context mContext,List<MatchRecord> myDataList) {
        this.mContext = mContext;
        this.recordList = myDataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.match_record, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        MatchRecord currentRecord = recordList.get(position);
        // Bind the MatchRecord data to the views here
        holder.bindTo(currentRecord);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Member Variables for the MatchRecord
        private TextView titleText;
        private TextView timeText;

        MyViewHolder(View itemView){
            super(itemView);
            titleText = itemView.findViewById(R.id.welcomeText);
            timeText = itemView.findViewById(R.id.timeText);
            itemView.setOnClickListener(this);
        }

        /**
         *  set the content of the item layout
         *  @param currentCard
         */
        void bindTo(MatchRecord currentCard){
            titleText.setText(currentCard.getTitle());
            timeText.setText(currentCard.getTime());
        }

        @Override
        public void onClick(View v) {
            MatchRecord currentRecord = recordList.get(getAdapterPosition());
            Global.Document.documentID = currentRecord.getDocumentID();
            Intent gotoMatchHistoryIntent = new Intent(mContext, KeywordMatchActivity.class);
            mContext.startActivity(gotoMatchHistoryIntent);
        }
    }
}
