package fantomit.zwalkowepegle.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.R;

public class RiverListAdapter extends ArrayAdapter<River> {
    public List<River> rivers;
    private LayoutInflater inflater;

    public RiverListAdapter(Context context, List<River> rivers){
        super(context, R.layout.river_list_item, rivers);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.rivers = rivers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RiverHolder riverHolder;

        if(convertView == null){
            convertView = inflater.inflate(R.layout.river_list_item, parent, false);

            riverHolder = new RiverHolder();
            riverHolder.mRiver = (TextView) convertView.findViewById(R.id.tvName);
            riverHolder.mRiverId = (TextView) convertView.findViewById(R.id.tvRiverId);
            //riverHolder.mTrend = (ImageView) convertView.findViewById(R.id.iconTrend);

            convertView.setTag(riverHolder);
        } else {
            riverHolder = (RiverHolder) convertView.getTag();
        }

        River r = rivers.get(position);

        riverHolder.mRiverId.setText(r.getRiverId());
        riverHolder.mRiver.setText(r.getRiverShort());

//        String trend = r.getTrend();
//        if(trend.equals("const")){
//            riverHolder.mTrend.setImageResource(R.drawable.ic_trending_neutral_black_36dp);
//        } else if(trend.equals("down")){
//            riverHolder.mTrend.setImageResource(R.drawable.ic_trending_down_black_36dp);
//        } else if(trend.equals("up")){
//            riverHolder.mTrend.setImageResource(R.drawable.ic_trending_up_black_36dp);
//        } else{
//            riverHolder.mTrend.setImageResource(R.drawable.ic_water_off_black_36dp);
//        }

        return convertView;
    }

    @Override
    public int getCount() {
        return rivers.size();
    }

    @Override
    public River getItem(int position) {
        return rivers.get(position);
    }

    public static class RiverHolder {
        public TextView mRiver;
        public TextView mRiverId;
        //public ImageView mTrend;
    }
}
