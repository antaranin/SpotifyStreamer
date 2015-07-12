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
import nanodegree.spotifystreamer.model_layer.Artist;

/**
 * Created by Arin on 26/06/15.
 */
public class ArtistDisplayingAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;
    private Context context;
    private final int layoutId;
    private final int textViewId;
    private final int imageViewId;
    private ArrayList<Artist> data;

    public ArtistDisplayingAdapter(Context context, ArrayList<Artist> data, int layoutId, int textViewId, int imageViewId)
    {
        this.context = context;
        this.layoutId = layoutId;
        this.textViewId = textViewId;
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
            holder.text = ButterKnife.findById(view, textViewId);
            holder.image = ButterKnife.findById(view, imageViewId);
            view.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) view.getTag();
        }

        Artist artist = data.get(i);
        if(artist == null || artist.getName() == null)
            throw new IllegalArgumentException("Artist in adapter was incorrect. Artist => " + artist);
        String imageUrl = artist.getImageUrl();
        if(imageUrl != null && imageUrl.length() > 0)
            Picasso.with(context).load(imageUrl)
                    .centerInside()
                    .resizeDimen(R.dimen.list_view_pic_width, R.dimen.list_view_pic_height)
                    .into(holder.image);
        else
            Picasso.with(context)
                    .load(R.drawable.no_image_available)
                    .centerInside()
                    .resizeDimen(R.dimen.list_view_pic_width, R.dimen.list_view_pic_height)
                    .into(holder.image);

        holder.text.setText(artist.getName());
        return view;
    }

    public void setData(ArrayList<Artist> data)
    {
        this.data = data;
        this.notifyDataSetChanged();
    }

    public ArrayList<Artist> getData()
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
        public TextView text;
        public ImageView image;

    }
}
