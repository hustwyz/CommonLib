/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.wyz.common.utils;

import im.wyz.common.http.NetworkUtil;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

public class ImageDownloader {

    private static final String TAG = "ImageDownloader";

    public static int NET_TYPE;

    public enum Mode {
        NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT
    }

    private Mode mode = Mode.CORRECT;

    // MD5 hasher.
    private static MessageDigest mDigest;

    private Context mContext;

    public ImageDownloader(Context mContext) {
        super();
        this.mContext = mContext;
        try {
            mDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // This shouldn't happen.
            throw new RuntimeException("No MD5 algorithm.");
        }
    }

    public void download(String url, ImageView imageView) {
        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);

        if (bitmap == null) {
            forceDownload(url, imageView);
        } else {
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void forceDownload(String url, ImageView imageView) {
        if (url == null) {
            imageView.setImageDrawable(null);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
            switch (mode) {
                case NO_ASYNC_TASK:
                    Bitmap bitmap = downloadBitmap(url);
                    addBitmapToCache(url, bitmap);
                    imageView.setImageBitmap(bitmap);
                    break;

                case NO_DOWNLOADED_DRAWABLE:
                    imageView.setMinimumHeight(156);
                    BitmapDownloaderTask task = null;

                    task = new BitmapDownloaderTask(imageView);

                    task.execute(url);
                    break;

                case CORRECT:

                    task = new BitmapDownloaderTask(imageView);
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(
                            imageView.getResources(), task);
                    imageView.setImageDrawable(downloadedDrawable);
                    imageView.setMinimumHeight(156);
                    task.execute(url);
                    break;
            }
        }
    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.imageUrl;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    public void writeFile(String url, Bitmap bitmap) {

        String hashedUrl = getMd5(url);

        FileOutputStream fos;

        try {
            fos = mContext.openFileOutput(hashedUrl, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            return;
        }

        final BufferedOutputStream bos = new BufferedOutputStream(fos, 16384);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
        try {
            bos.flush();
            bos.close();
            fos.close();
        } catch (IOException e) {}
    }

    static class FlushedInputStream extends FilterInputStream {

        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break; // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    private Bitmap downloadBitmap(final String imageUrl) {

        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = new DefaultHttpClient();
        // 代理支持
        if (NetworkUtil.getNetworkType(mContext) == NetworkUtil.APN_CMWAP) {
            HttpHost proxy = new HttpHost(NetworkUtil.getHostIp(), NetworkUtil.getHostPort());
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }

        final HttpGet getRequest = new HttpGet(imageUrl);
        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                return null;
            }
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                final byte[] respBytes = EntityUtils.toByteArray(entity);
                writeImageFile(imageUrl, entity, respBytes);
                // Decode the bytes and return the bitmap.
                return BitmapFactory.decodeByteArray(respBytes, 0, respBytes.length, null);

            }
        } catch (IOException e) {
            getRequest.abort();
        } catch (OutOfMemoryError e) {
            clearCache();
            System.gc();
        } catch (IllegalStateException e) {
            getRequest.abort();
        } catch (Exception e) {
            getRequest.abort();
        } finally {}
        return null;
    }

