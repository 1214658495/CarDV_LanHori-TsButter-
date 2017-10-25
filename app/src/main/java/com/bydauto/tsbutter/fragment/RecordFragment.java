package com.bydauto.tsbutter.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bydauto.tsbutter.connect.IFragmentListener;
import com.bydauto.tsbutter.R;
import com.bydauto.tsbutter.RemoteCam;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by byd_tw on 2017/9/18.
 */

public class RecordFragment extends Fragment implements SurfaceHolder.Callback, IFragmentListener {
    private static final String TAG = "RecordFragment";
    @BindView(R.id.iv_record)
    ImageView ivRecord;
    @BindView(R.id.iv_takephoto)
    ImageView ivTakephoto;
    @BindView(R.id.iv_mic)
    ImageView ivMic;
    @BindView(R.id.iv_setting)
    ImageView ivSetting;
    @BindView(R.id.ll_menus)
    LinearLayout llMenus;
//    @BindView(R.id.iv_photo_focus)
//    ImageView ivPhotoFocus;
//    @BindView(R.id.record_loading)
//    ImageView recordLoading;
//    @BindView(R.id.record_bg)
//    RelativeLayout recordBg;
//    @BindView(R.id.textViewRecordTime)
//    TextView textViewRecordTime;
//    @BindView(R.id.isRec)
//    LinearLayout isRec;
    @BindView(R.id.sv_recordVideo)
    SurfaceView svRecordVideo;


    private Unbinder unbinder;
    private RemoteCam mRemoteCam;

    IjkMediaPlayer player;
    SurfaceView surface;
    SurfaceHolder surfaceHolder;
    private String path = "rtsp://192.168.42.1/live" ;

//    public RecordFragment() {
//        super();
//    }
//
//    public RecordFragment(Context context) {
//        super();
//    }

//    public ViewListPageFragment(Context context, onSegmentViewClickListener mOnSegmentViewClickListener) {
//        this.mOnSegmentViewClickListener = mOnSegmentViewClickListener;
//    }


    @Override
    public void onAttach(Activity activity) {
        Log.e(TAG, "onAttach: 1111");
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        Log.e(TAG, "onCreateView: 1111");
        View view = inflater.inflate(R.layout.fragment_record, container, false);
        unbinder = ButterKnife.bind(this, view);
        init(view);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        // activity 可见时尝试继续播放
        if (player != null){
            player.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        player.pause();
    }

    public void setRemoteCam(RemoteCam cam) {
        mRemoteCam = cam;
    }

    private void init(View view) {
//        tvSegmentNomalVideo = (TextView) view.findViewById(R.id.ivNormalVideoList);
//        tvSegmentPhoto = (TextView) view.findViewById(R.id.tv_segmentPhoto);
//        tvSegmentEventVideo = (TextView) view.findViewById(R.id.tv_segmentEventVideo);

        // 初始化播放器
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        surface = (SurfaceView) view.findViewById(R.id.sv_recordVideo);
        surfaceHolder = surface.getHolder();
        //如下未实现比例
        surfaceHolder.setFixedSize(getActivity().getWindowManager().getDefaultDisplay()
                .getWidth(), getActivity().getWindowManager().getDefaultDisplay().getWidth()
                / 16 * 9);
        surfaceHolder.addCallback(this);
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: ");
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.iv_record, R.id.iv_takephoto, R.id.iv_mic, R.id.iv_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_record:
                break;
            case R.id.iv_takephoto:
                break;
            case R.id.iv_mic:
                break;
            case R.id.iv_setting:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        openVideo();
        player.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void openVideo(){
        release();

        try {
            player = new IjkMediaPlayer();

            player.setDataSource(path);
            player.setDisplay(surfaceHolder);

            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setScreenOnWhilePlaying(true);
            player.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void release() {
        if (player != null) {
            player.reset();
            player.release();
            player = null;
//            AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//            am.abandonAudioFocus(null);
        }
    }

    @Override
    public void onFragmentAction(int type, Object param, Integer... array) {

    }



}
