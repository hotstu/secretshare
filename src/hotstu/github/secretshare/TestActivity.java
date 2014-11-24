package hotstu.github.secretshare;

import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_code);
        
        LinearLayout frame = (LinearLayout) findViewById(R.id.frame_before_code_generated);
        LinearLayout frame2 = (LinearLayout) findViewById(R.id.frame_after_code_generated);
        frame.setVisibility(View.GONE);
        ImageView iv = (ImageView) frame2.findViewById(R.id.image_qrcode);
        Bitmap b = null;
        try {
            b = this.genQrcode("1234567sdfghjdfghjsdfghjcvbjnmdfghjhello,world包含汉字并且非常非常long还有日本語あいうえおとうようにはいしていた12314188812阿打发打发地方官", 512);
        } catch (WriterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        iv.setImageBitmap(b);
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
        
      //Canvas canvas = new Canvas(b);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D
        DisplayMetrics dm = getResources().getDisplayMetrics();
//        float fontsize = TypedValue.applyDimension(
//                TypedValue.COMPLEX_UNIT_SP, 20f, dm);
        float fontsize = 20f;
        paint.setColor(Color.rgb(61, 61, 61));
        // text size in pixels
        paint.setTextSize(fontsize);
        Rect bounds = new Rect();
        paint.getTextBounds("由GFW提供", 0, "由GFW提供".length(), bounds);
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.RED);
        
        int width = matrix.getWidth();  
        int height = matrix.getHeight();
        int[] pixels = new int[width * height]; 
        for (int y = 0; y < height; y++) {  
            for (int x = 0; x < width; x++) {  
                if (matrix.get(x, y)) {  
                    pixels[y * width + x] = Color.BLACK;  
                }  else {
                    pixels[y * width + x] = Color.WHITE;
                }
            }  
        }  
        Bitmap bitmap = Bitmap.createBitmap(width, height,  
                Bitmap.Config.ARGB_8888);  
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        
        
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = bitmap.getHeight() - bounds.height();
        System.out.println(String.format(" w=%d bw=%d x=%d y=%d", bitmap.getWidth(), bounds.width(), x, y));
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText("由GFW提供", x, y, paint);
        return bitmap;
    }
}
