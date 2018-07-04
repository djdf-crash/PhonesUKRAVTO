package ua.in.ukravto.kb.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Response;
import ua.in.ukravto.kb.repository.service.RetrofitHelper;
import ua.in.ukravto.kb.utils.Pref;

import static android.support.constraint.Constraints.TAG;

public class DownloadService extends Service {
    @SuppressLint("StaticFieldLeak")
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "START DownloadService!");

        String token = Pref.getInstance(getApplicationContext()).getString(Pref.USER_TOKEN,"");
        try {
            final Response<ResponseBody> resp = RetrofitHelper.getPhoneService().getDownloadLastUpdateAPP(token).execute();
            if (resp.isSuccessful()){
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        boolean writtenToDisk = writeResponseBodyToDisk(resp);

                        Log.d(TAG, "file download was a success? " + writtenToDisk);
                        return null;
                    }
                }.execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean writeResponseBodyToDisk(Response<ResponseBody> response) {
        try {
            ResponseBody body = response.body();

            File futureStudioIconFile = new File(getExternalFilesDir(null) + File.separator + "app.apk");

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

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
