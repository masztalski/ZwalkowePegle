package fantomit.zwalkowepegle.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import fantomit.zwalkowepegle.DBmodels.River;
import fantomit.zwalkowepegle.R;

public class RiverListAdapter extends ArrayAdapter<River> {
    public List<River> rivers;
    private LayoutInflater inflater;
    public HashMap<String, Integer> plywalnosc;
    private SparseBooleanArray mSelection = new SparseBooleanArray();

    public RiverListAdapter(Context context, List<River> rivers, HashMap<String, Integer> plywalnosc) {
        super(context, R.layout.river_list_item, rivers);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.rivers = rivers;
        this.plywalnosc = plywalnosc;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RiverHolder riverHolder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.river_list_item, parent, false);

            riverHolder = new RiverHolder();
            riverHolder.mRiver = (TextView) convertView.findViewById(R.id.tvName);
            riverHolder.mRiverId = (TextView) convertView.findViewById(R.id.tvRiverId);
            riverHolder.mRiverPlywalnosc = (TextView) convertView.findViewById(R.id.tvPlywalnoscRzeki);
            //riverHolder.mTrend = (ImageView) convertView.findViewById(R.id.iconTrend);

            convertView.setTag(riverHolder);
        } else {
            riverHolder = (RiverHolder) convertView.getTag();
        }

        River r = rivers.get(position);

        riverHolder.mRiverId.setText(r.getRiverId());
        riverHolder.mRiver.setText(r.getRiverShort());
        //riverHolder.mRiverPlywalnosc.setVisibility(View.GONE);
        if (plywalnosc.get(r.getRiverId()) != null) {
            riverHolder.mRiverPlywalnosc.setText(Integer.toString(plywalnosc.get(r.getRiverId())) + "/" + Integer.toString(r.getConnectedStations().size()));
        } else {
            riverHolder.mRiverPlywalnosc.setText("-" + "/" + Integer.toString(r.getConnectedStations().size()));
        }

        if (mSelection.get(position)) {
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.holo_blue_light));// this is a selected position so make it red
            Log.e("ADAPTER", "Position " + Integer.toString(position) + " is checked");
        } else {
            convertView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.background_light));
        }

        return convertView;
    }

    public void setNewSelection(int position, boolean value) {
        mSelection.put(position, value);
        notifyDataSetChanged();
    }

    public boolean isPositionChecked(int position) {
        Boolean result = mSelection.get(position);
        return result == null ? false : result;
    }

    public void removeSelection(int position) {
        mSelection.delete(position);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        mSelection = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public ArrayList<Integer> getSelection(){
        ArrayList<Integer> result = new ArrayList<>();
        for(int i = 0; i < mSelection.size(); i++){
            if(mSelection.valueAt(i)){
                result.add(mSelection.keyAt(i));
            }
        }
        return result;
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
        public TextView mRiverPlywalnosc;
        //public ImageView mTrend;
    }
}
