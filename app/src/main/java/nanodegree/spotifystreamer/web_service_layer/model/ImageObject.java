package nanodegree.spotifystreamer.web_service_layer.model;

import org.parceler.Parcel;

import lombok.Builder;
import lombok.Data;

/**
 * Created by Arin on 24/06/15.
 */
@Data
@Parcel
public class ImageObject
{
    private String url;
    private Integer height;
    private Integer width;

    @Builder
    public ImageObject(String url, Integer height, Integer width)
    {
        this.url = url;
        this.height = height;
        this.width = width;
    }
}
