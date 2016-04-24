package com.wenhuaijun.easyimageloader.imageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import com.wenhuaijun.easyimageloader.R;
import java.util.concurrent.Executor;

/**
 * Created by Wenhuaijun on 2016/4/22 0022.
 */
public class ImageLoader {
    private static final String TAG="TAG";
    private static final int TAG_KEY_URI = R.id.image;
    private Context mContext;
    private ImageLrucache imageLrucache;
    //创建一个静态的线程池对象
    public static final Executor THREAD_POOL_EXECUTOR =ImageThreadPoolExecutor.getInstance();
    //创建一个更新ImageView的UI的Handler
    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //给iamgeView加载bitmap
            TaskResult result =(TaskResult) msg.obj;
            ImageView imageView =result.imageView;
            //判断是否数据错乱
            String uri =(String)imageView.getTag(TAG_KEY_URI);
            if (uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else{
                Log.i(TAG,"数据不是最新数据");
            }
        }

    };

    //私有的构造方法，防止在外部实例化该ImageLoader
    private ImageLoader (Context context){
        mContext =context.getApplicationContext();
        imageLrucache = new ImageLrucache();
    }
    //这或许是最简单的建造者模式
    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    public void bindBitmap(final String url, final ImageView imageView){
        bindBitmap(url, imageView, 0, 0);
    }
    //外层调用的方法，依次从内存、本地、网络中去获取缓存
    public void bindBitmap(final String uri,final ImageView imageView,final int reqWidth,final int reqHeight){
        //imageView的数据清空
       // imageView.setImageResource(R.drawable.ic_loading);
        //防止加载图片的时候数据错乱
        imageView.setTag(TAG_KEY_URI, uri);
        //从内存缓存中获取bitmap
        Bitmap bitmap = imageLrucache.loadBitmapFromMemCache(uri);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        LoadBitmapTask loadBitmapTask =new LoadBitmapTask(mContext,mMainHandler,imageView,uri,reqWidth,reqHeight);
       //使用线程池去执行Runnable对象
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }
}