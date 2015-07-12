package nanodegree.spotifystreamer.web_service_layer.response_listeners;

import java.util.ArrayList;

import nanodegree.spotifystreamer.model_layer.Artist;

/**
 * Created by Arin on 26/06/15.
 */
public interface OnArtistResponseListener
{
    void onArtistSearchFailure();

    void onArtistsRetrieved(ArrayList<Artist> artistsRetrieved);
}
