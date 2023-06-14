package edu.hkust.jmatch.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import edu.hkust.jmatch.R;

public class BarCharAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final double[] scores;
    String[] colors = new String[]{"#483D8B", "#6A5ACD", "#7B68EE", "#9370DB", "#8A2BE2", "#9400D3", "#9932CC", "#BA55D3", "#CDB5CD", "#E0B0FF"};

    public BarCharAdapter(Context context, String[] values, double[] scores) {
        super(context, R.layout.bar_item, values);
        this.context = context;
        this.values = values;
        this.scores = scores;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bar_item, parent, false);
        TextView textView = rowView.findViewById(R.id.bar);
        textView.setText(values[position]);
        textView.setBackgroundColor(Color.parseColor(colors[position]));
        textView.setWidth((int) (200 + 1000 * scores[position] / 100));
        return rowView;
    }
}
