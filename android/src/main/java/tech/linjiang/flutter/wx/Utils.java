package tech.linjiang.flutter.wx;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

class Utils {
    private static Context CONTEXT;

    static void init(Context context) {
        CONTEXT = context.getApplicationContext();
    }

    private static ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            1, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(2)) {{
        allowCoreThreadTimeOut(true);
    }};

    static Context getContext() {
        return CONTEXT;
    }

    static byte[] httpGet(String url) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(15_000);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while (-1 != (len = is.read(buffer))) {
                out.write(buffer, 0, len);
                out.flush();
            }
            return out.toByteArray();
        }
        throw new IOException(""+conn.getResponseCode());
    }

    static void execute(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

    static byte[] getFileBytes(String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static String getJsonStringSafely(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    static String getJsonStringSafely(JSONArray jsonObject, int index) {
        try {
            return jsonObject.getString(index);
        } catch (JSONException e) {
            return null;
        }
    }

    static JSONArray getJsonArraySafely(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getJSONArray(key);
        } catch (JSONException e) {
            return null;
        }
    }

    static int getJsonIntSafely(JSONObject jsonObject, String key) {
        try {
            return jsonObject.getInt(key);
        } catch (JSONException e) {
            return 0;
        }
    }

    static void log(String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d("flutter_wx", String.format(msg, args));
        }
    }
}
