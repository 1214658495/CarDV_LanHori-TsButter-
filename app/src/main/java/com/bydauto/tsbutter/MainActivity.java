package com.bydauto.tsbutter;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bydauto.tsbutter.Connect.IChannelListener;
import com.bydauto.tsbutter.Connect.IFragmentListener;
import com.bydauto.tsbutter.Fragment.GridViewFragment;
import com.bydauto.tsbutter.Fragment.PhotoDetailFragment;
import com.bydauto.tsbutter.Fragment.RecordFragment;
import com.bydauto.tsbutter.Unit.LogcatHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements IFragmentListener, IChannelListener {
    private static final String TAG = "MainActivity";

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.iv_recordSelected)
    ImageView ivRecordSelected;
    @BindView(R.id.iv_normalVideoList)
    ImageView ivNormalVideoList;
    @BindView(R.id.iv_photoList)
    ImageView ivPhotoList;
    @BindView(R.id.iv_eventVideoList)
    ImageView ivEventVideoList;
    @BindView(R.id.fl_contentView)
    FrameLayout flContentView;
    @BindView(R.id.fl_all)
    FrameLayout flAll;


    private RecordFragment mRecordFragment;
    private GridViewFragment mGridViewFragment;
    public RemoteCam mRemoteCam;
    public ArrayList<Model> mPlaylist; //播放列表
    private List<Model> selectedFiles;
    private int selectedFilesCount;
    private static int hadDownloadedFlag = 0;
    private String mGetFileName;

    private boolean isConnected;
//    private Context context;
//
//    public MainActivity(Context context) {
//        this.context = context;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: 111");
        super.onCreate(savedInstanceState);
        LogcatHelper.getInstance(this).start();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mRemoteCam = new RemoteCam(this);
        mRemoteCam.setChannelListener(this).setConnectivity(RemoteCam
                .CAM_CONNECTIVITY_WIFI_WIFI).setWifiInfo
                (wifiManager.getConnectionInfo().getSSID().replace("\"", ""), getWifiIpAddr());


        mRemoteCam.startSession();
//        mGridViewFragment.setRemoteCam(mRemoteCam);

        if (null == mRecordFragment) {
            mRecordFragment = new RecordFragment();
        }
//        getFragmentManager().beginTransaction().replace(R.id.fl_list, mGridListFragment).commitAllowingStateLoss();
        getFragmentManager().beginTransaction().replace(R.id.fl_contentView, mRecordFragment).commit();

        mRecordFragment.setRemoteCam(mRemoteCam);
