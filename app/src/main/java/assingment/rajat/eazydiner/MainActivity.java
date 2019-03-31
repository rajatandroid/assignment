package assingment.rajat.eazydiner;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button playButton, stopButton;
    private ProgressDialog progressDialog;
    private static final String url = "https://ia802508.us.archive.org/5/items/testmp3testfile/mpthreetest.mp3";
    private AsyncTask asyncTask;
    private static final String pathToSave = Environment.getExternalStorageDirectory()+"/musicAssignemnt.mp3";
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        if(!checkPermission(this, WRITE_EXTERNAL_STORAGE)){
            requestPermission(this, WRITE_EXTERNAL_STORAGE);
        }
    }

    private void initViews() {
        findViewById(R.id.download_button).setOnClickListener(this);
        playButton = findViewById(R.id.play_button);
        stopButton = findViewById(R.id.stop_button);
        playButton.setOnClickListener(this);
        playButton.setEnabled(false);
        stopButton.setOnClickListener(this);
        stopButton.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.download_button:
                if(checkPermission(this, WRITE_EXTERNAL_STORAGE)){
                    checkConnection();
                }
                else{
                    requestPermission(this, WRITE_EXTERNAL_STORAGE);
                }
                break;
            case R.id.play_button:
                playMusic();
                break;
            case R.id.stop_button:
                mediaPlayer.stop();
                stopButton.setEnabled(false);
                playButton.setEnabled(true);
                break;
        }
    }

    private void checkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            asyncTask  = new DownloadMusicFile().execute(url);
        } else {
            Toast.makeText(this, "No network connection availeble!", Toast.LENGTH_SHORT).show();
        }
    }

    private void playMusic() {
        mediaPlayer = MediaPlayer.create(this, Uri.parse(pathToSave));
        if(mediaPlayer!=null){
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
            playButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
        else{
            Toast.makeText(this, "Some error occurred in downloading file! Please try again!", Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean checkPermission(Context context, String... permissions) {
        boolean chk = false;
        for (String permission : permissions) {
            chk = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return chk;
    }

    public static void requestPermission(Context context, String... permissions) {
        ActivityCompat.requestPermissions((AppCompatActivity) context, permissions,
                AppCompatActivity.RESULT_FIRST_USER);
    }

    class DownloadMusicFile extends AsyncTask<String, String, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            initialiseProgress();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            int count;
            try {
                URL url = new URL(strings[0]);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                int fileLength = urlConnection.getContentLength();

                InputStream inputStream = new BufferedInputStream(url.openStream(), 8192);

                OutputStream outputStream = new FileOutputStream(pathToSave);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/fileLength));
                    outputStream.write(data, 0, count);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(Boolean s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if(s){
                playButton.setEnabled(true);
            }
            else{
                Toast.makeText(MainActivity.this, "Unable to download file due to network error!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(asyncTask!=null){
           asyncTask.cancel(true);
        }
        if(mediaPlayer!=null){
            mediaPlayer.release();
        }
    }

    private void initialiseProgress() {
       progressDialog = new ProgressDialog(this);
       progressDialog.setMessage("Downloading Music...");
       progressDialog.setCancelable(false);
       progressDialog.setIndeterminate(false);
       progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
       progressDialog.setMax(100);
       progressDialog.show();
    }

}
