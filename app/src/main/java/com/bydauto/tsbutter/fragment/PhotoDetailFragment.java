package com.bydauto.tsbutter.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bydauto.tsbutter.Model;
import com.bydauto.tsbutter.R;
import com.bydauto.tsbutter.unit.ServerConfig;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by byd_tw on 2017/8/22.
 */

public class PhotoDetailFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "PhotoDetailFragment";
    
    private ImageView mPhotoDetailImage,mRotateImage,mBackImage,mPhotoDownloadImage;
    private Bitmap bitmap;
    private String filePath;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            bitmap = (Bitmap) msg.obj;
            mPhotoDetailImage.setImageBitmap(bitmap);

        }
    };

    public PhotoDetailFragment() {
        super();
    }

    public static PhotoDetailFragment newInstance(Model model, String pwd) {
        PhotoDetailFragment newFragment = new PhotoDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("fP", (pwd + "/" + model.getName()));
        newFragment.setArguments(bundle);
        return newFragment;
    }
//    public PhotoDetailFragment(String filePath) {
//        super();
//        this.filePath = filePath;
//    }


//    从Activity传递到Fragment中的参数
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate: 111");
        Bundle arg1 = getArguments();
        filePath = arg1.getString("fP");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photodetail, container, false);
        init(view);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void init(View view) {
        mPhotoDetailImage = (ImageView) view.findViewById(R.id.img_photoDetail);
        mBackImage = (ImageView) view.findViewById(R.id.img_back);
        mRotateImage = (ImageView) view.findViewById(R.id.img_rotate);
        mPhotoDownloadImage = (ImageView) view.findViewById(R.id.img_photoDownload);
        mBackImage.setOnClickListener(this);
        mRotateImage.setOnClickListener(this);
        mPhotoDownloadImage.setOnClickListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO: 2017/8/22 照片地址设置？
                String urlPath = "http://" + ServerConfig.HOST + filePath.substring(4);
                Bitmap bm = getInternetPhoto(urlPath);
                Message msg = new Message();
                msg.obj = bm;
                handler.sendMessage(msg);
            }
        }).start();
    }

    private Bitmap getInternetPhoto(String imageUrl) {
        Bitmap bm = null;
        HttpURLConnection con = null;
        try {
            URL url = new URL(imageUrl);
            con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5 * 1000);
            con.setReadTimeout(10 * 1000);
            bm = BitmapFactory.decodeStream(con.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return bm;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                Log.e(TAG, "onClick: 点击照片返回");
//                 getActivity().onBackPressed();
                getFragmentManager().popBackStack();
                break;
            case R.id.img_rotate:
//                原来要旋转需要返回心得照片在发送消息
                bitmap = rotateImage(bitmap, -90);
                Message msg = new Message();
                msg.obj = bitmap;
                handler.sendMessage(msg);
                break;
            case R.id.img_photoDownload:
                // TODO: 2017/9/22
                break;
        }
    }

    private Bitmap rotateImage(Bitmap originBitmap, float alpha) {
        Log.e(TAG, "rotateImage: 111"+alpha);
        if (originBitmap == null) {
            return null;
        }
        int width = originBitmap.getWidth();
        int height = originBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.setRotate(alpha);
        Bitmap newBitmap = Bitmap.createBitmap(originBitmap, 0, 0, width, height, matrix, false);
        if (newBitmap.equals(originBitmap)) {
            return newBitmap;
        }
//        originBitmap.recycle();
        return newBitmap;
    }
}
