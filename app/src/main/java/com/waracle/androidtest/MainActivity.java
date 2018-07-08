package com.waracle.androidtest;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    private static String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Fragment is responsible for loading in some JSON and
     * then displaying a list of cakes with images.
     * Fix any crashes
     * Improve any performance issues
     * Use good coding practices to make code more secure
     */
    public static class PlaceholderFragment extends ListFragment {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        private ListView mListView;
        private MyAdapter mAdapter;
        private int list_pos;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            //If applicable, receive the list position the user was at before rotating the screen
            if (savedInstanceState != null){
                this.list_pos = savedInstanceState.getInt("LIST_POS");
            }

            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Create and set the list adapter.
            mAdapter = new MyAdapter();
            mListView = getListView();
            mListView.setAdapter(mAdapter);

            // Load data from net.
            URL url = null;
            try {
                url = new URL(JSON_URL);
                new loadData().execute(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

        public int getListPos(){
           return mListView.getFirstVisiblePosition();
        }

        //saves the user's position on the list when orientation is flipped and the fragment redrawn
        @Override
        public void onSaveInstanceState(Bundle savedState) {

            super.onSaveInstanceState(savedState);

            savedState.putInt("LIST_POS",  getListPos());
        }


        // short network operations should be run in an AsyncTask or other thread so as not to block the UI thread
        private class loadData extends AsyncTask<URL, String, String> {

            @Override
            protected String doInBackground(URL...params) {

                URL url = params[0];
                String data = "";


                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }

                try {
                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){

                     BufferedReader in = null;
                        try {
                            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
                            StringBuffer stringBuffer = new StringBuffer();
                            String line = "";
                            while ((line = in.readLine()) != null) {
                                stringBuffer.append(line);
                            }

                            data = stringBuffer.toString();


                        }catch (IOException e){
                            e.printStackTrace();
                        } finally {

                            StreamUtils.close(in);
                            urlConnection.disconnect();
                        }

                        return data;

                        //anything other than a 200 response code will cause an error and the cake information will not be downloaded
                        //TODO: error handling and something to show the user that there has been a network error eg. toast message or dialog
                    } else {
                        return null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                JSONArray array = null;

                //once the data has been received by the app it can then be added to the listview
                try {
                    array = new JSONArray(s);
                    mAdapter.setItems(array);
                    mListView = getListView();
                    mListView.setAdapter(mAdapter);
                    mListView.setSelectionFromTop(list_pos, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }

        private class MyAdapter extends BaseAdapter {

            // Can you think of a better way to represent these items???
            private JSONArray mItems;
            private ImageLoader mImageLoader;

        /*    //Using the ViewHolder means that the system doesn't have to repeatedly call "findViewById"
            //which will make loading the listview faster
            class ViewHolder {
                TextView title;
                TextView desc;
                ImageView image;
            }*/

            public MyAdapter() {
                this(new JSONArray());
            }

            public MyAdapter(JSONArray items) {
                mItems = items;
                mImageLoader = new ImageLoader();
            }

            @Override
            public int getCount() {
                return mItems.length();
            }

            @Override
            public Object getItem(int position) {
                try {
                    return mItems.getJSONObject(position);
                } catch (JSONException e) {
                    Log.e("", e.getMessage());
                }
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }



            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());

                convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
                if (convertView != null) {
                    TextView title = (TextView) convertView.findViewById(R.id.title);
                    TextView desc = (TextView) convertView.findViewById(R.id.desc);
                    ImageView image = (ImageView) convertView.findViewById(R.id.image);


                    try {
                        JSONObject object = (JSONObject) getItem(position);
                        title.setText(object.getString("title"));
                        desc.setText(object.getString("desc"));
                        mImageLoader.load(object.getString("image"), image);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                    return convertView;

            }

            public void setItems(JSONArray items) {
                mItems = items;
            }
        }



        //Had intended to implement ViewHolder pattern, however it was causing list rows
        // to be recycled and show old images before new ones were loaded

       /* //    @SuppressLint("ViewHolder")
          //  @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());

                ViewHolder holder;
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.title);
                    holder.desc = (TextView) convertView.findViewById(R.id.desc);
                    holder.image = (ImageView) convertView.findViewById(R.id.image);
                    convertView.setTag(holder);


                }else {
                    holder = (ViewHolder) convertView.getTag();

                    //listview will recycle old image into incorrect row while new image is loading, so the imageView is made transparent until it is loaded
                    holder.image.setImageDrawable(getResources().getDrawable(android.R.color.transparent));
                }

                try {
                    JSONObject object = (JSONObject) getItem(position);
                    holder.title.setText(object.getString("title"));
                    holder.desc.setText(object.getString("desc"));
                    mImageLoader.load(object.getString("image"), holder.image);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return convertView;
            }*/

    }


    /*    private JSONArray loadData() throws IOException, JSONException {
            URL url = new URL(JSON_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // Can you think of a way to improve the performance of loading data
                // using HTTP headers???

                // Also, Do you trust any utils thrown your way????

                byte[] bytes = StreamUtils.readUnknownFully(in);

                // Read in charset of HTTP content.
                String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

                // Convert byte array to appropriate encoded string.
                String jsonText = new String(bytes, charset);

                // Read string as JSON.
                return new JSONArray(jsonText);
            } finally {
                urlConnection.disconnect();
            }
        }

     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     *//*
        public static String parseCharset(String contentType) {
            if (contentType != null) {
                String[] params = contentType.split(",");
                for (int i = 1; i < params.length; i++) {
                    String[] pair = params[i].trim().split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("charset")) {
                            return pair[1];
                        }
                    }
                }
            }
            return "UTF-8";
        }*/


}


