package nanodegree.spotifystreamer.web_service_layer.custom_apis;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hugo.weaving.DebugLog;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Pager;
import lombok.NonNull;
import lombok.Setter;
import nanodegree.spotifystreamer.data_processing_layer.AppTools;
import nanodegree.spotifystreamer.model_layer.Track;
import nanodegree.spotifystreamer.web_service_layer.response_listeners.OnTrackResponseListener;


/**
 * Created by Arin on 27/06/15.
 */
public class TrackApi
{
    private SpotifyService spotifyService;
    @Setter
    private OnTrackResponseListener trackListener;
    private AsyncTask<String, Void, ArrayList<Track>> trackTask;

    public TrackApi()
    {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyService = spotifyApi.getService();
    }

    public void sendSearchTrackRequest(@NonNull String artistName)
    {
        if (trackListener != null)
        {
            if (trackTask != null && trackTask.getStatus() == AsyncTask.Status.RUNNING)
                trackTask.cancel(false);

            trackTask = new TrackTask().execute(artistName);
        }
    }

    @DebugLog
    private ArrayList<Track> sendTrackRequest(String artistName)
    {
        try
        {
            Map<String, Object> map = new HashMap<>();
            map.put("limit", "10");
            Pager<kaaes.spotify.webapi.android.models.Track> tracks = spotifyService.searchTracks(artistName, map).tracks;

            List<kaaes.spotify.webapi.android.models.Track> items =
                    tracks.items;
            return AppTools.translateToPocosTracks(items);
        }
        catch (Exception e)
        {
            log("Exception while sending track request => " + e);
        }
        return null;
    }

    private void log(String logMessage)
    {
        Log.d("ArtistApi", logMessage);
    }

    private class TrackTask extends AsyncTask<String, Void, ArrayList<Track>>
    {
        @Override
        protected ArrayList<Track> doInBackground(String... strings)
        {
            return sendTrackRequest(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Track> tracks)
        {
            if (tracks != null)
                trackListener.onTracksRetrieved(tracks);
            else
                trackListener.onTrackSearchFailure();
        }
    }
}
