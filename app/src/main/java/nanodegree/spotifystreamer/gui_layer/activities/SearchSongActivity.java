package nanodegree.spotifystreamer.gui_layer.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import icepick.Icepick;
import icepick.Icicle;
import nanodegree.spotifystreamer.R;
import nanodegree.spotifystreamer.data_processing_layer.AppTools;
import nanodegree.spotifystreamer.gui_layer.fragments.SearchArtistFragment;
import nanodegree.spotifystreamer.gui_layer.fragments.TopTracksFragment;
import nanodegree.spotifystreamer.gui_layer.fragments.TrackPlayerFragment;
import nanodegree.spotifystreamer.model_layer.Artist;
import nanodegree.spotifystreamer.model_layer.Track;


public class SearchSongActivity extends Activity
        implements SearchArtistFragment.OnArtistFragmentInteractionListener, TrackPlayerFragment.OnTrackRequestedListener,
        TopTracksFragment.OnTrackFragmentInteractionListener
{
    private static final String ARTIST_FRAGMENT_KEY = "artistFragment";
    private static final String TRACKS_FRAGMENT_KEY = "tracksFragment";
    private static final String PLAYER_FRAGMENT_KEY = "playerFragment";

    @Optional
    @InjectView(R.id.top_tracks_fragment_container_ssa)
    FrameLayout topTracksFragmentContainer;
    @Icicle
    boolean artistPicked;
    private SearchArtistFragment artistFragment;
    private TopTracksFragment tracksFragment;
    private TrackPlayerFragment playerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_song);
        AppTools.setOptimalImageHeight(getResources().getDimension(R.dimen.list_view_pic_height));
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.action_bar,
                null);

        // Set up your ActionBar
        final ActionBar actionBar = getActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setCustomView(actionBarLayout);
        }
        ButterKnife.inject(this);

        log("Restoring instance => " + (savedInstanceState != null));
        if (savedInstanceState == null)
            createNewInstance();
        else
            restoreInstance(savedInstanceState);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        log("On resume");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        log("On pause");
    }

    private void createNewInstance()
    {
        log("Creating new instance");
        artistFragment = (SearchArtistFragment) getFragmentManager().findFragmentById(R.id.search_artist_fragment_ssa);
        if (artistFragment == null)
        {
            artistFragment = new SearchArtistFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_container_ssa, artistFragment)
                    .commit();

            tracksFragment = TopTracksFragment.createInstance(true);
        }
        else
        {
            tracksFragment = (TopTracksFragment) getFragmentManager().findFragmentById(R.id.top_tracks_fragment_ssa);
        }
        artistFragment.setListener(this);
        artistFragment.setRetainInstance(true);
        playerFragment = new TrackPlayerFragment();
        playerFragment.setOnTrackRequestedListener(this);
        tracksFragment.setListener(this);
        tracksFragment.setRetainInstance(true);

    }

    private void restoreInstance(Bundle savedInstanceState)
    {
        Icepick.restoreInstanceState(this, savedInstanceState);
        artistFragment = (SearchArtistFragment) getFragmentManager().getFragment(savedInstanceState, ARTIST_FRAGMENT_KEY);
        artistFragment.setListener(this);
        Fragment temp = getFragmentManager().getFragment(savedInstanceState, TRACKS_FRAGMENT_KEY);
        log("Tracks fragment was saved => " + (temp != null));
        if (temp == null)
            tracksFragment = TopTracksFragment.createInstance(true);
        else
            tracksFragment = (TopTracksFragment) temp;

        tracksFragment.setListener(this);

        temp = getFragmentManager().getFragment(savedInstanceState, PLAYER_FRAGMENT_KEY);
        log("player fragment was saved => " + (temp != null));
        if (temp == null)
            playerFragment = new TrackPlayerFragment();
        else
            playerFragment = (TrackPlayerFragment) temp;

        playerFragment.setOnTrackRequestedListener(this);

        if (artistPicked && topTracksFragmentContainer != null)
            topTracksFragmentContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
        getFragmentManager().putFragment(outState, ARTIST_FRAGMENT_KEY, artistFragment);
        if (topTracksFragmentContainer != null || !artistFragment.isAdded())//means that top tracks fragment is somewhere in the backstack
            getFragmentManager().putFragment(outState, TRACKS_FRAGMENT_KEY, tracksFragment);

        log("player fragment is visible => " + playerFragment.isVisible());
        if (playerFragment.isVisible())
            getFragmentManager().putFragment(outState, PLAYER_FRAGMENT_KEY, playerFragment);
    }

    private void log(String logMessage)
    {
        Log.d("SearchSongActivity", logMessage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_song, menu);
        return true;
    }

    @Override
    public void onArtistSelected(Artist artist)
    {
        log("Artist selected");
        if (!tracksFragment.isAdded())
            changeCurrentFragment(tracksFragment);
        if (!artistPicked && topTracksFragmentContainer != null)
        {
            topTracksFragmentContainer.setVisibility(View.VISIBLE);
            artistPicked = true;
        }
        tracksFragment.setArtist(artist);
    }

    private void changeCurrentFragment(Fragment fragment)
    {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_ssa, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void requestNextSong()
    {
        tracksFragment.selectNextTrack();
    }

    @Override
    public void requestPreviousSong()
    {
        tracksFragment.selectPreviousTrack();
    }

    @Override
    public void onTrackSelected(Track track)
    {
        if (!playerFragment.isVisible())
        {
            if (topTracksFragmentContainer == null) //means we are on a phone
                changeCurrentFragment(playerFragment);
            else
                playerFragment.show(getFragmentManager(), null);
        }

        playerFragment.setCurrentTrack(track);

    }
}
