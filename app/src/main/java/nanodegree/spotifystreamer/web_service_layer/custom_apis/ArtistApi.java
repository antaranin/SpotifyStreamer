package nanodegree.spotifystreamer.web_service_layer.custom_apis;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import lombok.NonNull;
import lombok.Setter;
import nanodegree.spotifystreamer.data_processing_layer.AppTools;
import nanodegree.spotifystreamer.model_layer.Artist;
import nanodegree.spotifystreamer.web_service_layer.response_listeners.OnArtistResponseListener;


/**
 * Created by Arin on 24/06/15.
 */
public class ArtistApi
{
    private SpotifyService spotifyService;
    @Setter
    private OnArtistResponseListener artistListener;
    private AsyncTask<String, Void, ArrayList<Artist>> artistTask;

    public ArtistApi()
    {
        SpotifyApi spotifyApi = new SpotifyApi();
        spotifyService = spotifyApi.getService();
    }

    public void sendSearchArtistRequest(@NonNull String artistName)
    {
        if (artistListener != null)
        {
            if (artistTask != null && artistTask.getStatus() == AsyncTask.Status.RUNNING)
                artistTask.cancel(false);

            artistTask = new ArtistTask().execute(artistName);
        }
    }

    @DebugLog
    private ArrayList<Artist> sendArtistRequest(String artistName)
    {
        try
        {
            List<kaaes.spotify.webapi.android.models.Artist> items =
                    spotifyService.searchArtists(artistName).artists.items;
            return AppTools.translateToPocosArtist(items);
        }
        catch (Exception e)
        {
            log("Exception while sending artist request => " + e);
        }
        return null;
    }

    private void log(String logMessage)
    {
        Log.d("ArtistApi", logMessage);
    }

    private class ArtistTask extends AsyncTask<String, Void, ArrayList<Artist>>
    {
        @Override
        protected ArrayList<Artist> doInBackground(String... strings)
        {
            return sendArtistRequest(strings[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists)
        {
            if (artists != null)
                artistListener.onArtistsRetrieved(artists);
            else
                artistListener.onArtistSearchFailure();
        }
    }
}
