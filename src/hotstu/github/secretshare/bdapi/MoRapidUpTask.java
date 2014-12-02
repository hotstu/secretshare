package hotstu.github.secretshare.bdapi;

import hotstu.github.secretshare.App;
import hotstu.github.secretshare.utils.DeviceUtils;
import hotstu.github.secretshare.utils.FileUtil;
import hotstu.github.secretshare.utils.HttpUtil;
import hotstu.github.secretshare.utils.IoUtils;
import hotstu.github.secretshare.utils.cipher.AESCodec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class MoRapidUpTask extends AsyncTask<Entity, String, Bitmap> {

    public interface OnProcessListener {
        public void onStart(String msg);

        public void onProgress(String msg);

        public void onSucess(Bitmap img);

        public void onFailed(String msg);

        public void onFinish();
    }

    private OnProcessListener mListener;
    private Context mContext;

    public MoRapidUpTask(Context context, OnProcessListener listener) {
        super();
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.onStart("正在提取文件信息...");
    }

    @Override
    protected Bitmap doInBackground(Entity... params) {
        final FileEntity e = (FileEntity) params[0];
        
        final String contentMD5 = e.getMd5();
        final String contentLength = String.valueOf(e.getSize());
        String crc32 = null;
        try {
            crc32 = head(e.getPath());
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        if (crc32 == null || crc32.length() != 8) {
            crc32 = HttpUtil.pseudoCRC32();
        }
        String sliceMD5 = null;
        try { 
            sliceMD5 = fetch(e.getPath());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        if (sliceMD5 == null) {
            return null;
        }
        
        publishProgress("提取成功，正在生成二维码...");

        final String fileName = e.getFilename();
        
        Map<String, String> datadict = new HashMap<String, String>();
        datadict.put("content-length", contentLength);
        datadict.put("content-md5", contentMD5);
        datadict.put("slice-md5", sliceMD5);
        datadict.put("content-crc32", crc32);
        datadict.put("titile", fileName);
        Gson g = new Gson();
        Type typeOfSrc = new TypeToken<Map<String, Object>>(){}.getType();
        String datajson = g.toJson(datadict, typeOfSrc);
        String key = null;
        try {
            key = post(datajson);
        } catch (Exception e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        if (key == null)
            return null;
        try {
            Bitmap bitmap = genQrcode(key, 512);
            return bitmap;
        } catch (WriterException e1) {
            e1.printStackTrace();
            return null;
        }
        
    }
    
    
    
    @Override
    protected void onProgressUpdate(String... values) {
        mListener.onProgress(values[0]);
    }

    
    @Override
    protected void onPostExecute(Bitmap result) {
        if (result == null) {
            mListener.onFailed("发生错误..");
        }
        else {
            mListener.onSucess(result);
        }
        mListener.onFinish();
    }
    
    

    @Override
    protected void onCancelled(Bitmap result) {
        mListener.onFinish();
    }

    /**
     * 获取slice md5
     * @param path
     * @return
     * @throws IOException
     */
    private String fetch(String path) throws IOException {
        final String queryUrl = "http://d.pcs.baidu.com/rest/2.0/pcs/file?app_id=250528&method=download&ec=1&path=%s&err_ver=1.0&es=1";
        Request req = new Request.Builder()
                .url(String.format(queryUrl, URLEncoder.encode(path, "UTF-8")))
                .addHeader("Cookie", App.SESSION)
                .addHeader("User-Agent", HttpUtil.UA_GUANJIA)
                .addHeader("Accept", "*/*")
                .addHeader("Range",
                        String.format("bytes=%d-%d", 0, 256 * 1024 - 1)).get()
                .build();
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        InputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            Response resp = client.newCall(req).execute();
            HttpUtil.debugHeaders(resp);
            is = resp.body().byteStream();
            os = new ByteArrayOutputStream();
            int count;
            byte[] buff = new byte[2048];
            int readed = 0;
            while ((count = is.read(buff, 0, buff.length)) != -1) {
                os.write(buff, 0, count);
                readed += count;
                if (readed >= 256 * 1024)
                    break;
                // System.out.println(readed);
            }
            os.flush();
            byte[] data = os.toByteArray();
            is.close();
            os.close();
            if (data.length < 256 * 1024) {
                // TODO BAD download
                System.out.println(data.length);
                return null;
            } else {
                System.out.println(data.length);
                byte[] content = new byte[256 * 1024];
                for (int i = 0; i < content.length; i++) {
                    content[i] = data[i];
                }
                String sliceMd5 = FileUtil.md5(content);
                return sliceMd5;
            }
        } finally {
            IoUtils.closeSilently(is);
            IoUtils.closeSilently(os);
        }
        
    }
    
    /**
     * 通过发送head请求获取crc32 always check whether it's length == 8
     * @param path
     * @return 
     * @throws IOException 
     */
    private String head(String path) throws Exception {
        final String queryUrl = "http://d.pcs.baidu.com/rest/2.0/pcs/file?app_id=250528&method=download&ec=1&path=%s&err_ver=1.0&es=1";
        Request req = new Request.Builder()
                .url(String.format(queryUrl, URLEncoder.encode(path, "UTF-8")))
                .addHeader("Cookie", App.SESSION)
                .addHeader("User-Agent", HttpUtil.UA_GUANJIA)
                .head()
                .build();
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        Response resp = client.newCall(req).execute();
        Long crc32in = Long.valueOf(resp.header("x-bs-meta-crc32"));
        return String.format("%08x", crc32in);
    }
    
    private String post(String data) throws Exception {
        String imei = DeviceUtils.uniqueId(mContext);
        String macaddr = DeviceUtils.getMacAddress(mContext);
        String aeskey = AESCodec.initkey();
        String encryptData = AESCodec.encrypt(data, aeskey);
        String tempdata = aeskey + ";" + imei + ";" + macaddr;
        //System.out.println(tempdata);
        //python python-rsa 包 和android不兼容，放弃使用rsa加密
//        String key = Base64.encodeToString(RSACoder.encryptByPublicKey(tempdata.getBytes(), 
//                RSAKeyGen.getPublicKey()),
//                Base64.URL_SAFE|Base64.NO_WRAP);
        String key = tempdata;
        StringBuilder sb = new StringBuilder();
        sb.append("method=add&")
        .append("key=")
        .append(URLEncoder.encode(key, "utf-8"))
        .append('&')
        .append("data=")
        .append(URLEncoder.encode(encryptData, "utf-8"));
        //System.out.println(sb.toString());
        
        final String queryUrl = "http://ddddddd.jd-app.com/secretshare/";

        RequestBody body = RequestBody.create(MediaType
                .parse("application/x-www-form-urlencoded; charset=utf-8"),
                sb.toString());
        Request req = new Request.Builder().url(queryUrl)
                .addHeader("User-Agent", "Secretshare v" + App.VERSION_CODE)
                .addHeader("Accept", "*/*").post(body).build();
        OkHttpClient client = HttpUtil.getOkHttpClientinstance();
        Response resp = client.newCall(req).execute();
        HttpUtil.debugHeaders(resp);
        JsonParser jp = new JsonParser();
        String json = resp.body().string();
        System.out.println(json);
        JsonElement je = jp.parse(json);
        if (je.getAsJsonObject().get("errno").getAsInt() == 0) {
            return je.getAsJsonObject().get("id").getAsString();
        }
        else {
            return null;
        }
    }
    
    
    
    /**
     * 
     * @param text 内容
     * @param size 尺寸 长宽相等
     * @return
     * @throws WriterException 
     */
    private Bitmap genQrcode(String text, int size) throws WriterException {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();    
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");   
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size, hints);
        
        String textAuthrity = "下载「度盘秘享」扫描二维码";
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        float fontsize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 20f, dm);
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize(fontsize);
        Rect bounds = new Rect();
        paint.getTextBounds(textAuthrity, 0, textAuthrity.length(), bounds);
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.RED);
        
        int width = matrix.getWidth();  
        int height = matrix.getHeight();
        int[] pixels = new int[width * height]; 
        for (int y = 0; y < height; y++) {  
            for (int x = 0; x < width; x++) {  
                if (matrix.get(x, y)) {  
                    pixels[y * width + x] = Color.BLACK;  
                }  
                else {
                    pixels[y * width + x] = Color.WHITE;
                }
            }  
        }  
        Bitmap bitmap = Bitmap.createBitmap(width, height,  
                Bitmap.Config.ARGB_8888);  
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        
        
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = bitmap.getHeight() - bounds.height();
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(textAuthrity, x, y, paint);
        return bitmap;
    }
}











