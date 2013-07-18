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

package im.wyz.commonlib.utils;

import im.wyz.commonlib.R;
import im.wyz.commonlib.http.HttpRequest;
import im.wyz.commonlib.http.NetworkUtil;
import im.wyz.commonlib.http.Response;
import im.wyz.commonlib.http.SecretLisaException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

public abstract class BaseImageLoader {

	private static final String TAG = "ImageDownloaderBase";

	public static int NET_TYPE;

	private Context mContext;

	public BaseImageLoader(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void download(String url, ImageView imageView) {
		resetPurgeTimer();
		Bitmap bitmap = getBitmapFromCache(url);

		if (bitmap == null) {
			Log.v(TAG, "bitmap is null,download from web");
			forceDownload(url, imageView);
		} else {
			Log.v(TAG, "bitmap is not null");
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
			BitmapDownloaderTask task = null;
			task = new BitmapDownloaderTask(imageView);
			DownloadedDrawable downloadedDrawable = new DownloadedDrawable(
					imageView.getResources(), task);
			imageView.setImageDrawable(downloadedDrawable);
			// imageView.setBackgroundColor(Color.WHITE);
			imageView.setBackgroundResource(R.drawable.wallpaper_bg);
			imageView.setMinimumHeight(156);
			task.execute(url);
		}
	}

	private boolean cancelPotentialDownload(String url,
			ImageView imageView) {
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

	private BitmapDownloaderTask getBitmapDownloaderTask(
			ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof DownloadedDrawable) {
				DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	private Bitmap downloadBitmap(final String imageUrl) {
		// AndroidHttpClient is not allowed to be used from the main thread
		final HttpClient client = new DefaultHttpClient();
		// 代理支持
		int netType = NetworkUtil.getNetworkType(mContext);
		if (netType == NetworkUtil.TYPE_WAP) {
			String proxyHost = android.net.Proxy.getDefaultHost();
			if (proxyHost != null) {
				HttpHost proxy = new HttpHost(proxyHost,
						android.net.Proxy.getDefaultPort());
				client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
						proxy);
			}
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
				try {
					final byte[] respBytes = getBytes(entity.getContent());
					writeImageFile(imageUrl, respBytes);
					// Decode the bytes and return the bitmap.
					return BitmapFactory.decodeByteArray(respBytes, 0,
							respBytes.length, null);
				} finally {
					entity.consumeContent();
				}

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
		} finally {
		}
		return null;
	}

	private Bitmap downloadBitmapFromWeb(String imageUrl) {
		HttpRequest request = new HttpRequest(mContext);
		Response response = null;
		try {
			response = request.httpRequest(imageUrl, "GET", null, null);
			final byte[] respBytes = getBytes(response.getInputStream());
			writeImageFile(imageUrl, respBytes);
			// Decode the bytes and return the bitmap.
			return BitmapFactory.decodeByteArray(respBytes, 0,
					respBytes.length, null);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				response.disconnect();
			}
		}
		return null;
	}

	public byte[] getBytes(InputStream bis) throws SecretLisaException {
		byte[] content = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int length = 0;
			while ((length = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, length);
			}
			content = baos.toByteArray();
			if (content.length == 0) {
				content = null;
			}
			baos.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	private synchronized void writeImageFile(final String imageUrl,
			final byte[] respBytes) throws IOException {
		String hashedUrl = CommonUtil.md5(imageUrl);
		FileOutputStream fos = null;
		try {
			if (FileUtil.isSdcardValid()) {
				Log.d(TAG, "sdcard valid");
				fos = new FileOutputStream(
						new File(
								FileUtil.getFolderPath(getCachePath())
										+ hashedUrl));
			} else {
				Log.d(TAG, "sdcard not valid");
				fos = mContext.openFileOutput(hashedUrl, Context.MODE_PRIVATE);
			}
			fos.write(respBytes);
		} catch (FileNotFoundException e) {
			Log.w(TAG, "Error writing to bitmap cache: " + imageUrl, e);
		} catch (IOException e) {
			Log.w(TAG, "Error writing to bitmap cache: " + imageUrl, e);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
		private String imageUrl;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		@Override
		protected void onPreExecute() {
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
				// download from web server
				if (bitmap == null) {
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD) {
						bitmap = downloadBitmap(imageUrl);
					} else {
						bitmap = downloadBitmapFromWeb(imageUrl);
					}
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
				if (imageView != null)
					imageView.setBackgroundDrawable(null);
				BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
				if ((this == bitmapDownloaderTask) /* || (mode != Mode.CORRECT) */) {
					if (imageView != null) {
						imageView.setImageBitmap(bitmap);
					}
				}
			}
		}
	}

	/**
	 * A fake Drawable that will be attached to the imageView while the download
	 * is in progress.
	 * 
	 * <p>
	 * Contains a reference to the actual download task, so that a download task
	 * can be stopped if a new binding is required, and makes sure that only the
	 * last started download process can bind its result, independently of the
	 * download finish order.
	 * </p>
	 */
	class DownloadedDrawable extends BitmapDrawable {

		private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

		public DownloadedDrawable(Resources res,
				BitmapDownloaderTask bitmapDownloaderTask) {
			// super(BitmapFactory.decodeResource(res, R.drawable.back));
			bitmapDownloaderTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapDownloaderTaskReference.get();
		}
	}

	/*
	 * Cache-related fields and methods.
	 * 
	 * We use a hard and a soft cache. A soft reference cache is too
	 * aggressively cleared by the Garbage Collector.
	 */

	private static final int HARD_CACHE_CAPACITY = 10;

	private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

	// Hard cache, with a fixed maximum capacity and a life duration
	private final HashMap<String, Bitmap> sHardBitmapCache = new LinkedHashMap<String, Bitmap>(
			HARD_CACHE_CAPACITY / 2, 0.75f, true) {

		@Override
		protected boolean removeEldestEntry(
				LinkedHashMap.Entry<String, Bitmap> eldest) {
			if (size() > HARD_CACHE_CAPACITY) {
				// Entries push-out of hard reference cache are transferred to
				// soft reference cache
				sSoftBitmapCache.put(eldest.getKey(),
						new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
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
	 * @param bitmap
	 *            The newly downloaded bitmap.
	 */
	private void addBitmapToCache(String url, Bitmap bitmap) {
		if (bitmap != null) {
			synchronized (sHardBitmapCache) {
				sHardBitmapCache.put(url, bitmap);
			}
		}
	}

	/**
	 * @param url
	 *            The URL of the image that will be retrieved from the cache.
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

		Log.d(TAG, "look from local file:" + url);
		// look from local file.
		Bitmap bitmap = getBitmapFromCacheFile(url);

		if (bitmap != null) {
			addBitmapToCache(url, bitmap);
		}
		return bitmap;
	}

	// Looks to see if an image is in the file system.
	public synchronized Bitmap getBitmapFromCacheFile(String url) {
		String hashedUrl = CommonUtil.md5(url);
		Log.d(TAG, "file md5 = " + hashedUrl);
		FileInputStream fis = null;
		try {
			if (FileUtil.isSdcardValid()
					&& FileUtil.fileExist(FileUtil
							.getFolderPath(getCachePath())
							+ hashedUrl)) {
				Log.d(TAG, "look from external storage");
				fis = new FileInputStream(
						new File(
								FileUtil.getFolderPath(getCachePath())
										+ hashedUrl));
			} else {
				Log.d(TAG, "look from internal storage");
				fis = mContext.openFileInput(hashedUrl);
			}
			return BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			Log.e(TAG, "error", e);
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

	/**
	 * Clears the image cache used internally to improve performance. Note that
	 * for memory efficiency reasons, the cache will automatically be cleared
	 * after a certain inactivity delay.
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

	
	protected abstract String getCachePath();
}
