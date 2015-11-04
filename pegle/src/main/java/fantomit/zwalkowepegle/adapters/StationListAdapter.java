package fantomit.zwalkowepegle.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fantomit.zwalkowepegle.APImodels.Station;
import fantomit.zwalkowepegle.R;

public class StationListAdapter extends ArrayAdapter<Station> {
    public List<Station> stations;
    private LayoutInflater inflater;

    public StationListAdapter(Context context, List<Station> stations) {
        super(context, R.layout.river_list_item, stations);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.stations = stations;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RiverHolder riverHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.station_list_item, parent, false);

            riverHolder = new RiverHolder();
            riverHolder.mStation = (TextView) convertView.findViewById(R.id.tvName);
            riverHolder.mTrend = (ImageView) convertView.findViewById(R.id.iconTrend);
            riverHolder.mLevel = (TextView) convertView.findViewById(R.id.level);
            riverHolder.mPrzeplyw = (TextView) convertView.findViewById(R.id.przeplyw);

            convertView.setTag(riverHolder);
        } else {
            riverHolder = (RiverHolder) convertView.getTag();
        }

        Station s = stations.get(position);

        riverHolder.mStation.setText(s.getName());
        riverHolder.mLevel.setText(Integer.toString(s.getStatus().getCurrentValue()) + " cm");
        if (s.getDischargeRecords().isEmpty()) {
            riverHolder.mPrzeplyw.setVisibility(View.GONE);
        } else {
            riverHolder.mPrzeplyw.setVisibility(View.VISIBLE);
            String przeplyw = Double.toString(s.getDischargeRecords().get(s.getDischargeRecords().size() - 1).getValue()) + " m<sup>3</sup>/s";
            riverHolder.mPrzeplyw.setText(Html.fromHtml(przeplyw), TextView.BufferType.SPANNABLE);
        }
        String trend = s.getTrend();
        /*==========Przy ustawieniu customowych poziomów charakterystycznych dla stacji=========*/
        if (s.getLw_poziom() != -1 && s.getLw_poziom() < s.getStatus().getCurrentValue()) {
            riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.up));
        } else if (s.getLw_poziom() != -1) {
            riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.down));
        } else {
            riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.unknown));
        }
        if (trend.equals("const")) {
            riverHolder.mTrend.setImageResource(R.drawable.ic_trending_neutral_black_48dp);
            //riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.neutral));
        } else if (trend.equals("down")) {
            riverHolder.mTrend.setImageResource(R.drawable.ic_trending_down_black_48dp);
            //riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.down));
        } else if (trend.equals("up")) {
            riverHolder.mTrend.setImageResource(R.drawable.ic_trending_up_black_48dp);
            //riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.up));
        } else {
            riverHolder.mTrend.setImageResource(R.drawable.ic_help_black_36dp);
            //riverHolder.mLevel.setTextColor(getContext().getResources().getColor(R.color.unknown));
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public Station getItem(int position) {
        return stations.get(position);
    }

    public static class RiverHolder {
        public TextView mStation;
        public TextView mLevel;
        public TextView mPrzeplyw;
        public ImageView mTrend;
    }
}
