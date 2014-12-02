package hotstu.github.secretshare.bdapi;

import hotstu.github.secretshare.App;
import hotstu.github.secretshare.utils.HttpUtil;
import hotstu.github.secretshare.utils.cipher.AESCodec;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class MoRapidDownTask extends AsyncTask<String, String, Boolean> {

    public interface OnProcessListener {
        public void onStart(String msg);

        public void onProgress(String msg);

        public void onSucess(String msg);

        public void onFailed(@Nullable String msg);

        public void onFinish();
    }

    private OnProcessListener mListener;
    private String fileName;
    private String errmsg;

    public MoRapidDownTask(OnProcessListener listener) {
        super();
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.onStart("正在提取文件信息...");
    }

    @Override
    protected Boolean doInBackground(String... params) {
        final String hash = params[0];
        try {
            String kd = get(hash);
            if (kd == null) {
                return false;
            }
            JsonParser j = new JsonParser();
            JsonElement je = j.parse(kd);
            int errno = je.getAsJsonObject().get("errno").getAsInt();
            if (errno != 0) {
                errmsg = "无效二维码或者已失效";
                return false;
            }
            String key = je.getAsJsonObject().get("key").getAsString();
            String data = je.getAsJsonObject().get("data").getAsString();
            String jsonstr = AESCodec.decrypt(data, key);
            Gson g = new Gson();
            Type typeOfSrc = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> datadict = g.fromJson(jsonstr, typeOfSrc);

            String length = datadict.get("content-length");
            String contentMD5 = datadict.get("content-md5");
            ;
            String sliceMD5 = datadict.get("slice-md5");
            ;
            this.fileName = datadict.get("titile");
            ;
            String filePath = "/SecretShare/" + fileName;
            String crc32 = datadict.get("content-crc32");
            StringBuilder payload = new StringBuilder();

            payload.append("method=rapidupload").append('&').append("path=")
                    .append(URLEncoder.encode(filePath, "utf-8")).append('&').append("content-length=")
                    .append(length).append('&').append("content-md5=")
                    .append(contentMD5).append('&').append("slice-md5=")
                    .append(sliceMD5).append('&').append("content-crc32=")
                    .append(crc32);

            String json = push(payload.toString());
            JsonObject jo = j.parse(json).getAsJsonObject();
            int err = jo.get("errno").getAsInt();
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
            mListener.onFailed(errmsg);
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

    private String get(String key) throws IOException {
        final String queryUrl = "http://ddddddd.jd-app.com/secretshare/%s/";

        Request req = new Request.Builder().url(String.format(queryUrl, key))
                .addHeader("User-Agent", "Secretshare v" + App.VERSION_CODE)
                .addHeader("Accept", "*/*").get().build();
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        Response resp = client.newCall(req).execute();
        HttpUtil.debugHeaders(resp);
        return resp.body().string();
    }
}
