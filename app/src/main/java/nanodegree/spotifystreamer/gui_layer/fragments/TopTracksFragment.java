package nanodegree.spotifystreamer.gui_layer.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import icepick.Icepick;
import icepick.Icicle;
import lombok.Setter;
import nanodegree.spotifystreamer.R;
import nanodegree.spotifystreamer.gui_layer.custom_tools.TrackDisplayingAdapter;
import nanodegree.spotifystreamer.model_layer.Artist;
import nanodegree.spotifystreamer.model_layer.Track;
import nanodegree.spotifystreamer.web_service_layer.custom_apis.TrackApi;
import nanodegree.spotifystreamer.web_service_layer.response_listeners.OnTrackResponseListener;

/**
 * A fragment representing a list of Items.
 * <p>
 * <p>
 * interface.
 */
public class TopTracksFragment extends Fragment implements OnTrackResponseListener
{

    private static final String TRACK_KEY = "tracks";
    @InjectView(R.id.top_tracks_lv_ttf)
    ListView trackListView;
    private TrackApi trackApi;
    private Artist artist;
    private TrackDisplayingAdapter adapter;
    @Setter
    private OnTrackFragmentInteractionListener listener;
    @Icicle
    boolean isSoleFragment;


    @InjectView(R.id.no_tracks_found_tv)
    TextView noTrackFoundTv;

    @Icicle
    Track currentTrack;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TopTracksFragment()
    {

    }

    public static TopTracksFragment createInstance(boolean isSoleFragment)
    {
        TopTracksFragment fragment = new TopTracksFragment();
        fragment.isSoleFragment = isSoleFragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            restoreInstance(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_top_tracks, container, false);
        initialize(root);
        return root;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (trackListView != null && adapter != null)
        {
            resetAdapter();
            if(adapter.getData().size() > 0)
            {
                noTrackFoundTv.setVisibility(View.INVISIBLE);
                trackListView.setVisibility(View.VISIBLE);
            }
        }
        if (isSoleFragment)
        {
            if (artist != null)
            {
                TextView titleActionBar = ButterKnife.findById(getActivity(), R.id.title_tv_action_bar);
                titleActionBar.setText(artist.getName());
            }
            ImageView backBtn = ButterKnife.findById(getActivity(), R.id.back_btn_action_bar);
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(view -> getActivity().onBackPressed());
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (trackListView != null)
            trackListView.setOnItemClickListener(null);
        if (isSoleFragment)
        {
            Activity activity = getActivity();
            ImageView backBtn = ButterKnife.findById(activity, R.id.back_btn_action_bar);
            backBtn.setVisibility(View.GONE);
            backBtn.setOnClickListener(null);
            TextView titleActionBar = ButterKnife.findById(activity, R.id.title_tv_action_bar);
            titleActionBar.setText(R.string.spotify_streamer);
        }
    }

    private void resetAdapter()
    {
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(trackListView);
        trackListView.setAdapter(animationAdapter);
        trackListView.setOnItemClickListener((adapterView, view, i, l) -> recoverTrack(i));
    }

    private void recoverTrack(int trackNum)
    {
        if (listener != null)
        {
            currentTrack = ((Track)adapter.getItem(trackNum));
            listener.onTrackSelected(currentTrack);
        }
    }

    private void initialize(View container)
    {
        ButterKnife.inject(this, container);
        trackApi = new TrackApi();
        trackApi.setTrackListener(this);
        if (artist != null)
            trackApi.sendSearchTrackRequest(artist.getName());
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
        if (adapter != null)
            outState.putParcelableArrayList(TRACK_KEY, adapter.getData());
    }

    @DebugLog
    private void restoreInstance(Bundle savedInstance)
    {
        Icepick.restoreInstanceState(this, savedInstance);
        ArrayList<Track> data = savedInstance.getParcelableArrayList(TRACK_KEY);
        if (data != null)
        {
            adapter = new TrackDisplayingAdapter(getActivity(), data, R.layout.track_list_row,
                    R.id.track_name_tv, R.id.track_album_tv, R.id.track_picture_iv);
//            resetAdapter();
        }
    }

    public void setArtist(Artist artist)
    {
        log("Setting artist");
        this.artist = artist;
        if (isSoleFragment && isAdded())
        {
            TextView titleActionBar = ButterKnife.findById(getActivity(), R.id.title_tv_action_bar);
            titleActionBar.setText(artist.getName());
        }
        findArtistTracks();
    }

    private void findArtistTracks()
    {
        if (trackApi != null)
            trackApi.sendSearchTrackRequest(artist.getName());
    }

    @Override
    public void onTrackSearchFailure()
    {
        trackListView.setVisibility(View.INVISIBLE);
        noTrackFoundTv.setVisibility(View.VISIBLE);
        noTrackFoundTv.setText(R.string.server_error);

    }

    @Override
    public void onTracksRetrieved(ArrayList<Track> tracksRetrieved)
    {
        if (adapter == null)
        {
            adapter = new TrackDisplayingAdapter(getActivity(), tracksRetrieved,
                    R.layout.track_list_row, R.id.track_name_tv, R.id.track_album_tv, R.id.track_picture_iv);
            resetAdapter();
        }
        else
        {
            adapter.setData(tracksRetrieved);
        }

        if(tracksRetrieved.size() == 0)
        {
            trackListView.setVisibility(View.INVISIBLE);
            noTrackFoundTv.setVisibility(View.VISIBLE);
        }
        else if(trackListView.getVisibility() == View.INVISIBLE)
        {
            trackListView.setVisibility(View.VISIBLE);
            noTrackFoundTv.setVisibility(View.INVISIBLE);
        }

    }

    private void log(String logMessage)
    {
        Log.d("TopTracksFragment", logMessage);
    }

    public void selectNextTrack()
    {
        int trackCount = adapter.getCount();
        if(listener == null || trackCount <= 0)
            return;

        if(currentTrack == null)
            recoverTrack(0);
        else
            recoverTrack((adapter.getData().indexOf(currentTrack) + 1) % trackCount);
    }

    public void selectPreviousTrack()
    {
        int trackCount = adapter.getCount();
        if(listener == null || trackCount <= 0)
            return;

        if(currentTrack == null)
            recoverTrack(adapter.getCount() - 1);
        else
            recoverTrack((adapter.getData().indexOf(currentTrack) + trackCount - 1) % trackCount); //because of java stupid modulo
    }

    public interface OnTrackFragmentInteractionListener
    {
        void onTrackSelected(Track track);
    }
}