    private synchronized void writeImageFile(final String imageUrl, final HttpEntity entity,
            final byte[] respBytes) throws IOException {
        String hashedUrl = getMd5(imageUrl);
        FileOutputStream fos = null;
        try {
            fos = mContext.openFileOutput(hashedUrl, Context.MODE_PRIVATE);
            fos.write(respBytes);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Error writing to bitmap cache: " + imageUrl, e);
        } catch (IOException e) {
            Log.w(TAG, "Error writing to bitmap cache: " + imageUrl, e);
        } finally {
            if (fos != null) {
                fos.close();
            }
            entity.consumeContent();
        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

        private String imageUrl;

        private ImageSwitcher imageSwitcher = null;

        private final WeakReference<ImageView> imageViewReference;

        private int newWidth;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        public BitmapDownloaderTask(ImageView imageView, int newWidth) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.newWidth = newWidth;
        }

        public BitmapDownloaderTask(ImageSwitcher imageSwitcher) {
            this.imageSwitcher = imageSwitcher;
            imageViewReference = new WeakReference<ImageView>(
                    (ImageView) imageSwitcher.getNextView());
        }

        @Override
        protected void onPreExecute() {
//            imageViewReference.get().setBackgroundResource(R.drawable.image_bg);
            super.onPreExecute();
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {

            if (isCancelled()) {
                return null;
            }

            imageUrl = params[0];

            if (!TextUtils.isEmpty(imageUrl)) {

                // first look from local file.
                Bitmap bitmap = getBitmapFromCacheFile(imageUrl);

                //download from web server
                if (bitmap == null) {

                    bitmap = downloadBitmap(imageUrl);
                }
                if (bitmap != null) {
                    addBitmapToCache(imageUrl, bitmap);
                    return bitmap;
                }
            }
            return null;
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap == null) {
                return;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if(imageView!=null)imageView.setBackgroundDrawable(null);
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                if ((this == bitmapDownloaderTask) || (mode != Mode.CORRECT)) {
                    if (imageView != null) {
                        imageView.setImageBitmap(bitmap);
                        if (imageSwitcher != null && mode != Mode.CORRECT) {
                            imageSwitcher.showNext();
                        }
                    }
                }
            }
        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class OnlyDownloadBitmapTask extends AsyncTask<String, Void, Void> {

        /**
         * Actual download method.
         */
        @Override
        protected Void doInBackground(String... params) {

            String imageUrl = params[0];
            if (!TextUtils.isEmpty(imageUrl)) {
                Bitmap bitmap = downloadBitmap(imageUrl);
                if (bitmap != null) {
                    addBitmapToCache(imageUrl, bitmap);
                }
            }
            return null;
        }
    }

    /**
     * A fake Drawable that will be attached to the imageView while the
     * download is in progress.
     * 
     * <p>
     * Contains a reference to the actual download task, so that a download
     * task can be stopped if a new binding is required, and makes sure
     * that only the last started download process can bind its result,
     * independently of the download finish order.
     * </p>
     */
    static class DownloadedDrawable extends BitmapDrawable {

        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(Resources res, BitmapDownloaderTask bitmapDownloaderTask) {
            //super(BitmapFactory.decodeResource(res, R.drawable.back));
            bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
                    bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        clearCache();
    }

    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */

    private static final int HARD_CACHE_CAPACITY = 10;

    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(
            HARD_CACHE_CAPACITY / 2, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else return false;
        }
    };

    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache = new ConcurrentHashMap<String, SoftReference<Bitmap>>(
            HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {

        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * 
     * @param bitmap The newly downloaded bitmap.
     */
    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the
     *        cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        // look from local file.
        Bitmap bitmap = getBitmapFromCacheFile(url);

        if (bitmap != null) {
            addBitmapToCache(url, bitmap);
        }
        return bitmap;
    }

    public static Bitmap createFromCache(String file) {
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            return BitmapFactory.decodeFile(file, options);
        } catch (Exception e) {
            return null;
        }
    }

    // Looks to see if an image is in the file system.
    public synchronized Bitmap getBitmapFromCacheFile(String url) {
        String hashedUrl = getMd5(url);
        FileInputStream fis = null;

        try {
            fis = mContext.openFileInput(hashedUrl);
            return BitmapFactory.decodeStream(fis);
        } catch (Exception e) {
            // Not there.
            return null;
        } catch (OutOfMemoryError e) {
            // Not there.
            System.gc();
            clearCache();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // Ignore.
                }
            }
        }
    }

    // prepare a image by the url
    public void prepareImage(String url) {
        if (getBitmapFromCache(url) == null) {
            new OnlyDownloadBitmapTask().execute(url);
        }
    }

    // MD5 hases are used to generate filenames based off a URL.
    public static String getMd5(String url) {
        if (!TextUtils.isEmpty(url) && mDigest != null) {
            mDigest.update(url.getBytes());
            return getHashString(mDigest);
        }
        return null;
    }

    private static String getHashString(MessageDigest digest) {
        if (digest == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        byte[] byteArray = digest.digest();
        if (byteArray == null) {
            return null;
        }

        for (byte b : byteArray) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }

        return builder.toString();
    }

    /**
     * Clears the image cache used internally to improve performance. Note
     * that for memory efficiency reasons, the cache will automatically be
     * cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }

}
