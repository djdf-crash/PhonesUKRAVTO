package ua.in.ukravto.kb.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Response;
import ua.in.ukravto.kb.BuildConfig;
import ua.in.ukravto.kb.R;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.utils.NotificationBuilderHelper;
import ua.in.ukravto.kb.utils.Pref;

import static android.support.constraint.Constraints.TAG;

public class DownloadService extends IntentService {

    private final int notificationId = 999;
    private final String NAME_UPDATE_FILE = "update.apk";
    private final String PATH_DOWNLOAD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator;


    public DownloadService() {
        super("Download service UkrAVTO");
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        Log.d(TAG, "START DownloadService!");

        String token = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN,"");
        try {
            final Response<ResponseBody> resp = RetrofitHelper.getPhoneService().getDownloadLastUpdateAPP(token).execute();
            if (resp.isSuccessful()){

                final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                final NotificationCompat.Builder mBuilder = NotificationBuilderHelper.buildMessage(getApplicationContext(),
                        getString(R.string.title_notif_download),
                        getString(R.string.text_notif_download),
                        NotificationCompat.PRIORITY_LOW,
                        NotificationCompat.CATEGORY_PROGRESS);


                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        boolean writtenToDisk = writeResponseBodyToDisk(getApplicationContext(), resp, mBuilder, notificationManager);

                        Log.d(TAG, "file download was a success? " + writtenToDisk);
                        return writtenToDisk;
                    }

                    @Override
                    protected void onPostExecute(Boolean writtenToDisk) {
                        super.onPostExecute(writtenToDisk);
                        if (writtenToDisk){
                            notificationManager.cancel(notificationId);
                            Intent intentForPending = createIntentForInstallAPP(getApplicationContext());
                            getApplicationContext().startActivity(Intent.createChooser(intentForPending,""));
                        }else {
                            final PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
                            mBuilder.setContentText(getString(R.string.text_download_fail)).setProgress(0,0,false);
                            mBuilder.setContentIntent(pendingIntent).addAction(R.drawable.ic_retry_black,getString(R.string.text_retry), pendingIntent);
                            notificationManager.notify(notificationId, mBuilder.build());
                        }
                    }
                }.execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Intent createIntentForInstallAPP(Context ctx) {
        String dest = PATH_DOWNLOAD + NAME_UPDATE_FILE;
        Intent intent = new Intent(Intent.ACTION_VIEW);;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(ctx, BuildConfig.APPLICATION_ID + ".provider", new File(dest));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(contentUri);
        }else {
            final Uri uri = Uri.fromFile(new File(dest));
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setDataAndType(uri,"application/vnd.android.package-archive");
        }
        return intent;
    }

    private boolean writeResponseBodyToDisk(Context ctx, Response<ResponseBody> response, NotificationCompat.Builder mBuilder, NotificationManagerCompat notificationManager) {
        try {
            ResponseBody body = response.body();

            File futureStudioIconFile = new File(PATH_DOWNLOAD + NAME_UPDATE_FILE);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                mBuilder.setProgress((int) fileSize,(int)fileSizeDownloaded, false);
                notificationManager.notify(notificationId, mBuilder.build());


                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    mBuilder.setProgress((int) fileSize, (int) fileSizeDownloaded, false);
                    notificationManager.notify(notificationId, mBuilder.build());

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
