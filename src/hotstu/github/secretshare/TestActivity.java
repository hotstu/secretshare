package hotstu.github.secretshare;

import hotstu.github.secretshare.bdapi.MoRapidDownTask;
import hotstu.github.secretshare.bdapi.MoRapidUpTask;
import hotstu.github.secretshare.bdapi.MoRapidDownTask.OnProcessListener;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

public class TestActivity extends Activity {
    private Bitmap qrcodeImage;
    private MyHandler mHandler;
    private static String qrcodeText;
    
    //handler 内存泄露
    static class MyHandler extends Handler {
        WeakReference<TestActivity > mActivityReference;
     
        MyHandler(TestActivity activity) {
            mActivityReference= new WeakReference<TestActivity>(activity);
        }
     
        @Override
        public void handleMessage(Message msg) {
            final TestActivity activity = mActivityReference.get();
            if (activity != null) {
                if (msg.what == 0) {
                    // qrcode not found
                    Toast.makeText(activity, "It's not a valid qrcode",
                            Toast.LENGTH_LONG).show();

                } else if (msg.what == 1) {
                    // qrcode found, ask whether send file or not
                    qrcodeText = (String) msg.obj;
//                    Toast.makeText(activity, qrcodeText,
//                            Toast.LENGTH_LONG).show();
                    activity.showDialog();
                }
            }
        }
    }

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_code);

        LinearLayout frame = (LinearLayout) findViewById(R.id.frame_before_code_generated);
        LinearLayout frame2 = (LinearLayout) findViewById(R.id.frame_after_code_generated);
        frame.setVisibility(View.GONE);
        frame2.setVisibility(View.VISIBLE);
        ImageView iv = (ImageView) frame2.findViewById(R.id.image_qrcode);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        mHandler = new MyHandler(this);

        if (Intent.ACTION_SEND.equals(action) && type != null
                && type.startsWith("image/")) {
            handleSendImage(intent); // Handle single image being sent
        } else {
            Toast.makeText(this, "can not handle this intent",
                    Toast.LENGTH_LONG).show();
        }

        if (qrcodeImage != null && !qrcodeImage.isRecycled()) {
            iv.setImageBitmap(qrcodeImage);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    Result r = detectQrcode(qrcodeImage);
                    if (r == null)
                        mHandler.sendEmptyMessage(0);
                    else {
                        Message msg = mHandler.obtainMessage();
                        msg.what = 1;
                        msg.obj = r.getText();
                        mHandler.sendMessage(msg);
                    }

                }
            }).start();
        }

    }

    private  void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("读取二维码成功，是否同步到百度网盘?").setCancelable(true)
                .setPositiveButton("Yes", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new MoRapidDownTask(new OnProcessListener() {
                            
                            @Override
                            public void onSucess(String msg) {
                                Toast.makeText(TestActivity.this, msg, Toast.LENGTH_LONG).show();
                                
                            }
                            
                            @Override
                            public void onStart(String msg) {
                                // TODO Auto-generated method stub
                                
                            }
                            
                            @Override
                            public void onProgress(String msg) {
                                // TODO Auto-generated method stub
                                
                            }
                            
                            @Override
                            public void onFinish() {
                                // TODO Auto-generated method stub
                                
                            }
                            
                            @Override
                            public void onFailed(String msg) {
                                if (msg != null && msg.length() > 0) {
                                   
                                    Toast.makeText(TestActivity.this, msg, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(TestActivity.this, "失败咯,请检查网络和登陆", Toast.LENGTH_LONG).show();
                                }
                                
                                
                            }
                        }).execute(qrcodeText);
                    }
                }).setNegativeButton("Cancel", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (qrcodeImage != null && !qrcodeImage.isRecycled()) {
            qrcodeImage.recycle();
        }
        if (imageUri != null) {
            Log.d("handleSendImage", imageUri.getPath());
            InputStream is = null;
            try {
                is = getContentResolver().openInputStream(imageUri);
                qrcodeImage = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                /* do nothing */
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        /* do nothing */
                    }
                }
            }

        }

    }

    private Result detectQrcode(Bitmap imageBitmap) {
        MultiFormatReader reader = new MultiFormatReader();
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        int[] pixels = new int[width * height];
        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height,
                pixels);
        Result result = null;
        boolean success;

        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            result = reader.decodeWithState(bitmap);
            success = true;
        } catch (ReaderException ignored) {
            success = false;
        }

        // Map<DecodeHintType, Object> decodeHints = new HashMap<DecodeHintType,
        // Object>();
        // decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        return result;
    }

}
