package hotstu.github.secretshare;

import hotstu.github.secretshare.adapter.FileInfoAdaper;
import hotstu.github.secretshare.bdapi.Entity;
import hotstu.github.secretshare.bdapi.RESTBaiduPathLoader;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import com.umeng.analytics.MobclickAgent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class FileViewerFragment extends ListFragment implements
        LoaderCallbacks<List<Entity>> {
    /** 当前路径，例如/myfolder/ **/
    private String curPath;

    private Stack<String> mPathStack;

    private FileInfoAdaper mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new FileInfoAdaper(getActivity(),
                R.layout.list_item_icon_text);
        setListAdapter(mAdapter);
        setListShown(false);
        setEmptyText("没有内容，请检查网络连接");

        if (savedInstanceState != null
                && savedInstanceState.getSerializable("stack") != null) {
            mPathStack = (Stack<String>) savedInstanceState
                    .getSerializable("stack");
            curPath = mPathStack.pop();
        } else {
            mPathStack = new Stack<String>();
            curPath = "/";
        }
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart("FileViewerFragment"); // 统计页面
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("FileViewerFragment");
    }

    /**
     * 
     * @return ture if 已经处理
     */
    public boolean onBackPressed() {
        String backpath = getBackPath();
        if (backpath == null) {
            return false;
        } else {
            setListShown(false);
            Bundle args = new Bundle();
            args.putString("dir", backpath);
            getLoaderManager().restartLoader(0, args, this);
            curPath = backpath;            
            return true;
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("stack", mPathStack);
    }

    @Override
    public Loader<List<Entity>> onCreateLoader(int id, Bundle args) {
        return new RESTBaiduPathLoader(getActivity(), args);
    }

    @Override
    public void onLoadFinished(Loader<List<Entity>> loader, List<Entity> data) {
        mAdapter.clear();
        if (data != null) {
            mAdapter.addAll(data);
        }

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Entity>> loader) {
        Log.d("PathLoaderFragment", "onLoaderReset");
        mAdapter.clear();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final Entity e = (Entity) l.getItemAtPosition(position);
        if (e.isIsdir()) {
            setListShown(false);
            
            Bundle args = new Bundle();
            args.putString("dir", e.getPath());
            getLoaderManager().restartLoader(0, args, this);

            mPathStack.push(curPath);
            curPath = e.getPath();
            return;
        }
        if (!e.isIsdir() && e.getSize() > 256 * 1024) {
            // TODO check filesize, filesize < 256*1024 bytes will not process
            new AlertDialog.Builder(getActivity())
                    .setTitle("请选择对 " + e.getFilename() + " 的操作:")
                    .setItems(new String[] { "提取分享码" }, new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((FileViewerActivity) getActivity())
                                    .startCodeFragment(e);

                        }
                    }).create().show();
        } else {
            Toast.makeText(getActivity(), "不支持小于256kb文件", Toast.LENGTH_LONG)
                    .show();
        }

    }

    private String getBackPath() {
        try {
            return mPathStack.pop();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.actionbar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            setListShown(false);
            Bundle args = new Bundle();
            args.putString("dir", curPath);
            getLoaderManager().restartLoader(0, args, this);
            return true;
        }
        return false;
    }

}
