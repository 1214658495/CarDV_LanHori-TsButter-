package com.bydauto.tsbutter.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bydauto.tsbutter.connect.IFragmentListener;
import com.bydauto.tsbutter.Model;
import com.bydauto.tsbutter.R;
import com.bydauto.tsbutter.RemoteCam;
import com.bydauto.tsbutter.unit.ServerConfig;
import com.jakewharton.disklrucache.DiskLruCache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * @author byd_tw11111
 */
public class GridViewFragment extends Fragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "GridViewFragment";

    @BindView(R.id.gv_gridView)
    GridView gvGridView;

    public SwipeRefreshLayout refreshView;

    @BindView(R.id.iv_cancel)
    ImageView ivCancel;
    @BindView(R.id.iv_share)
    ImageView ivShare;
    @BindView(R.id.iv_export)
    ImageView ivExport;
    @BindView(R.id.iv_delect)
    ImageView ivDelect;
    @BindView(R.id.iv_select)
    ImageView ivSelect;
    private ArrayList<Model> mPlaylist;

    private Unbinder unbinder;

    private List<Model> modelList = new ArrayList<>();

    private RemoteCam mRemoteCam;
    private String mPwd;

    private IFragmentListener mFragmentListener;

    public int currentSegment;

    public PhotoWallAdapter mAdapter;
    public boolean isMultiChoose = false;
    private List<Model> fileList = new ArrayList<>();


    public GridViewFragment() {
        mAdapter = null;
//        mFragmentListener = null;
        mPwd = null;
//        this.currentSegment = currentSegment;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.e(TAG, "onAttach: 1111");
        super.onAttach(activity);
        mFragmentListener = (IFragmentListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        Log.e(TAG, "onCreateView: 1111");
        View view = inflater.inflate(R.layout.fragment_gridview, container, false);
        unbinder = ButterKnife.bind(this, view);
        initData();
        initView(view);
//        prepareItem();
        return view;
    }

    private void initData() {
//        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        mLayoutManager = new GridLayoutManager(getActivity(),1);
//        mLayoutManager = new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL);


    }


    private void initView(View view) {
//        RecyclerView rvRecyclerview = (RecyclerView) findViewById(R.id.rv_recyclerview);
        refreshView = (SwipeRefreshLayout) view.findViewById(R.id.refreshView);
        refreshView.setOnRefreshListener(this);
        gvGridView.setOnItemClickListener(this);

        // 设置布局管理器
//        rvRecyclerview.setLayoutManager(mLayoutManager);

// TODO: 2017/9/25 如下到底是否需要加？
//        if (mAdapter == null && mRemoteCam.videoFolder() != null) {
//            Log.e(TAG, "initView: 11适配器为空");
//            mPwd = mRemoteCam.videoFolder() + "/";
//            listDirContent(mPwd);
//        } else {
//            // 设置adapter
//            Log.e(TAG, "initView: 1111 设置适配器");
//            showDirContents();
//        }

    }

    private void listDirContent(String path) {
        if (path != null) {
            mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_LS, path);
        }
    }

    public void setRemoteCam(RemoteCam cam) {
        mRemoteCam = cam;
    }


    public void updateDirContents(JSONObject parser) {
        refreshView.setRefreshing(false);


        ArrayList<Model> models = new ArrayList<>();

        try {
            JSONArray contents = parser.getJSONArray("listing");

            for (int i = 0; i < contents.length(); i++) {
                Model item = new Model(contents.getJSONObject(i).toString());

                if ((item.getName().endsWith(".MP4")) || (item.getName().endsWith(".mp4")) || (item.getName().endsWith(".JPG"))) {
                    models.add(item);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Collections.sort(models, new Order());
        if (currentSegment == 0 && models.size() > 0) {
            models.remove(0);
        }
        mPlaylist = models;
        if (mFragmentListener != null) {
            mFragmentListener.onFragmentAction(IFragmentListener.ACTION_UPDATE_PLAYLIST, mPlaylist);
        }
        mAdapter = new PhotoWallAdapter(getActivity(), 0, mPlaylist, gvGridView);
        showDirContents();
    }

    private void showDirContents() {
        gvGridView.setAdapter(mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.fluchCache();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mAdapter != null) {
            mAdapter.cancelAllTasks();
            mAdapter.clear();
        }
        mAdapter = null;
        mPwd = null;
        mFragmentListener = null;
        currentSegment = 0;

    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView: ");
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onRefresh() {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (!isMultiChoose) {
            Model item = (Model) parent.getItemAtPosition(position);
            Log.e(TAG, "onItemClick: 11111 !isMultiChoose");
            if (mFragmentListener != null) {
                if (currentSegment == 2) {
                    mFragmentListener.onFragmentAction(IFragmentListener.ACTION_PHOTO_DETAIL, item);

                }
            }
        } else {
            Log.e(TAG, "onItemClick: 11111 isMultiChoose");
//            由getisSelectedAt函数得到item一直未被选择过返回false
            boolean isSelected = mAdapter.getisSelectedAt(position);
            if (!isSelected) {
                fileList.add(mPlaylist.get(position));
            } else {
                fileList.remove(mPlaylist.get(position));
            }

            Log.e(TAG, "onItemClick: 111" + isSelected);
//            如果设置为isSelected则选不中checkbox  // 选中状态的切换?
            mAdapter.setItemisSelectedMap(position, !isSelected);
        }
    }

    @OnClick({R.id.iv_select, R.id.iv_cancel, R.id.iv_share, R.id.iv_export, R.id.iv_delect})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_select:
                enterChoose();
                break;
            case R.id.iv_cancel:
                enterCancel();
                break;
            case R.id.iv_share:
                break;
            case R.id.iv_export:
                if (fileList.size() > 0) {
                    if (mFragmentListener != null) {
                        mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE_MULTI, fileList);
                        mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_DOWNLOAD, null);
                    }
                } else {
                    Toast.makeText(getActivity(), "请选择一个文件", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_delect:
                // TODO: 2017/9/22 待办
                if (fileList.size() > 0) {
                    mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE_MULTI, fileList);
                    for (Model m : fileList) {
                        if (currentSegment == 0) {
                            mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE, mRemoteCam.videoFolder()
                                    + "/" + m.getName());
                        } else if (currentSegment == 1) {
                            mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE, mRemoteCam.eventFolder()
                                    + "/" + m.getName());
                        } else if (currentSegment == 2) {
                            mFragmentListener.onFragmentAction(IFragmentListener.ACTION_FS_DELETE, mRemoteCam.photoFolder()
                                    + "/" + m.getName());
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "请先选择一个删除文件", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                break;
        }
//            作用：让checkbox更新显示
        mAdapter.notifyDataSetChanged();
    }

    private void enterChoose() {
        isMultiChoose = true;
        ivSelect.setVisibility(View.GONE);
        ivCancel.setVisibility(View.VISIBLE);
        fileList.clear();
    }

    public void enterCancel() {
        isMultiChoose = false;
        ivSelect.setVisibility(View.VISIBLE);
        ivCancel.setVisibility(View.GONE);
        fileList.clear();
        // TODO: 2017/9/21 如下作用还不清楚
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
//            if (!mAdapter.isSelectedMap.isEmpty()) {//此处被我屏蔽了
            mAdapter.isSelectedMap.clear();
//            }
        }
    }

    public void clearAdapter() {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.cancelAllTasks();
        }
    }

    private class Order implements Comparator<Model> {

        @Override
        public int compare(Model lhs, Model rhs) {
            return rhs.getName().compareTo(lhs.getName());
        }

    }

    private class PhotoWallAdapter extends ArrayAdapter<Model>
            // implements OnScrollListener
    {
        final private ArrayList<Model> mArrayList;

        private Set<BitmapWorkerTask> taskCollection;

        private LruCache<String, Bitmap> mMemoryCache;

        private DiskLruCache mDiskLruCache;

        private GridView mPhotoWall;
        //记录选择的项目和是否选中状态
        //        public HashMap<Integer, Boolean> isSelectedMap;
        // 记录选择的项目和是否选中状态
        public SparseBooleanArray isSelectedMap;


        //isMultiChoose 表示是否需要重新加载缩略图
        public PhotoWallAdapter(Context context, int textViewResourceId, ArrayList<Model>
                arrayList, GridView photoWall) {

            super(context, textViewResourceId, arrayList);

            mArrayList = arrayList;
            mPhotoWall = photoWall;
//            isSelectedMap = new HashMap<Integer, Boolean>();
            isSelectedMap = new SparseBooleanArray();

            taskCollection = new HashSet<BitmapWorkerTask>();
            int maxMemory = (int) Runtime.getRuntime().maxMemory();
            Log.e(TAG, "PhotoWallAdapter: maxMemory = " + maxMemory);
            int cacheSize = maxMemory / 8;
            mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount();
                }
            };

            try {
                // 获取图片缓存路径
                File cacheDir = getDiskCacheDir(context, "thumb");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                // 创建DiskLruCache实例，初始化缓存数据
                mDiskLruCache = DiskLruCache
                        .open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            loadBitmaps(0, mArrayList.size());

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Model model = mArrayList.get(position);
            View view;
            if (convertView == null) {
                if (currentSegment == 0) {
                    Log.e(TAG, "getView: 111 currentSegment == 0");
                    view = LayoutInflater.from(getContext()).inflate(R.layout
                            .layout_normal_video, null);
                } else if (currentSegment == 1) {
                    Log.e(TAG, "getView: 111 currentSegment == 1");
                    view = LayoutInflater.from(getContext()).inflate(R.layout
                            .layout_collision_video, null);
                } else {
                    Log.e(TAG, "getView: 111 currentSegment == 2");
                    view = LayoutInflater.from(getContext()).inflate(R.layout.layout_photo, null);
                }
            } else {
                Log.e(TAG, "getView: 111 convertView ！= null");
                view = convertView;
            }


            CheckBox cbMultiChoose = (CheckBox) view.findViewById(R.id.cb_cbx);

            // TODO: 2017/9/19 先注释了如下判断
            if (isMultiChoose) {
                cbMultiChoose.setVisibility(View.VISIBLE);
                // TODO: 2017/9/21 此处自己添加
//                cbMultiChoose.setClickable(true);
//                cbMultiChoose.setFocusable(true);//只让box被点击，防止照片被点击
//从hash表中获取位置选中状态，不会导致错位
                cbMultiChoose.setChecked(getisSelectedAt(position));
                if (currentSegment != 2) {
//                    tvFileNum.setText("已选择" + fileList.size() + "个视频");
                } else {
//                    tvFileNum.setText("已选择" + fileList.size() + "张照片");
                }
            } else {
                cbMultiChoose.setVisibility(View.INVISIBLE);

            }

            TextView nameView = (TextView) view.findViewById(R.id.tv_title);

//            int i = model.getName().indexOf('_');
//            int i2 = model.getName().lastIndexOf('_');
//            int i3 = model.getName().indexOf('.');
//            String date = model.getName().substring(i + 1, i2);
//            StringBuilder sb = new StringBuilder(date);
//            sb.insert(6, '-');
//            sb.insert(4, '-');
//            String time = model.getName().substring(i2 + 1, i3 - 1);
//            StringBuilder sb2 = new StringBuilder(time);
//            sb2.insert(4, ':').insert(2, ':');
//            nameView.setText(sb.toString() + "  " + sb2.toString());
//           // nameView.setText(model.getName());

            String mData = model.getName().substring(0, 10);
            String mTime = model.getName().substring(11, 19);
            nameView.setText(mData + " " + mTime);

            String url = null;

            if (currentSegment == 0) {
//                url = "http://" + ServerConfig.HOST + mRemoteCam.videoFolder().substring(4) + "/Thumb/" +
//                        model.getThumbFileName();
                url = "http://" + ServerConfig.HOST + mRemoteCam.videoFolder().substring(4) + "/" +
                        model.getName();
//                imageUrl=http://192.168.42.1/SD0/NORMAL/2017-08-30-16-48-20.mp4
            } else if (currentSegment == 1) {
//                url = "http://" + ServerConfig.HOST + mRemoteCam.eventFolder().substring(4) + "/Thumb/" +
//                        model.getThumbFileName();
                url = "http://" + ServerConfig.HOST + mRemoteCam.eventFolder().substring(4) + "/" +
                        model.getName();
            } else if (currentSegment == 2) {
//                url = "http://" + ServerConfig.HOST + mRemoteCam.photoFolder().substring(4) + "/Thumb/" +
//                        model.getThumbFileName();
                url = "http://" + ServerConfig.HOST + mRemoteCam.photoFolder().substring(4) + "/" +
                        model.getName();  //删除二级文件夹
            }
            ImageView photo;
            if (currentSegment == 2) {
                photo = (ImageView) view.findViewById(R.id.iv_pic_photo);
            } else {
                photo = (ImageView) view.findViewById(R.id.iv_icon);
            }
            photo.setTag(url);
            setImageView(url, photo);
//            loadBitmaps(0, mArrayList.size());
            loadBitmaps(photo, url);
            return view;
        }

        private void setImageView(String imageUrl, ImageView imageView) {
            Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);

            } else {
                imageView.setImageResource(R.mipmap.empty_photo);
            }

        }

        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
            if (getBitmapFromMemoryCache(key) == null) {
                mMemoryCache.put(key, bitmap);
            }
        }

        public Bitmap getBitmapFromMemoryCache(String key) {
            return mMemoryCache.get(key);
        }


        /**
         * private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {
         * try {
         * for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
         * Model model = mArrayList.get(i);
         * String imageUrl;
         * if (currentSegment == 0) {
         * //  imageUrl = "http://" + ServerConfig.HOST + mRemoteCam.videoFolder().substring(4) +
         * //           "/Thumb/" + model.getThumbFileName();
         * imageUrl = "http://" + ServerConfig.HOST + mRemoteCam.videoFolder().substring(4) + "/" + model.getName();
         * } else if (currentSegment == 1) {
         * //     imageUrl = "http://" + ServerConfig.HOST + mRemoteCam.eventFolder().substring(4) +
         * //                 "/Thumb/" + model.getThumbFileName();
         * imageUrl = "http://" + ServerConfig.HOST + mRemoteCam.eventFolder().substring(4) + "/" + model.getName();
         * } else {
         * //              imageUrl = "http://" + ServerConfig.HOST + mRemoteCam.photoFolder().substring(4) +
         * //               "/Thumb/" + model.getThumbFileName();
         * imageUrl = "http://" + ServerConfig.HOST + mRemoteCam.photoFolder().substring(4) + "/" + model.getName();  //删除二级文件夹
         * }
         * Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
         * if (bitmap == null) {
         * BitmapWorkerTask task = new BitmapWorkerTask();
         * taskCollection.add(task);
         * task.execute(imageUrl);
         * } else {
         * ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
         * if (imageView != null && bitmap != null) {
         * imageView.setImageBitmap(bitmap);
         * }
         * }
         * }
         * } catch (Exception e) {
         * e.printStackTrace();
         * }
         * }
         */


        public void loadBitmaps(ImageView imageView, String imageUrl) {
            try {
                Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
                if (bitmap == null) {
                    BitmapWorkerTask task = new BitmapWorkerTask();
                    taskCollection.add(task);
                    task.execute(imageUrl);
                } else {
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void cancelAllTasks() {
            if (taskCollection != null) {
                for (BitmapWorkerTask task : taskCollection) {
                    task.cancel(false);
                }
            }
        }

        /**
         * 根据传入的uniqueName获取硬盘缓存的路径地址。
         */
        public File getDiskCacheDir(Context context, String uniqueName) {
            String cachePath;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                    || !Environment.isExternalStorageRemovable()) {
                cachePath = context.getExternalCacheDir().getPath();
            } else {
                cachePath = context.getCacheDir().getPath();
            }
            return new File(cachePath + File.separator + uniqueName);
        }

        /**
         * 获取当前应用程序的版本号。
         */
        public int getAppVersion(Context context) {
            try {
                PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(),
                        0);
                return info.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return 1;
        }

        /**
         * 使用MD5算法对传入的key进行加密并返回。
         */
        public String hashKeyForDisk(String key) {
            String cacheKey;
            try {
                final MessageDigest mDigest = MessageDigest.getInstance("MD5");
                mDigest.update(key.getBytes());
                cacheKey = bytesToHexString(mDigest.digest());
            } catch (NoSuchAlgorithmException e) {
                cacheKey = String.valueOf(key.hashCode());
            }
            return cacheKey;
        }

        /**
         * 将缓存记录同步到journal文件中。
         */
        public void fluchCache() {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String bytesToHexString(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String hex = Integer.toHexString(0xFF & bytes[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        }


        public boolean getisSelectedAt(int position) {

            //如果当前位置的key值为空，则表示该item未被选择过，返回false，否则返回true
            if (isSelectedMap.get(position)) {
                return isSelectedMap.get(position);
            }
            return false;
        }

        public void setItemisSelectedMap(int position, boolean isSelected) {
            this.isSelectedMap.put(position, isSelected);
            notifyDataSetChanged();
        }

        class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

            private String imageUrl;

            @Override
            protected Bitmap doInBackground(String... params) {
                imageUrl = params[0];
                FileDescriptor fileDescriptor = null;
                FileInputStream fileInputStream = null;
                InputStream is = null;
                DiskLruCache.Snapshot snapShot = null;
                try {
                    // 生成图片URL对应的key
                    final String key = hashKeyForDisk(imageUrl);
                    // 查找key对应的缓存
                    snapShot = mDiskLruCache.get(key);
                    if (snapShot == null) {
                        // 如果没有找到对应的缓存，则准备从网络上请求数据，并写入缓存
                        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                        if (editor != null) {
                            OutputStream outputStream = editor.newOutputStream(0);
                            if (downloadUrlToStream(imageUrl, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                        }
                        // 缓存被写入后，再次查找key对应的缓存
                        snapShot = mDiskLruCache.get(key);
                    }


                    /**
                     *
                     if (snapShot != null) {
                     fileInputStream = (FileInputStream) snapShot.getInputStream(0);
                     fileDescriptor = fileInputStream.getFD();
                     }
                     // 将缓存数据解析成Bitmap对象
                     Bitmap bitmap = null;
                     if (fileDescriptor != null) {
                     bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                     // BitmapFactory.Options options = new BitmapFactory.Options();
                     // options.inSampleSize = 16;
                     //  bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
                     }
                     */
                    Bitmap bitmap = null;
                    if (snapShot != null) {
                        is = snapShot.getInputStream(0);
                        bitmap = BitmapFactory.decodeStream(is);
                    }

                    if (bitmap != null) {
                        // 将Bitmap对象添加到内存缓存当中
                        addBitmapToMemoryCache(params[0], bitmap);
                    }
                    return bitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    /**
                     *
                     if (fileDescriptor == null && fileInputStream != null) {
                     try {
                     fileInputStream.close();
                     } catch (IOException e) {
                     }
                     }
                     */

                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
                if (imageView != null && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
                taskCollection.remove(this);
            }

            /**
             * 建立HTTP请求，并获取Bitmap对象。
             *
             * @param urlString 图片的URL地址
             * @return 解析后的Bitmap对象
             */
            private boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
                HttpURLConnection urlConnection = null;
                BufferedOutputStream out = null;
                BufferedInputStream in = null;
                Bitmap bitmap;
                try {
                    final URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();

//我添加的
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 18;  //16-free 2.3M
                    bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream(), null, options);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    InputStream inputimage = new ByteArrayInputStream(baos.toByteArray());
                    in = new BufferedInputStream(inputimage, 8 * 1024);

//                    in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
                    out = new BufferedOutputStream(outputStream, 8 * 1024);
                    int b;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    return true;
                } catch (final IOException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            //如何下载yuv mListener.onFragmentAction(IFragmentListener.ACTION_FS_DOWNLOAD, null);
            private Bitmap downloadBitmap(String imageUrl) {
                Bitmap bitmap = null;
                HttpURLConnection con = null;
                try {
//                    Log.e(TAG, "downloadBitmap: 1111 tryHttpURLConnection");
                    URL url = new URL(imageUrl);
                    con = (HttpURLConnection) url.openConnection();
                    con.setConnectTimeout(5 * 1000);
                    con.setReadTimeout(10 * 1000);
                    con.setDoInput(true);
                    con.setDoOutput(true);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 16;  //16-free 2.3M

//                    bitmap = BitmapFactory.decodeStream(con.getInputStream());
                    bitmap = BitmapFactory.decodeStream(con.getInputStream(), null, options);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
                return bitmap;
            }
        }
    }
}
