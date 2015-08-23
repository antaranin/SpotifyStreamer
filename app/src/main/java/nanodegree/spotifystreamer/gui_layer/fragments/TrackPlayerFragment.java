package nanodegree.spotifystreamer.gui_layer.fragments;


import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import hugo.weaving.DebugLog;
import icepick.Icepick;
import icepick.Icicle;
import nanodegree.spotifystreamer.R;
import nanodegree.spotifystreamer.gui_layer.listener_adapters.OnSeekBarChangeAdapter;
import nanodegree.spotifystreamer.model_layer.Track;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrackPlayerFragment extends DialogFragment
{
    private final static int SECOND_LENGTH = 1000;
    private final static String BITMAP_KEY = "bitmapKey";
    @Icicle
    Track currentTrack;
    @Icicle
    boolean isPlaying;
    boolean isLoaded;

    @Icicle
    boolean isSoleInstance;

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
    private MediaPlayer player;
    private OnTrackRequestedListener listener;

    private ProgressBarRunnable progressRunnable;
    private Handler progressHandler;

    public TrackPlayerFragment()
    {
        // Required empty public constructor
    }

    public static TrackPlayerFragment createInstance(boolean isSoleInstance)
    {
        TrackPlayerFragment fragment = new TrackPlayerFragment();
        fragment.isSoleInstance = isSoleInstance;
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
        if(isPlaying)
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
                if(b)
                {
                    log("Setting time in user made change => " + i);
                    currentTime = i;
                    player.seekTo(i);
                }
            }
        });

        if(isPlaying)
            progressHandler.post(progressRunnable);
    }

    private void playbackCompleted()
    {
        onNextSongPressed();
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
    public void setCurrentTrack(Track track)
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
        songProgressBar.setMax(player.getDuration());
        songProgressBar.setProgress(currentTime);
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
            songProgressBar.setProgress(currentTime);
//            log("Current time after setting in runnable => " + currentTime);
        }
    }
}
