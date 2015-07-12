package nanodegree.spotifystreamer.model_layer;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Arin on 24/06/15.
 */

@Data
public class Track implements Parcelable
{
    private String name;
    private String album;
    private String imageUrl;

    public final Parcelable.Creator<Track> CREATOR = new ClassLoaderCreator<Track>()
    {
        @Override
        public Track createFromParcel(Parcel parcel)
        {
            return new Track(parcel);
        }

        @Override
        public Track[] newArray(int i)
        {
            return new Track[i];
        }

        @Override
        public Track createFromParcel(Parcel parcel, ClassLoader classLoader)
        {
            return new Track(parcel);
        }
    };

    public Track()
    {
    }

    @Builder
    public Track(String name, String album, String imageUrl)
    {
        this.name = name;
        this.album = album;
        this.imageUrl = imageUrl;
    }

    private Track(Parcel parcel)
    {
        name = parcel.readString();
        album = parcel.readString();
        imageUrl = parcel.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i)
    {
        parcel.writeString(name);
        parcel.writeString(album);
        parcel.writeString(imageUrl);
    }

}
