package nanodegree.spotifystreamer.model_layer;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Arin on 24/06/15.
 */
@Data
public class Artist implements Parcelable
{
    private String id;
    private String name;
    private String imageUrl;

    public final static Parcelable.Creator<Artist> CREATOR = new ClassLoaderCreator<Artist>()
    {

        @Override
        public Artist createFromParcel(Parcel parcel, ClassLoader classLoader)
        {
            return new Artist(parcel);
        }

        @Override
        public Artist createFromParcel(Parcel parcel)
        {
            return new Artist(parcel);
        }

        @Override
        public Artist[] newArray(int i)
        {
            return new Artist[i];
        }
    };

    public Artist()
    {
    }

    @Builder
    public Artist(String id, String name, String imageUrl)
    {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    private Artist(Parcel parcel)
    {
        id = parcel.readString();
        name = parcel.readString();
        imageUrl = parcel.readString();
    }

    protected boolean canEqual(Object other)
    {
        return other instanceof Artist;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(imageUrl);
    }
}
