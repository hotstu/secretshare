package hotstu.github.secretshare.bdapi;

import hotstu.github.secretshare.App;
import hotstu.github.secretshare.utils.HttpUtil;

import java.io.IOException;

import android.os.AsyncTask;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class RapidDownTask extends AsyncTask<String, String, Boolean> {

    public interface OnProcessListener {
        public void onStart(String msg);

        public void onProgress(String msg);

        public void onSucess(String msg);

        public void onFailed();

        public void onFinish();
    }

    private OnProcessListener mListener;
    private String fileName;

    public RapidDownTask(OnProcessListener listener) {
        super();
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.onStart("正在提取文件信息...");
    }

    @Override
    protected Boolean doInBackground(String... params) {
        final String data = params[0];
        int splictIndex = data.indexOf(';');
        if (splictIndex == -1)
            return false;
        String length = data.substring(0, splictIndex);

        String contentMD5 = data.substring(splictIndex + 1, splictIndex + 33);
        String sliceMD5 = data.substring(splictIndex + 33, splictIndex + 65);
        this.fileName = data.substring(splictIndex + 65);
        String filePath = "/SecretShare/" + fileName;
        StringBuilder payload = new StringBuilder();
        /**
         * 'method': 'rapidupload', 'path':payload.get('path',''),
         * 'content-length':payload.get('content-length',''),
         * 'content-md5':payload.get('content-md5',''),
         * 'slice-md5':payload.get('slice-md5',''),
         * 'content-crc32':payload.get('content-crc32',''),
         */
        payload.append("method=rapidupload").append('&').append("path=")
                .append(filePath).append('&').append("content-length=")
                .append(length).append('&').append("content-md5=")
                .append(contentMD5).append('&').append("slice-md5=")
                .append(sliceMD5).append('&').append("content-crc32=")
                .append(HttpUtil.pseudoCRC32());
        try {
            String json = push(payload.toString());
            JsonParser parser = new JsonParser();
            JsonObject j = parser.parse(json).getAsJsonObject();
            int err = j.get("errno").getAsInt();
            if (err == 0)
                return true;
            if (err == -8) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }

    }

    @Override
    protected void onProgressUpdate(String... values) {
        mListener.onProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            mListener.onSucess(fileName + " 成功添加到百度网盘");

        } else {
            mListener.onFailed();
        }
        mListener.onFinish();
    }

    @Override
    protected void onCancelled(Boolean result) {
        mListener.onFinish();
    }

    private String push(String payload) throws IOException {
        final String queryUrl = "http://pan.baidu.com/api/rapidupload?clienttype=8&channel=00000000000000000000000000000000&version=5.0.1.6";

        RequestBody body = RequestBody.create(MediaType
                .parse("application/x-www-form-urlencoded; charset=utf-8"),
                payload);
        Request req = new Request.Builder().url(queryUrl)
                .addHeader("Cookie", App.SESSION)
                .addHeader("User-Agent", HttpUtil.UA_GUANJIA)
                .addHeader("Accept", "*/*").post(body).build();
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        Response resp = client.newCall(req).execute();
        HttpUtil.debugHeaders(resp);

        return resp.body().string();

    }
}
