package com.udacity.gradle.builditbigger;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.rks.myapplication.backend.myApi.MyApi;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.myapps.rk.jokeactivity.JokeActivity;
import com.rkapps.JokeTeller;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    final String LOG_TAG = MainActivity.class.getSimpleName();
    ProgressBar mProgressBar;
    InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        requestNewInterstitial();

        //Call GCE AsyncTask
        //new EndpointsAsyncTask().execute(new Pair<Context, String>(this, "Manfred"));
        //new EndpointsAsyncTask().execute(new Pair<Context, String>(this, "Android"));
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("5554")
                .build();

        mInterstitialAd.loadAd(adRequest);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void tellJoke(View view) {
        Log.d(LOG_TAG, "riddhik Method tellJoke()");

//        JokeTeller jt = new JokeTeller();
//        Toast.makeText(this, jt.getARandomJoke(), Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(this, JokeActivity.class);
//        intent.putExtra(Intent.EXTRA_TEXT, jt.getARandomJoke());
//        startActivity(intent);

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                new EndpointsAsyncTask().execute(new Pair<Context, String>(getApplicationContext(), ""));
            }
        });

        requestNewInterstitial();
    }

    private class EndpointsAsyncTask extends AsyncTask<Pair<Context, String>, Integer, String> {
        final String LOG_TAG = EndpointsAsyncTask.class.getSimpleName();

        private MyApi myApiService = null;
        private Context context;

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            Log.d(LOG_TAG, "riddhik In doInBackground() method");

            if (myApiService == null) {  // Only do this once
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // options for running against local devappserver
                        // - 10.0.2.2 is localhost's IP address in Android emulator
                        // - turn off compression when running against local devappserver
                        .setRootUrl("http://10.0.2.2:8080/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                // end options for devappserver

                myApiService = builder.build();
            }

            context = params[0].first; //application context
            String name = params[0].second; //input String value

            //To generate progress bar
            for (int i = 0; i < 10; i++) {
                sleep();
                publishProgress(i * 20);
            }

            try {
//            return myApiService.sayHi(name).execute().getData();
//            return myApiService.sayJoke(name).execute().getData();
                return myApiService.sayARandomJoke().execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(LOG_TAG, "riddhik In onPostExecute() method");

//            Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            mProgressBar.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(getApplicationContext(), JokeActivity.class);
            intent.putExtra(Intent.EXTRA_TEXT, result); //result from doInBackground
            startActivity(intent);
        }

        public void sleep() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
