package nanodegree.spotifystreamer.data_processing_layer;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Image;
import nanodegree.spotifystreamer.model_layer.Artist;
import nanodegree.spotifystreamer.model_layer.Track;
import nanodegree.spotifystreamer.web_service_layer.model.ImageObject;

/**
 * Created by Arin on 27/06/15.
 */
public class AppTools
{
    private static float optimalImageHeight;

    public static ImageObject pickOptimalObject(List<ImageObject> images)
    {
        float minDiff = -1;
        ImageObject bestObject = null;
        for (ImageObject image : images)
        {
            if (bestObject == null)
            {
                minDiff = getDifferenceFromOptimal(image, optimalImageHeight);
                bestObject = image;
            }
            else
            {
                float currentDiff = getDifferenceFromOptimal(image, optimalImageHeight);
                if (image.getHeight() < image.getWidth())
                {
                    if (currentDiff < minDiff && currentDiff > 0)
                    {
                        minDiff = currentDiff;
                        bestObject = image;
                    }
                    else if (minDiff < 0 && currentDiff > minDiff)
                    {
                        minDiff = currentDiff;
                        bestObject = image;
                    }
                }
            }
        }
        return bestObject;
    }

    private static float getDifferenceFromOptimal(ImageObject image, float optimal)
    {
        return optimal - image.getHeight();
    }

    public static void setOptimalImageHeight(float optimalImageHeight)
    {
        AppTools.optimalImageHeight = optimalImageHeight;
    }

    public static ArrayList<Track> translateToPocosTracks(List<kaaes.spotify.webapi.android.models.Track> webTrackObjects)
    {
        ArrayList<Track> tracks = new ArrayList<>();
        for (kaaes.spotify.webapi.android.models.Track webTrackObject : webTrackObjects)
        {
            tracks.add(translateToPoco(webTrackObject));
        }
        return tracks;
    }

    public static Track translateToPoco(kaaes.spotify.webapi.android.models.Track webTrackObject)
    {
        AlbumSimple webAlbumObject = webTrackObject.album;
        String album = webAlbumObject == null ? null : webAlbumObject.name;
        String imageUrl = null;
        if (album != null)
        {
            ImageObject image = AppTools.pickOptimalObject(translateToPocosImages(webAlbumObject.images));
            imageUrl = image == null ? null : image.getUrl();
        }
        return Track.builder()
                .album(album)
                .imageUrl(imageUrl)
                .name(webTrackObject.name)
                .build();
    }

    public static ArrayList<Artist> translateToPocosArtist(List<kaaes.spotify.webapi.android.models.Artist> webArtistObjects)
    {
        ArrayList<Artist> artists = new ArrayList<>();
        for (kaaes.spotify.webapi.android.models.Artist webArtistObject : webArtistObjects)
        {
            if (webArtistObject != null)
                artists.add(translateToPoco(webArtistObject));
        }
        return artists;
    }

    public static Artist translateToPoco(kaaes.spotify.webapi.android.models.Artist webArtistObject)
    {
        ArrayList<ImageObject> images = translateToPocosImages(webArtistObject.images);
        ImageObject image = AppTools.pickOptimalObject(images);
        String imageUrl = image == null ? null : image.getUrl();
        return Artist.builder()
                .name(webArtistObject.name)
                .id(webArtistObject.id)
                .imageUrl(imageUrl)
                .build();
    }

    public static ArrayList<ImageObject> translateToPocosImages(List<Image> webImageObjects)
    {
        ArrayList<ImageObject> images = new ArrayList<>();
        for (Image webImageObject : webImageObjects)
        {
            if (webImageObject != null)
                images.add(translateToPoco(webImageObject));
        }
        return images;
    }

    public static ImageObject translateToPoco(Image webImageObject)
    {
        return ImageObject.builder()
                .url(webImageObject.url)
                .height(webImageObject.height)
                .width(webImageObject.width)
                .build();
    }
}
