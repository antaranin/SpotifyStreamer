package nanodegree.spotifystreamer.gui_layer.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import icepick.Icepick;
import icepick.Icicle;
import lombok.Setter;
import nanodegree.spotifystreamer.R;
import nanodegree.spotifystreamer.gui_layer.custom_tools.ArtistDisplayingAdapter;
import nanodegree.spotifystreamer.gui_layer.listener_adapters.TextWatcherAdapter;
import nanodegree.spotifystreamer.model_layer.Artist;
import nanodegree.spotifystreamer.web_service_layer.custom_apis.ArtistApi;
import nanodegree.spotifystreamer.web_service_layer.response_listeners.OnArtistResponseListener;


/**
 * A placeholder fragment containing a simple view.
 */
public class SearchArtistFragment extends Fragment implements OnArtistResponseListener
{
    private static final String ARTISTS_KEY = "artists";
    @InjectView(R.id.artist_lv_saf)
    ListView artistListView;
    @InjectView(R.id.artist_name_et)
    EditText artistNameEditText;
    @InjectView(R.id.no_artists_found_tv)
    TextView noArtistFoundTv;
    private ArtistApi artistApi;
    private ArtistDisplayingAdapter adapter;
    @Setter
    private OnArtistFragmentInteractionListener listener;
    private boolean ignoreNextTextChange;

    @Icicle
    String artistName;

    public SearchArtistFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (adapter != null && artistListView != null)
        {
            resetAdapter();
            if(adapter.getData().size() > 0)
            {
                noArtistFoundTv.setVisibility(View.INVISIBLE);
                artistListView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void resetAdapter()
    {
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(adapter);
        animationAdapter.setAbsListView(artistListView);
        artistListView.setAdapter(animationAdapter);
        artistListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (listener != null)
                listener.onArtistSelected((Artist) adapter.getItem(i));
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        artistListView.setOnItemClickListener(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_search_artist, container, false);
        initialize(root);
        if (savedInstanceState != null)
            restoreInstance(savedInstanceState);
        postInitialize();

        return root;
    }

    private void initialize(View container)
    {
        artistApi = new ArtistApi();
        artistApi.setArtistListener(this);
        ButterKnife.inject(this, container);
    }

    private void postInitialize()
    {
        artistNameEditText.addTextChangedListener(new TextWatcherAdapter()
        {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if (ignoreNextTextChange)
                {
                    ignoreNextTextChange = false;
                    return;
                }
                artistName = artistNameEditText.getText().toString();
                if (artistName.length() > 0)
                    artistApi.sendSearchArtistRequest(artistName);
                else if(adapter != null)
                    adapter.clearData();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        log("Saving instance");
        Icepick.saveInstanceState(this, outState);
        super.onSaveInstanceState(outState);
        if (adapter != null)
            outState.putParcelableArrayList(ARTISTS_KEY, adapter.getData());
    }

    private void restoreInstance(Bundle savedInstanceState)
    {
        Icepick.restoreInstanceState(this, savedInstanceState);
        ignoreNextTextChange = true;
        artistNameEditText.setText(artistName);
        ArrayList<Artist> data = savedInstanceState.getParcelableArrayList(ARTISTS_KEY);
        if (data != null)
        {
            adapter = new ArtistDisplayingAdapter(getActivity(), data,
                    R.layout.artist_list_row, R.id.artist_name_tv, R.id.artist_picture_iv);
            resetAdapter();
        }
    }

    @Override
    public void onArtistSearchFailure()
    {
        artistListView.setVisibility(View.INVISIBLE);
        noArtistFoundTv.setVisibility(View.VISIBLE);
        noArtistFoundTv.setText(R.string.server_error);
    }

    @Override
    public void onArtistsRetrieved(ArrayList<Artist> artistsRetrieved)
    {
        log("Artists retrieved");
        if (adapter == null)
        {
            log("Creating adapter");
            adapter = new ArtistDisplayingAdapter(getActivity(), artistsRetrieved,
                    R.layout.artist_list_row, R.id.artist_name_tv, R.id.artist_picture_iv);
            resetAdapter();
        }
        else
        {
            adapter.setData(artistsRetrieved);
        }

        if(artistsRetrieved.size() == 0)
        {
            artistListView.setVisibility(View.INVISIBLE);
            noArtistFoundTv.setVisibility(View.VISIBLE);
            if(artistName == null || artistName.length() == 0)
                noArtistFoundTv.setText(R.string.enter_artist_name);
            else
                noArtistFoundTv.setText(R.string.no_artist_found);
        }
        else if(artistListView.getVisibility() == View.INVISIBLE)
        {
            artistListView.setVisibility(View.VISIBLE);
            noArtistFoundTv.setVisibility(View.INVISIBLE);
        }
    }

    private void log(String logMessage)
    {
        Log.d("SearchArtistFragment", logMessage);
    }

    public interface OnArtistFragmentInteractionListener
    {
        void onArtistSelected(Artist artist);
    }
}