//
////        getFragmentManager().beginTransaction().replace(R.id.fl_list, mGridListFragment).commitAllowingStateLoss();
//        getFragmentManager().beginTransaction().replace(R.id.fl_contentView, mGridViewFragment).commit();

    }

    private String getWifiIpAddr() {
//        WifiManager mgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager mgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        int ip = mgr.getConnectionInfo().getIpAddress();

        return String.format("%d.%d.%d.%d", (ip & 0xFF), (ip >> 8 & 0xFF), (ip >> 16 & 0xFF), (ip >> 24));
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: 111");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogcatHelper.getInstance(this).stop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRemoteCam.stopSession();
        finish();
        Log.e(TAG, "kill the process to force fresh launch next time");
        Process.killProcess(Process.myPid());
    }


    @OnClick({R.id.iv_back, R.id.iv_recordSelected, R.id.iv_normalVideoList, R.id.iv_photoList, R.id.iv_eventVideoList})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                mRemoteCam.stopSession();
                finish();
                Log.e(TAG, "kill the process to force fresh launch next time");
                Process.killProcess(Process.myPid());
                break;
            case R.id.iv_recordSelected:
                showRecordFragment();
                break;
            case R.id.iv_normalVideoList:
                showNormalVideoList();
                break;
            case R.id.iv_photoList:
                showPhotoList();
                break;
            case R.id.iv_eventVideoList:
                showEventVideoList();
                break;
        }
    }

    private void showRecordFragment() {
        if (null == mRecordFragment) {
            Log.e(TAG, "showRecordFragment: null == mRecordFragment");
            mRecordFragment = new RecordFragment();

        }
        getFragmentManager().beginTransaction().replace(R.id.fl_contentView, mRecordFragment).commit();

//        mRecordFragment.setRemoteCam(mRemoteCam);

    }


    private void showNormalVideoList() {
        Log.e(TAG, "showNormalVideoList: 1111");
        if (null == mGridViewFragment) {
            Log.e(TAG, "showNormalVideoList: 1111  null == mGridViewFragment");
            mGridViewFragment = new GridViewFragment();

        }
        getFragmentManager().beginTransaction().replace(R.id.fl_contentView, mGridViewFragment).commit();

        mGridViewFragment.currentSegment = 0;

        mGridViewFragment.clearAdapter();
//        getFragmentManager().beginTransaction().replace(R.id.fl_list, mGridListFragment).commitAllowingStateLoss();

        mGridViewFragment.setRemoteCam(mRemoteCam);
        Log.e(TAG, "showNormalVideoList: mRemoteCam.videoFolder()"+ mRemoteCam.videoFolder());
//        mRemoteCam.listDir(mRemoteCam.videoFolder());
        mRemoteCam.listDir("/tmp/SD0/NORMAL");

        if (mGridViewFragment.isMultiChoose) {
            mGridViewFragment.enterCancel();
        }
    }


    private void showPhotoList() {
        Log.e(TAG, "showPhotoList: 1111");
        if (null == mGridViewFragment) {
            Log.e(TAG, "showPhotoList: 1111  null == mGridViewFragment");
            mGridViewFragment = new GridViewFragment();

        }
        getFragmentManager().beginTransaction().replace(R.id.fl_contentView, mGridViewFragment).commit();
//
        mGridViewFragment.setRemoteCam(mRemoteCam);
//        mPWD = mRemoteCam.videoFolder() + "/";
        mGridViewFragment.currentSegment = 2;
        mGridViewFragment.clearAdapter();
        mRemoteCam.listDir(mRemoteCam.photoFolder());

        if (mGridViewFragment.isMultiChoose) {
            mGridViewFragment.enterCancel();
        }
    }

    private void showEventVideoList() {
        Log.e(TAG, "showEventVideoList: 1111");
        if (null == mGridViewFragment) {
            mGridViewFragment = new GridViewFragment();

        }

        getFragmentManager().beginTransaction().replace(R.id.fl_contentView, mGridViewFragment).commit();
        mGridViewFragment.setRemoteCam(mRemoteCam);
        mGridViewFragment.currentSegment = 1;
        mGridViewFragment.clearAdapter();
        mRemoteCam.listDir(mRemoteCam.eventFolder());

        if (mGridViewFragment.isMultiChoose) {
            mGridViewFragment.enterCancel();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e(TAG, "onConfigurationChanged: 1111");
    }

    @Override
    public void onFragmentAction(int type, Object param, Integer... array) {
        switch (type) {
            case IFragmentListener.ACTION_PHOTO_DETAIL:
                Log.e(TAG, "onFragmentAction: 准备显示照片详情");
                showPhotoDetailFragment((Model) param);
                break;
            case IFragmentListener.ACTION_FS_LS:
                Log.e(TAG, "onFragmentAction: IFragmentListener.ACTION_FS_LS");
                mRemoteCam.listDir((String) param);
                break;
            case IFragmentListener.ACTION_UPDATE_PLAYLIST:
                mPlaylist = (ArrayList<Model>) param;
                break;
            case IFragmentListener.ACTION_FS_DELETE_MULTI:
                selectedFiles = (ArrayList<Model>) param;
                selectedFilesCount = selectedFiles.size();
                break;
            case IFragmentListener.ACTION_FS_DOWNLOAD:
                if (param != null) {
                    mGetFileName = (String) param;
                    mRemoteCam.getFile(mGetFileName);
                } else {
                    downloadFiles();
                }
                break;
            case IFragmentListener.ACTION_FS_DELETE:
                showWaitProgram("Please waiting!");
                mRemoteCam.deleteFile((String) param);
                break;
        }
    }


    private void downloadFiles() {
        if (hadDownloadedFlag < selectedFilesCount) {
            if (mGridViewFragment.currentSegment == 0) {
                mGetFileName = mRemoteCam.videoFolder() + "/" + selectedFiles.get(hadDownloadedFlag).getName();
//                mGetFileName = Environment.getExternalStorageState()
// + "/BYD行车记录仪" + mRemoteCam.videoFolder().substring()
            } else if (mGridViewFragment.currentSegment == 1) {
                mGetFileName = mRemoteCam.eventFolder() + "/" + selectedFiles.get(hadDownloadedFlag).getName();
            } else if (mGridViewFragment.currentSegment == 2) {
                mGetFileName = mRemoteCam.photoFolder() + "/" + selectedFiles.get(hadDownloadedFlag).getName();
            }

            String fileName = Environment.getExternalStorageState()
                    + "/行车记录仪" + mGetFileName.substring(mGetFileName.lastIndexOf('/'));
            File file = new File(fileName);
            if (!file.exists()) {
//                file.mkdir();
//                param":"/tmp/SD0/PHOTO/2017-09-21-10-32-12.JPG"
                mRemoteCam.getFile(mGetFileName);
            } else {
                Toast.makeText(this, "文件已经下载了", Toast.LENGTH_SHORT).show();
                hadDownloadedFlag++;

            }
        } else {
            Toast.makeText(this, "文件下载结束", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWaitProgram(String s) {
        // TODO: 2017/9/22 添加操作
    }

    private void showPhotoDetailFragment(Model model) {
        Log.e(TAG, "showPhotoDetailFragment: 显示照片详情");
//        PhotoDetailFragment mPhotoDetailFragment = new PhotoDetailFragment((String) param);
//        String pwd = "/tmp/SD0/PHOTO";
        Fragment mPhotoDetailFragment = PhotoDetailFragment.newInstance(model, mRemoteCam.photoFolder());
        getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).replace(R.id.fl_all, mPhotoDetailFragment).addToBackStack(null).commit();
    }

    @Override
    public void onChannelEvent(final int type, final Object param, final String... array) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type & IChannelListener.MSG_MASK) {
                    case IChannelListener.CMD_CHANNEL_MSG:
                        handleCmdChannelEvent(type, param, array);
                        return;
                    case IChannelListener.DATA_CHANNEL_MSG:
                        handleDataChannelEvent(type, param);
                        return;
                    case IChannelListener.STREAM_CHANNEL_MSG:
                        handleStreamChannelEvent(type, param);
                        return;
                }
            }
        });
    }

    private void handleCmdChannelEvent(int type, Object param, String... array) {
        if (type >= 80) {
            handleCmdChannelError(type, param);
            return;
        }

        switch (type) {
            case IChannelListener.CMD_CHANNEL_EVENT_CONNECT_STATE:
                isConnected = (boolean) param;
                if (isConnected) {

                } else {
                    // TODO: 2017/9/25 后续优化提示
                    Toast.makeText(this, "请连接Wi-Fi", Toast.LENGTH_SHORT).show();
                }
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_START_SESSION:
                isConnected = true;
                // TODO: 2017/9/25 检测录音和录像状态
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                        removeLoadingFrag();
//                        showRecordFragment();
////                        showVideoFrag();
//                    }
//                }, 1000);

            case IChannelListener.CMD_CHANNEL_EVENT_GET_SINGLE_SETTING:
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_LS:
                // TODO: 2017/9/19 做grid view的处理
// mGridViewFragment.um
                mGridViewFragment.updateDirContents((JSONObject) param);
                break;
            case IChannelListener.CMD_CHANNEL_EVENT_START_LS:
//			showWaitDialog("Fetching Directory Info");
//                if (mCurrentFrag == mVideoFrag) {
//                mGridViewFragment.refreshView.setRefreshing(true);
//                mGridViewFragment.refreshView.post(new Runnable() {
//                    @Override
//                    public void run() {
                mGridViewFragment.refreshView.setRefreshing(true);
//                    }
//                });
//                } else if (mCurrentFrag == mPhotoFrag) {
//                    mPhotoFrag.refreshView.setRefreshing(true);
//                }
                break;
        }
    }

    private void handleCmdChannelError(int type, Object param) {
        switch (type) {
            case IChannelListener.CMD_CHANNEL_EVENT_GET_SINGLE_SETTING:
        }
    }

    private void handleDataChannelEvent(int type, Object param) {
        switch (type) {
            case IChannelListener.DATA_CHANNEL_EVENT_GET_FINISH:
                Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show();
                hadDownloadedFlag++;
                downloadFiles();
                break;

        }
    }


    private void handleStreamChannelEvent(int type, Object param) {
        switch (type) {
            case IChannelListener.STREAM_CHANNEL_EVENT_BUFFERING:
//                showWaitDialog("Buffering...");
                break;
            case IChannelListener.STREAM_CHANNEL_EVENT_PLAYING:
//                dismissDialog();
//                mRecordFrag.startStreamView();
                break;

            case IChannelListener.STREAM_CHANNEL_ERROR_PLAYING:
//                mRecordFrag.resetStreamView();
//                showAlertDialog("Error", "Cannot connect to LiveView!");
                break;

        }
    }


}
