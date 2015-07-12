package nanodegree.spotifystreamer.web_service_layer.response_listeners;

import java.util.ArrayList;

import nanodegree.spotifystreamer.model_layer.Track;

/**
 * Created by Arin on 27/06/15.
 */
public interface OnTrackResponseListener
{
    void onTrackSearchFailure();

    void onTracksRetrieved(ArrayList<Track> tracksRetrieved);
}
