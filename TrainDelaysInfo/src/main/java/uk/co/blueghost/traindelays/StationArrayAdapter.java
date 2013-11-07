package uk.co.blueghost.traindelays;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blueg_000 on 07/11/13.
 */
public class StationArrayAdapter extends ArrayAdapter<String>{

    private final List<Station> stationList;
    private final List<String> autoCompleteList;

    public StationArrayAdapter(Context context, int resource, List<String> objects, List<Station> stations) {
        super(context, resource, objects);

        autoCompleteList = objects;
        stationList = stations;
    }

    @Override
    public Filter getFilter() {
        return new StationFilter(this.stationList);
    }

    private class StationFilter extends Filter {

        private final List<Station> stationsList;

        public StationFilter(List<Station> stations) {
            stationsList = stations;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults fr = new FilterResults();
            if (constraint == null || constraint.length() == 0){
                Log.println(Log.INFO, "Query", "No Query");
                fr.count = 0;
                fr.values = new ArrayList<String>();
                return fr;
            }
            List<String> results = new ArrayList<String>();
            String constraintStr = constraint.toString().toLowerCase();
            Log.println(Log.INFO, "Query", constraintStr);
            Log.println(Log.INFO, "Query", "Searching:" + stationList.size() + " stations");
            for(Station station: stationsList){
                if (station.CRS == constraintStr) {
                    results.add(station.Description);
                    break;
                } else if (station.CRS.toLowerCase().startsWith(constraintStr) || station.Description.toLowerCase().startsWith(constraintStr)) {
                    results.add(station.Description);
                }
            }
            fr.count = results.size();
            fr.values = results;
            Log.println(Log.INFO, "Query", "results:" + fr.count);
            return fr;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.count > 0) {
                Log.println(Log.INFO, "Results", "FOUND");
                autoCompleteList.clear();
                autoCompleteList.addAll((ArrayList<String>) results.values);
                notifyDataSetChanged();
            } else {
                Log.println(Log.INFO, "Results", "-");
                notifyDataSetInvalidated();
            }
        }
    }
}


