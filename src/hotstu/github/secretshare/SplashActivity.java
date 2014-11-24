package hotstu.github.secretshare;

import java.io.IOException;

import hotstu.github.secretshare.utils.HttpUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
    private Handler mhandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        
        //MobclickAgent.setDebugMode(true);
//      SDK在统计Fragment时，需要关闭Activity自带的页面统计，
//      然后在每个页面中重新集成页面统计的代码(包括调用了 onResume 和 onPause 的Activity)。
        MobclickAgent.openActivityDurationTrack(false);
//      MobclickAgent.setAutoLocation(true);
//      MobclickAgent.setSessionContinueMillis(1000);
        
        MobclickAgent.updateOnlineConfig(this);
        
        mhandler = new Handler();
        Runnable logincheck = new Runnable() {

            @Override
            public void run() {
                if (App.SESSION == null) {
                    onGetUserName(null);
                    return;
                } else {
                    OkHttpClient client = HttpUtil.getOkHttpClientinstance();
                    Request req = new Request.Builder()
                            .url("http://passport.baidu.com/center")
                            .addHeader("Cookie", App.SESSION)
                            .addHeader("User-Agent", HttpUtil.UA_FIREFOX)
                            .build();

                    try {
                        Response resp = client.newCall(req).execute();
                        String header = resp.header("Set-Cookie", null);
                        if (header != null) {
                            String bduid = header.split(";")[0].trim();
                            App.SESSION += ("; " + bduid);
                        }
                        String html = resp.body().string();
                        Document soup = Jsoup.parse(html);
                        final String name = soup.select("div#displayUsername")
                                .text();
                        onGetUserName(name);
                    } catch (IOException e) {
                        e.printStackTrace();
                        onGetUserName(null);
                    }
                }

            }

            public void onGetUserName(final String userName) {
                mhandler.post(new Runnable() {
                    
                    @Override
                    public void run() {
                        if (userName == null) {
                            //获取用户名失败，可能的原因 1 没有网络连接 2 网络访问错误 3 session失效
                            App.USER_DISPLAY_NAME = null;
                        }
                        else {
                            App.USER_DISPLAY_NAME = userName;
                        }
                        startMainActivity();
                    }
                });
            }
        };
        
        if (savedInstanceState != null && savedInstanceState.getBoolean("logincheck") == true 
                && App.SESSION != null) {
            //we will go MainActivity directly
            startMainActivity();
        }
        else {
            //before go MainActivity, we check login
            new Thread(logincheck).start();
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart("SplashScreen"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd("SplashScreen"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("loginchecked", true);
    }
    
    private void startMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        this.finish();
    }
    
    

}
