package hotstu.github.secretshare;


import hotstu.github.secretshare.bdapi.RapidDownTask;
import hotstu.github.secretshare.bdapi.RapidDownTask.OnProcessListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends ActionBarActivity implements OnClickListener {
    private TextView btnLogin;
    private TextView btnFileMgr;
    private TextView btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = (TextView) findViewById(R.id.btn_login);
        btnFileMgr = (TextView) findViewById(R.id.btn_file_manage);
        btnScan = (TextView) findViewById(R.id.btn_saoyisao);

        String textLogin;
        if (App.SESSION == null) {
            textLogin = "未登录";
        } else if (App.USER_DISPLAY_NAME == null) {
            textLogin = "未知用户 - 已登陆";
        } else {
            textLogin = App.USER_DISPLAY_NAME + " - 已登陆";
        }
        btnLogin.setText(textLogin);
        btnFileMgr.setText("浏览度盘");
        btnScan.setText("扫一扫");

        btnLogin.setOnClickListener(this);
        btnFileMgr.setOnClickListener(this);
        btnScan.setOnClickListener(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd("MainActivity"); // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息 
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart("MainActivity"); //统计页面
        MobclickAgent.onResume(this);          //统计时长
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnLogin.getId()) {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            return;
        }
        if (v.getId() == btnFileMgr.getId()) {
            Intent i = new Intent(this, FileViewerActivity.class);
            startActivity(i);
            return;
        }
        if (v.getId() == btnScan.getId()) {
            if (App.SESSION == null) {
                Toast.makeText(this, "需要先登陆", Toast.LENGTH_LONG).show();
            } else {
                qrscan();
            }
            return;
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, intent);
        if (scanResult != null) {
            // handle scan result
            if (scanResult.getContents() == null
                    || scanResult.getContents().equals("")) {
                Toast.makeText(this, "bad input", Toast.LENGTH_LONG).show();
            } else {
                String data = scanResult.getContents();
                Log.d("scaned data: ", "" + data);
                new RapidDownTask(new OnProcessListener() {
                    
                    @Override
                    public void onSucess(String msg) {
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        
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
                    public void onFailed() {
                        Toast.makeText(MainActivity.this, "失败咯", Toast.LENGTH_LONG).show();
                        
                    }
                }).execute(data);
                
            }

        }
        // else continue with any other code you need in the method
    }
    
    private void qrscan() {
        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();

    }

}
