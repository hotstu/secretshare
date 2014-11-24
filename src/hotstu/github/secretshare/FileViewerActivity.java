package hotstu.github.secretshare;


import hotstu.github.secretshare.bdapi.Entity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.umeng.analytics.MobclickAgent;

public class FileViewerActivity extends ActionBarActivity {
    private FileViewerFragment list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fileviewer);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(R.id.fragment_content) == null) {
            list = new FileViewerFragment();
            fm.beginTransaction().add(R.id.fragment_content, list).commit();
        }
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onResume(this);          //统计时长
    }
    
    @Override
    public void onBackPressed() {
        if (!list.onBackPressed())
            super.onBackPressed();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void startCodeFragment(Entity e) {
        Fragment f = new CodeFragment(e);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.fragment_content, f)
        .addToBackStack(null).commit();
        
    }
    
   
}
