package hotstu.github.secretshare;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;

import com.umeng.analytics.MobclickAgent;

import hotstu.github.secretshare.bdapi.Entity;
import hotstu.github.secretshare.bdapi.RapidUpTask;
import hotstu.github.secretshare.bdapi.RapidUpTask.OnProcessListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CodeFragment extends Fragment {
    private Entity e;
    private LinearLayout bframe;
    private LinearLayout aframe;
    private TextView tvInfo;
    private Bitmap mBitmap;

    public CodeFragment(Entity e) {
        super();
        this.e = e;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("CodeFragment"); //统计页面
    }
    
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("CodeFragment"); 
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_code, container, false);
        bframe = (LinearLayout) v
                .findViewById(R.id.frame_before_code_generated);
        aframe = (LinearLayout) v.findViewById(R.id.frame_after_code_generated);
        tvInfo = (TextView) bframe.findViewById(R.id.txt_code_get_info);

        tvInfo.setText("...");
        return v;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        new RapidUpTask(getActivity(), new OnProcessListener() {

            @Override
            public void onSucess(Bitmap img) {

                if (img != null) {
                    ImageView ivCode = (ImageView) aframe
                            .findViewById(R.id.image_qrcode);
                    ivCode.setImageBitmap(img);
                    if (mBitmap != null && !mBitmap.isRecycled()) {
                        mBitmap.recycle();
                    }
                    mBitmap = img;
                }

            }

            @Override
            public void onStart(String msg) {
                bframe.setVisibility(View.VISIBLE);
                aframe.setVisibility(View.GONE);
                tvInfo.setText(msg);

            }

            @Override
            public void onProgress(String msg) {
                tvInfo.setText(msg);

            }

            @Override
            public void onFinish() {
                bframe.setVisibility(View.GONE);
                aframe.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailed(String msg) {
                tvInfo.setText(msg);

            }
        }).execute(e);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.actionbar_share, menu);

    }
    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_share) {
            String dst = Environment.getExternalStorageDirectory()
                    .getAbsolutePath()
                    + "/secretshare/"
                    +e.getFilename()
                    +".png";
            try {
                OutputStream os = FileUtils.openOutputStream(new File(dst));
                mBitmap.compress(CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
            } catch (IOException e1) {
                Toast.makeText(getActivity(), "write file error", Toast.LENGTH_LONG).show();
                e1.printStackTrace();
            }
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND)
                    .setType("image/*")
                    .putExtra(android.content.Intent.EXTRA_SUBJECT,
                            e.getFilename())
                    .putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(dst)));
            startActivity(intent);
            return true;
        }
        return false;
    }
    
    

   
}
