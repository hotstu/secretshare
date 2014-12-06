package hotstu.github.secretshare;

import hotstu.github.secretshare.bdapi.LoginTask;

import java.util.Map;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class LoginActivity extends ActionBarActivity implements
        OnClickListener, LoginTask.OnLoginListener {

    TextView tvUsername;
    TextView tvPassword;
    TextView tvPhilosophy;
    Button btnLogin;
    View captchaContainer;
    Map<String, String> cachedParams = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvUsername = (TextView) findViewById(R.id.login_username);
        tvPassword = (TextView) findViewById(R.id.login_password);
        tvPhilosophy = (TextView) findViewById(R.id.login_philosophy);
        captchaContainer = findViewById(R.id.login_captcha_container);
        btnLogin = (Button) findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd("LoginActivity"); // 保证 onPageEnd 在onPause
                                                  // 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart("LoginActivity"); // 统计页面
        MobclickAgent.onResume(this); // 统计时长
    }

    @Override
    public void onClick(View v) {
        final CharSequence username = tvUsername.getText();
        final CharSequence password = tvPassword.getText();
        if (username == null || username.length() <= 3) {
            tvUsername.setError("x");
            return;
        }
        if (password == null || password.length() <= 3) {
            tvPassword.setError("x");
            return;
        }
        if (cachedParams != null) {
            TextView tvCaptcha = (TextView) captchaContainer
                    .findViewById(R.id.login_captcha);
            final CharSequence captchaText = tvCaptcha.getText();
            if (TextUtils.isEmpty(captchaText)) {
                tvCaptcha.setError("x");
                return;
            }
            cachedParams.put("verifycode", captchaText.toString());
        }
        btnLogin.setClickable(false);
        LoginTask task = new LoginTask(cachedParams, this);
        task.execute(username.toString(), password.toString());

    }

    public void savebaduSessions(String bduss) {
        App.SESSION = bduss;
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.edit().putString("session", bduss).commit();

    }

    @Override
    public void onSuccess(String result) {
        savebaduSessions(result);
        tvPhilosophy.setText("成功");

    }

    @Override
    public void onProgress(String msg) {
        tvPhilosophy.setText(msg);

    }

    
    @Override
    public void onVcodeIsneeded(Map<String, String> params, Bitmap captcha) {
        tvPhilosophy.setText("出现了验证码，请输入验证码");
        captchaContainer.setVisibility(View.VISIBLE);
        ImageView iv = (ImageView) captchaContainer
                .findViewById(R.id.login_captcha_image);
        iv.setImageBitmap(captcha);
        // toast(params.get("vcodestr"));

    }

    @Override
    public void onFailed(Map<String, String> params, String msg) {
        if (TextUtils.isEmpty(msg)) {
            tvPhilosophy.setText("出现未知错误" );
        } else {
            tvPhilosophy.setText("出现错误:" + msg);
        }
        if (params != null) {
            this.cachedParams = params;
        }
    }

    @Override
    public void onFinish() {
        btnLogin.setClickable(true);

    }

}
