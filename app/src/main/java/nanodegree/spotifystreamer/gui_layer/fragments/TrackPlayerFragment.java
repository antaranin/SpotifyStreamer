package nanodegree.spotifystreamer.gui_layer.fragments;


import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import icepick.Icepick;
import icepick.Icicle;
import lombok.NonNull;
import nanodegree.spotifystreamer.R;
import nanodegree.spotifystreamer.gui_layer.listener_adapters.OnSeekBarChangeAdapter;
import nanodegree.spotifystreamer.model_layer.Artist;
import nanodegree.spotifystreamer.model_layer.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrackPlayerFragment extends DialogFragment
{
    private final static int SECOND_LENGTH = 1000;
    @Icicle
    Track currentTrack;
    @Icicle
    Artist artist;

    @Icicle
    boolean isPlaying = true;
    boolean isLoaded;

    @Icicle
    boolean isSoleFragment;

    @Icicle
    Bitmap albumCoverImage;

    @Icicle
    int currentTime;

    @InjectView(R.id.play_pause_btn_tpf)
    ImageView playPauseBtn;
    @InjectView(R.id.song_progress_sb_tpf)
    SeekBar songProgressBar;
    @InjectView(R.id.album_iv_tpf)
    ImageView albumCoverView;
    @InjectView(R.id.current_time_tv_tpf)
    TextView durationTv;
    @InjectView(R.id.total_time_tv_tpf)
    TextView totalTimeTv;
    private MediaPlayer player;
    private OnTrackRequestedListener listener;

    private ProgressBarRunnable progressRunnable;
    private Handler progressHandler;

    public TrackPlayerFragment()
    {
        // Required empty public constructor
    }

    public static TrackPlayerFragment createInstance(boolean isSoleFragment)
    {
        TrackPlayerFragment fragment = new TrackPlayerFragment();
        fragment.isSoleFragment = isSoleFragment;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        progressHandler = new Handler();
        progressRunnable = new ProgressBarRunnable();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_track_player, container, false);
        ButterKnife.inject(this, root);
        if (savedInstanceState != null)
            restoreInstance(savedInstanceState);

        if (getShowsDialog())
        {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            View backgroundView = ButterKnife.findById(root, R.id.background_layout_tpf);
            backgroundView.setBackgroundResource(R.drawable.note_background);
        }
        log("Saved instance is not null => " + (savedInstanceState != null));
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void restoreInstance(Bundle savedInstanceState)
    {
        Icepick.restoreInstanceState(this, savedInstanceState);
        log("Current time after restoring => " + currentTime);
        log("Current Track => " + currentTrack);
        log("Album cover is not null => " + (albumCoverImage != null));

        if (currentTrack != null)
        {
            if (albumCoverImage == null)
                albumCoverView.setImageResource(R.drawable.no_image_available);
            else
                albumCoverView.setImageBitmap(albumCoverImage);
        }
        if (isPlaying)
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);

    }


    @Override
    public void onResume()
    {
        super.onResume();
        log("Current time in on resume => " + currentTime);
        isLoaded = false;
        player = new MediaPlayer();
        player.setOnPreparedListener(mediaPlayer -> onTrackReadyToPlay());
        player.setOnCompletionListener(mediaPlayer -> playbackCompleted());
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if (currentTrack != null)
            resetTrack();

        songProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeAdapter()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                if (b)
                {
                    log("Setting time in user made change => " + i);
                    currentTime = i;
                    player.seekTo(i);
                    durationTv.setText(formatIntoTimeString(currentTime));
                }
            }
        });

        if (isPlaying)
        {
            progressHandler.post(progressRunnable);
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
        }

        if (currentTrack != null)
        {
            TextView songTitleTv = ButterKnife.findById(getView(), R.id.top_edge_tpf);
            songTitleTv.setText(currentTrack.getName());
        }

        if (isSoleFragment)
        {
            TextView titleActionBar = ButterKnife.findById(getActivity(), R.id.title_tv_action_bar);
            if (currentTrack != null)
            {
                String title = "";
                if (artist != null)
                    title = artist.getName() + " - ";

                title += currentTrack.getAlbum();
                titleActionBar.setText(title);
            }

            ImageView backBtn = ButterKnife.findById(getActivity(), R.id.back_btn_action_bar);
            backBtn.setVisibility(View.VISIBLE);
            backBtn.setOnClickListener(view -> getActivity().onBackPressed());
        }

        if(getShowsDialog())
        {
            int width = getResources().getDimensionPixelSize(R.dimen.player_width);
            int height = getResources().getDimensionPixelSize(R.dimen.player_height);
            getDialog().getWindow().setLayout(width, height);
        }
    }

    private void playbackCompleted()
    {
        onNextSongPressed();
    }

    @OnClick(R.id.play_pause_btn_tpf)
    void onPlayPausePressed()
    {
        isPlaying = !isPlaying;
        if (isPlaying)
        {
            playPauseBtn.setImageResource(android.R.drawable.ic_media_pause);
            if (isLoaded)
            {
                player.start();
                progressHandler.post(progressRunnable);
            }
        }
        else if (isLoaded)
        {
            playPauseBtn.setImageResource(android.R.drawable.ic_media_play);
            player.pause();
            progressHandler.removeCallbacks(progressRunnable);
        }
    }

    @OnClick(R.id.next_song_btn_tpf)
    void onNextSongPressed()
    {
        if (listener != null)
            listener.requestNextSong();
    }

    @OnClick(R.id.previous_song_btn_tpf)
    void onPreviousSongPressed()
    {
        if (listener != null)
            listener.requestPreviousSong();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        currentTime = player.getCurrentPosition();
        player.release();
        player = null;
        songProgressBar.setOnSeekBarChangeListener(null);
        progressHandler.removeCallbacks(progressRunnable);
    }

    private void onTrackReadyToPlay()
    {
        log("current time in track ready to play => " + currentTime);
        isLoaded = true;
        player.seekTo(currentTime);
        prepareSeekBar();
        if (isPlaying)
            player.start();
    }

    @DebugLog
    public void setArtist(@NonNull Artist artist)
    {
        this.artist = artist;
        if (currentTrack != null && isSoleFragment)
        {
            if (getActivity() != null)
            {
                TextView titleActionBar = ButterKnife.findById(getActivity(), R.id.title_tv_action_bar);
                titleActionBar.setText(artist.getName() + " - " + currentTrack.getAlbum());
            }
        }
    }

    @DebugLog
    public void setCurrentTrack(@NonNull Track track)
    {
        this.currentTrack = track;
        currentTime = 0;
        loadAlbumImage();
        if (player == null)
            return;

        if (isLoaded)
        {
            player.stop();
            isLoaded = false;
        }
        if (isSoleFragment)
        {
            String title = "";
            if (artist != null)
                title = artist.getName() + " - ";

            title += track.getAlbum();

            TextView titleActionBar = ButterKnife.findById(getActivity(), R.id.title_tv_action_bar);
            titleActionBar.setText(title);
        }
        TextView songTitleTv = ButterKnife.findById(getView(), R.id.top_edge_tpf);
        songTitleTv.setText(currentTrack.getName());
        resetTrack();
    }

    @DebugLog
    private void loadAlbumImage()
    {
        new AsyncTask<String, Void, Bitmap>()
        {
            @Override
            protected Bitmap doInBackground(String... strings)
            {
                if (strings.length == 0 || strings[0] == null)
                    return null;

                try
                {
                    InputStream stream = (InputStream) new URL(strings[0]).getContent();
                    return BitmapFactory.decodeStream(stream);
                }
                catch (IOException e)
                {
                    log("Exception while decoding album image url => " + e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap)
            {
                albumCoverImage = bitmap;

                if (bitmap != null)
                    albumCoverView.setImageBitmap(bitmap);
                else
                    albumCoverView.setImageResource(R.drawable.no_image_available);
            }
        }.execute(currentTrack.getImageUrl());
    }

    private void prepareSeekBar()
    {
        int duration = player.getDuration();
        songProgressBar.setMax(duration);
        songProgressBar.setProgress(currentTime);
        totalTimeTv.setText(formatIntoTimeString(duration));
    }

    @DebugLog
    private void resetTrack()
    {
        try
        {
            player.reset();
            player.setDataSource(currentTrack.getPreviewUrl());
            player.prepareAsync();
        }
        catch (IOException e)
        {
            log("Exception while reseting track => " + e);
        }

    }

    private void setCurrentDuration()
    {
    }

    private String formatIntoTimeString(int miliseconds)
    {
        int seconds = miliseconds / SECOND_LENGTH;
        int minutePart = seconds / 60;
        int secondPart = seconds % 60;
        String secondPartString = secondPart >= 10 ? String.valueOf(secondPart) : "0" + secondPart;
        return String.valueOf(minutePart) + ":" + secondPartString;
    }

    private void log(String logMessage)
    {
        Log.d(TrackPlayerFragment.class.getSimpleName(), logMessage);
    }

    public void setOnTrackRequestedListener(OnTrackRequestedListener listener)
    {
        this.listener = listener;
    }

    public interface OnTrackRequestedListener
    {
        void requestNextSong();

        void requestPreviousSong();
    }

    private class ProgressBarRunnable implements Runnable
    {

        @Override
        public void run()
        {
            progressHandler.postDelayed(this, SECOND_LENGTH);
            if (!player.isPlaying())
                return;

            //            log("Current time before setting in runnable => " + currentTime);
            currentTime = player.getCurrentPosition();
            durationTv.setText(formatIntoTimeString(currentTime));
            songProgressBar.setProgress(currentTime);
            //            log("Current time after setting in runnable => " + currentTime);
        }
    }
}
