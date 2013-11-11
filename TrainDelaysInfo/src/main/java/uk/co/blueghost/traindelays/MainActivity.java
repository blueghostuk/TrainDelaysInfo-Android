package uk.co.blueghost.traindelays;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class MainActivity extends Activity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    List<Station> mStations;

    private Activity mParentActivity = this;
    private List<String> mFromAutoCompleteSource = new ArrayList<String>();
    private List<String> mToAutoCompleteSource = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // call AsynTask to perform network operation on separate thread
        new HttpAsyncTask().execute("http://api.trainnotifier.co.uk/Station/?apiName=co.uk.blueghost.traindelays");
    }

    public void doSearch(View view){
        AutoCompleteTextView fromACTV = (AutoCompleteTextView)findViewById(R.id.fromStationAclTxtView);
        AutoCompleteTextView toACTV = (AutoCompleteTextView)findViewById(R.id.toStationAclTxtView);

        String from = fromACTV.getText().toString();
        String to = toACTV.getText().toString();

        Station fromStation = null,
                toStation = null;
        for (Station s : mStations) {
            if (from.equalsIgnoreCase(s.Description)) {
                fromStation = s;
            }
            if (to.equalsIgnoreCase(s.Description)) {
                toStation = s;
            }
            if (fromStation != null && toStation != null) {
                break;
            }
        }
        if (fromStation == null || toStation == null){
            return;
        }


        ResultsFragment results = (ResultsFragment)mSectionsPagerAdapter.getItem(1);

        EditText edit1 = (EditText)findViewById(R.id.editText);
        edit1.setText(fromStation.Description);
        EditText edit2 = (EditText)findViewById(R.id.editText2);
        edit2.setText(toStation.Description);

        mViewPager.setCurrentItem(1);
    }

    public static List<Station> GET(String url){
        InputStream inputStream;
        List<Station> result = null;
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = readJsonStream(inputStream);

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static List<Station> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readMessagesArray(reader);
        }
            finally {
                reader.close();
            }
    }

    private static List<Station> readMessagesArray(JsonReader reader) throws IOException {
        List<Station> messages = new ArrayList<Station>();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readMessage(reader));
        }
        reader.endArray();
        return messages;
    }

    private static Station readMessage(JsonReader reader) throws IOException {
        String stationName = null;
        String tiploc = null;
        String description = null;
        String crs = null;
        double lat = 0;
        double lon = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("StationName")) {
                stationName = reader.nextString();
            } else if (name.equals("Tiploc")) {
                tiploc = reader.nextString();
            } else if (name.equals("Description")) {
                description = reader.nextString();
            } else if (name.equals("CRS")) {
                crs = reader.nextString();
            } else if (name.equals("Lat") && reader.peek() != JsonToken.NULL) {
                lat = reader.nextDouble();
            } else if (name.equals("Lon") && reader.peek() != JsonToken.NULL) {
                lon = reader.nextDouble();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        Station station = new Station();
        station.StationName = stationName;
        station.Tiploc = tiploc;
        station.Description = description;
        station.CRS = crs;
        station.Lat = lat;
        station.Lon = lon;

        return station;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, List<Station>> {
        @Override
        protected List<Station> doInBackground(String... urls) {

            return GET(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(List<Station> result) {
            Toast.makeText(getBaseContext(), "Received!", Toast.LENGTH_LONG).show();

            StationArrayAdapter fromAdapter = new StationArrayAdapter(mParentActivity, android.R.layout.simple_dropdown_item_1line, mFromAutoCompleteSource, result);
            StationArrayAdapter toAdapter = new StationArrayAdapter(mParentActivity, android.R.layout.simple_dropdown_item_1line, mToAutoCompleteSource, result);

            AutoCompleteTextView fromACTV = (AutoCompleteTextView)findViewById(R.id.fromStationAclTxtView);
            AutoCompleteTextView toACTV = (AutoCompleteTextView)findViewById(R.id.toStationAclTxtView);

            mStations = result;

            fromACTV.setAdapter(fromAdapter);
            toACTV.setAdapter(toAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch(position){
                default:
                case 0:
                    return new PlaceholderFragment();
                case 1:
                    return new ResultsFragment();
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class ResultsFragment extends Fragment  {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_results, container, false);
        }
    }

}
