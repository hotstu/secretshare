package hotstu.github.secretshare;

import hotstu.github.secretshare.bdapi.LoginTask;

import java.util.Map;

import com.umeng.analytics.MobclickAgent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LoginActivity extends ActionBarActivity implements OnClickListener {

    TextView tvUsername;
    TextView tvPassword;
    TextView tvPhilosophy;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvUsername = (TextView) findViewById(R.id.login_username);
        tvPassword = (TextView) findViewById(R.id.login_password);
        tvPhilosophy = (TextView) findViewById(R.id.login_philosophy);
        btnLogin = (Button) findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd("LoginActivity"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart("LoginActivity"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }
    
    @Override
    public void onClick(View v) {
        final CharSequence username = tvUsername.getText();
        final CharSequence password = tvPassword.getText();
        if (username != null && password != null && password.length() >= 3
                && username.length() >= 3) {
            btnLogin.setClickable(false);
            LoginTask task = new LoginTask(new LoginTask.OnLoginListener() {

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
                public void onFailed() {
                    tvPhilosophy.setText("出现了来历不明的错误");
                    // toast("onFailed");

                }

                @Override
                public void onVcodeIsneeded(Map<String, String> params) {
                    tvPhilosophy.setText("出现了验证码，目前不支持");
                    // toast(params.get("vcodestr"));

                }

                @Override
                public void onFailed(String msg) {
                    tvPhilosophy.setText("出现错误:" + msg);
                    // toast(msg);

                }

                @Override
                public void onFinish() {
                    btnLogin.setClickable(true);

                }
            });
            task.execute(username.toString(), password.toString());
        }

    }

    public  void savebaduSessions(String bduss) {
        App.SESSION = bduss;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("session", bduss).commit();

    }

}
