package nanodegree.spotifystreamer.gui_layer.custom_tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.ButterKnife;
import nanodegree.spotifystreamer.R;
import nanodegree.spotifystreamer.model_layer.Track;

/**
 * Created by Arin on 27/06/15.
 */
public class TrackDisplayingAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;
    private Context context;
    private final int layoutId;
    private final int nameTextViewId;
    private final int albumTextViewId;
    private final int imageViewId;
    private ArrayList<Track> data;

    public TrackDisplayingAdapter(Context context, ArrayList<Track> data, int layoutId,
                                   int nameTextViewId, int albumTextViewId, int imageViewId)
    {
        this.context = context;
        this.layoutId = layoutId;
        this.nameTextViewId = nameTextViewId;
        this.albumTextViewId = albumTextViewId;
        this.imageViewId = imageViewId;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.data = data;
    }

    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public Object getItem(int i)
    {
        if(data == null || data.size() <= i)
            return null;
        return data.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup)
    {
        View view = convertView;
        ViewHolder holder;
        if(view == null)
        {
            view = inflater.inflate(layoutId, null);
            holder = new ViewHolder();
            holder.nameView = ButterKnife.findById(view, nameTextViewId);
            holder.albumView = ButterKnife.findById(view, albumTextViewId);
            holder.image = ButterKnife.findById(view, imageViewId);
            view.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) view.getTag();
        }

        Track track = data.get(i);
        if(track == null || track.getName() == null || track.getAlbum() == null)
            throw new IllegalArgumentException("Track in adapter was incorrect. Track => " + track);
        String imageUrl = track.getImageUrl();
        if(imageUrl != null && imageUrl.length() > 0)
            Picasso.with(context).load(imageUrl).into(holder.image);
        else
            Picasso.with(context).load(R.drawable.no_image_available).into(holder.image);

        holder.nameView.setText(track.getName());
        holder.albumView.setText(track.getAlbum());
        return view;
    }

    public void setData(ArrayList<Track> data)
    {
        this.data = data;
        this.notifyDataSetChanged();
    }

    public ArrayList<Track> getData()
    {
        return data;
    }

    public void clearData()
    {
        data.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder
    {
        public TextView nameView;
        public TextView albumView;
        public ImageView image;

    }
}

