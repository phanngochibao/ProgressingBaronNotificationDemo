package com.example.progressingbaronnotificationdemo;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public String URL="";
    EditText editText;
    ImageView image;
    Button button;
    String CHANNEL_ID = "my_channel_01";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=(EditText)findViewById(R.id.edittxt);
        image = (ImageView) findViewById(R.id.image);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                URL=editText.getText().toString();
                new DownloadImage().execute(URL);
            }
        });
    }

    private class DownloadImage extends AsyncTask<String, String, String> {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);

        int PROGRESS_MAX = 100;
        int PROGRESS_CURRENT = 0;
        int notificationId = 002;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.setContentTitle("Picture Download")
                    .setContentText("Download in progress")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setPriority(NotificationCompat.PRIORITY_LOW);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Test";
                String description = "Day la test";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(description);
                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }

            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationManager.notify(notificationId, builder.build());

        }

        @Override
        protected String doInBackground(String... URL) {

            int count;
            try{
                java.net.URL url = new URL(URL[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                String storageDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                String fileName = "/downloadedfile.jpg";
                File imageFile = new File(storageDir+fileName);
                OutputStream output = new FileOutputStream(imageFile);

                byte data[] = new byte[1024];
                long total = 0;

                while((count = input.read(data)) != -1){
                    total += count;

                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    output.write(data, 0, count);
                }
                output.flush();

                output.close();
                input.close();
            }catch (Exception e){
                Log.e("Error: ", e.getMessage());
            }

            return null;

        }

        protected void onProgressUpdate(String... progress){
            PROGRESS_CURRENT = Integer.parseInt(progress[0]);
            builder.setProgress(PROGRESS_MAX,PROGRESS_CURRENT,true);
        }

        @Override
        protected void onPostExecute(String result) {
            String imagePath = Environment.getExternalStorageDirectory() + "/downloadedfile.jpg";
            image.setImageDrawable(Drawable.createFromPath(imagePath));

            builder.setContentText("Download complete")
                    .setProgress(PROGRESS_MAX,PROGRESS_CURRENT,false);
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
