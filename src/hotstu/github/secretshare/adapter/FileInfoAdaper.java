package hotstu.github.secretshare.adapter;

import hotstu.github.secretshare.R;
import hotstu.github.secretshare.bdapi.Entity;
import hotstu.github.secretshare.utils.FileUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FileInfoAdaper extends ModenArrayAdapter<Entity> {
    private int mResouceId;
    private LayoutInflater mInflater;
    private Bitmap iconFolder;
    private Bitmap iconUnknown;
    private Bitmap iconZip;

    public FileInfoAdaper(Context context, int resource) {
        super(context, resource);
        this.mResouceId = resource;
        this.mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.iconFolder = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.icon_folder);
        this.iconUnknown = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.icon_file);
        this.iconZip = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.icon_zip);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResouceId);
    }

    private View createViewFromResource(int position, View convertView,
            ViewGroup parent, int resource) {
        View view;
        ViewHolder holder;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_item_icon_text, parent,
                    false);
            holder = new ViewHolder();
            // holder.position = position;
            holder.icon = (ImageView) view.findViewById(R.id.thumbnail);
            holder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        // icon -------------title-------

        Entity item = getItem(position);
        if (item.isIsdir()) {
            holder.icon.setImageBitmap(iconFolder);

        } else if (FileUtil.isZipfile(item.getFilename())) {
            // 7z tar is unsupported by baidu or is really rare be seen
            holder.icon.setImageBitmap(iconZip);
        } else {
            holder.icon.setImageBitmap(iconUnknown);
        }

        holder.title.setText(item.getFilename());
        return view;
    }

    private static class ViewHolder {
        // public int position;
        public ImageView icon;
        public TextView title;
    }

}
