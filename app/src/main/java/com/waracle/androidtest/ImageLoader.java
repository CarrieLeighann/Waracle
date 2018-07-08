package com.waracle.androidtest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.protocol.HTTP;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;

/**
 * Created by Riad on 20/05/2015.
 */
public class ImageLoader {

    private static final String TAG = ImageLoader.class.getSimpleName();

    public ImageLoader() { /**/ }

    /**
     * Simple function for loading a bitmap image from the web
     *
     * @param url       image url
     * @param imageView view to set image too.
     */
    public void load(String url, ImageView imageView) {

        if (TextUtils.isEmpty(url)) {
            throw new InvalidParameterException("URL is empty!");
        }

        // Can you think of a way to improve loading of bitmaps
        // that have already been loaded previously??

        try {
            new loadImageData(imageView).execute(new URL(url));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    //Network operation again has to into asynchronous task to avoid blocking the UI
    private class loadImageData extends AsyncTask<URL, String, Bitmap>{

        ImageView imageView;


        public loadImageData(ImageView imageView) {
            this.imageView = imageView;

        }

        @Override
        protected Bitmap doInBackground(URL... urls) {

            URL url = urls[0];

            InputStream inputStream = null;
            HttpURLConnection connection = null;



            try {
                connection= (HttpURLConnection) url.openConnection();
                int status = connection.getResponseCode();
                if ( status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == HttpURLConnection.HTTP_SEE_OTHER ||
                        status == HttpURLConnection.HTTP_OK) {

                    //Victoria sponge image url was returning a 301 connection status, so the app has to redirect to the new URL for this image
                    if (status != HttpURLConnection.HTTP_OK) {
                        url = new URL(connection.getHeaderField("Location"));


                        connection = (HttpURLConnection) url.openConnection();
                    }

                    try {
                        // Read data from workstation
                        inputStream = connection.getInputStream();

                        return BitmapFactory.decodeStream(inputStream);

                    } catch (IOException e) {
                        // Read the error from the workstation
                        inputStream = connection.getErrorStream();
                        return null;

                    }

                    //Again needs error handling to feedback to user
                } else {
                    return null;
                }


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Close the input stream if it exists.
                StreamUtils.close(inputStream);

                // Disconnect the connection
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }



        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
                setImageView(imageView, bitmap);

        }

    }


    private static void setImageView(ImageView imageView, Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }

/*
    private static byte[] loadImageData(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        InputStream inputStream = null;
        try {
            try {
                // Read data from workstation
                inputStream = connection.getInputStream();
            } catch (IOException e) {
                // Read the error from the workstation
                inputStream = connection.getErrorStream();
            }

            // Can you think of a way to make the entire
            // HTTP more efficient using HTTP headers??

            return StreamUtils.readUnknownFully(inputStream);
        } finally {
            // Close the input stream if it exists.
            StreamUtils.close(inputStream);

            // Disconnect the connection
            connection.disconnect();
        }
    }*/

  /*  private static Bitmap convertToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }*/


}
