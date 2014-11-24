package hotstu.github.secretshare.bdapi;

import hotstu.github.secretshare.App;
import hotstu.github.secretshare.utils.FileUtil;
import hotstu.github.secretshare.utils.HttpUtil;
import hotstu.github.secretshare.utils.IoUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Hashtable;

import org.apache.commons.io.FilenameUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class RapidUpTask extends AsyncTask<Entity, String, Bitmap> {

    public interface OnProcessListener {
        public void onStart(String msg);

        public void onProgress(String msg);

        public void onSucess(Bitmap img);

        public void onFailed(String msg);

        public void onFinish();
    }

    private OnProcessListener mListener;
    private Context mContext;

    public RapidUpTask(Context context, OnProcessListener listener) {
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
//        String filenamebase = FilenameUtils.getBaseName(e.getFilename());
//        String fileExtension = FilenameUtils.getExtension(e.getFilename());
//        if (filenamebase.length() > 80) {
//            filenamebase = filenamebase.substring(0, 80);
//        }
        //不检查文件名长度，可能导致二维码过密
        final String fileName = e.getFilename();
        StringBuilder sb = new StringBuilder();
        sb.append(contentLength)
        .append(';')
        .append(contentMD5)
        .append(sliceMD5)
        .append(fileName);
        try {
            Bitmap bitmap = genQrcode(sb.toString(), 512);
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











