package teamawaazdtu.com.awaaz;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.andremion.music.MusicCoverView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ListenAudios extends AppCompatActivity {

    int audioPosition;
    MusicCoverView mCoverView;
    MediaPlayer mediaPlayer = null;
    int flag = 0;
    String filePath;
    ListView lv;
    String[] items;
    FetchSongs fs;
    ArrayList<File> mySongs;
    ProgressDialog dialog;
    public static final String CREATED_TIME = "created_time";
    int currentPlaying = -1;
    public static final int PLAY = 1;
    public static final int PAUSE = 0;
    boolean fileSelected = false;
    ImageButton btnPause, btnPlay;
    View previousView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listen_audios);

        currentPlaying = -1;

        bindViews();
        setDialogBox();
        displayAudios();
        setListeners();
    }

    private void setDialogBox() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait, Fetching Audio Files...");
        dialog.setCancelable(true);
        dialog.show();
    }

    private void setListeners() {

        //media player listeners ----
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                stopCover();
                updateButtons(PLAY);
                previousView.setBackgroundColor(Color.argb(0, 50, 0, 50));
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                playCover();
                updateButtons(PAUSE);
//                audioDuration = mediaPlayer.getDuration() / 1000.0;
            }
        });

        //list view items listener ----
        ArrayAdapter<String> adp= new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,items);
        lv.setAdapter(adp);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(!fileSelected){
                    fileSelected = true;
                }
                if(previousView!=null) {
                    previousView.setBackgroundColor(Color.argb(0, 50, 0, 50));
                }
                previousView = view;
                view.setBackgroundColor(Color.argb(40,50,0,50));

//                stopCover();

                currentPlaying = i;
                flag = 0;

                if (fs.getfetchstatus() != true) {
                    Log.d("TAGFetchStatus : ", "here");
                    mySongs = fs.findSongs(getExternalFilesDir(Environment.DIRECTORY_MUSIC));
                } else {
                    mySongs = fs.getsonglist();
                }
//                Intent intent = getIntent();
//                    position = intent.getIntExtra("pos", 0);
                audioPosition = i;
                audioPosition = mySongs.size()-i-1;
//                Log.d("TAG123pos", String.valueOf(audioPosition));
                filePath = mySongs.get(audioPosition).toString();
//                Log.d("TAG123filepath",filePath);
                playSound(filePath);
            }
        });

        //play-pause button listener ----
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateButtons(PLAY);
                stopCover();
                stopSound();
//                previousView.setBackgroundColor(Color.argb(0, 50, 0, 50));
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fileSelected) {
                    updateButtons(PAUSE);
                    mediaPlayer.start();
                    playCover();
                    previousView.setBackgroundColor(Color.argb(40, 50, 0, 50));
                }
                else{
                    Toast.makeText(ListenAudios.this, "Select audio to play", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //coverView Listeners ----
        mCoverView.setCallbacks(new MusicCoverView.Callbacks() {
            @Override
            public void onMorphEnd(MusicCoverView coverView) {

                if (MusicCoverView.SHAPE_CIRCLE == coverView.getShape()) {
                    coverView.start();
                }
            }

            @Override
            public void onRotateEnd(MusicCoverView coverView) {
                coverView.morph();
            }
        });
    }

    private void displayAudios() {
        while(fs.getfetchstatus()!=true){
            mySongs=fs.findSongs(getExternalFilesDir(Environment.DIRECTORY_MUSIC));
//            Log.d("File root : ", String.valueOf(getExternalFilesDir(Environment.DIRECTORY_MUSIC)));
        }
        if(mySongs!=null){
            dialog.dismiss();
        }

        mySongs = fs.getsonglist();

        items= new String[mySongs.size()];
        for (int i=0;i<mySongs.size();i++){
            String temp = makeRecordingName(mySongs.get(mySongs.size()-i-1).getName());
//            items[i]=mySongs.get(i).getName().replace(".wav","");
            items[i] = temp;
        }
    }

    private String makeRecordingName(String name) {
        char[] chars = new char[15];
        chars[0] = name.charAt(3);
        chars[1] = name.charAt(4);
        chars[2] = name.charAt(5);
        chars[3] = ' ';
        chars[4] = name.charAt(6);
        chars[5] = name.charAt(7);
        chars[6] = ' ';
        chars[7] = name.charAt(8);
        chars[8] = name.charAt(9);
        chars[9] = ':';
        chars[10] = name.charAt(10);
        chars[11] = name.charAt(11);
        chars[12] = ':';
        chars[13] = name.charAt(12);
        chars[14] = name.charAt(13);

        String temp = String.valueOf(chars);
        return temp;
    }

    private void bindViews() {
        lv = (ListView)findViewById(R.id.listView);
        btnPause = (ImageButton) findViewById(R.id.btn_pause);
        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_play_last_audio);
        linearLayout.getBackground().setAlpha(110);
        mCoverView = (MusicCoverView) findViewById(R.id.cover);
        fs = new FetchSongs();
        mediaPlayer = new MediaPlayer();

    }

    private void updateButtons(int option) {
        Handler handler = new Handler();
        switch(option){
            case 1 :  //play
                btnPause.animate().alpha(0).setDuration(250);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnPause.setVisibility(View.GONE);
                        btnPlay.setVisibility(View.VISIBLE);
                        btnPlay.animate().alpha(1).setDuration(250);
                    }
                },250);
                break;

            case 0 :
                btnPlay.animate().alpha(0).setDuration(250);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnPlay.setVisibility(View.GONE);
                        btnPause.setVisibility(View.VISIBLE);
                        btnPause.animate().alpha(1).setDuration(250);
                    }
                },250);
                break;

            default:
                break;
        }
    }

    private void playSound(String filePath){
        try {
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
//                mediaPlayer.start();
        } catch (IOException e) {
//                Log.d("ErrorAudio",e+"");
            e.printStackTrace();
        } catch (Exception e){
//                Log.d("TAG123","Error: "+e);
            e.printStackTrace();
        }
    }

    private void stopSound(){
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    public void onBackPressed() {
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        super.onBackPressed();
    }

    private void stopCover(){
        if (mCoverView.isRunning()) {
            mCoverView.stop();
        }
    }

    private void playCover() {
        if (!mCoverView.isRunning()) {
            mCoverView.morph();
        }
    }

}
