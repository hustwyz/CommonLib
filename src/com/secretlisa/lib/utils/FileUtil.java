package com.secretlisa.lib.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.secretlisa.lib.CommonConfig;
import com.secretlisa.lib.R;

public class FileUtil {

	public static char[] hexChar = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * judge ExternalStorage is valid,show toast if is not valid
	 * 
	 * return true if valid
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSdcardValid(Context context) {
		String status = Environment.getExternalStorageState();
		boolean sdcardValid = true;
		if (!status.equals(Environment.MEDIA_MOUNTED)) {
			sdcardValid = false;
			int msg;
			if (status.equals(Environment.MEDIA_SHARED)) {
				msg = R.string.download_sdcard_busy_dlg_msg;
			} else {
				msg = R.string.download_no_sdcard_dlg_msg;
			}
			CommonUtil.showToast(context, msg);
		}
		return sdcardValid;
	}

	/**
	 * judge ExternalStorage is valid return true if valid
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isSdcardValid() {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return false;
		}
		return true;
	}

	/**
	 * get external storage directory path
	 * 
	 * @return
	 */
	public static String getExternalStorageDirectory() {
		String storagePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		if (!storagePath.endsWith(File.separator)) {
			storagePath = storagePath + File.separator;
		}
		return storagePath;
	}

	/**
	 * get folder path
	 * 
	 * @param folderPath
	 * @return
	 */
	public static String getFolderPath(final String folderPath) {
		if (createFolder(folderPath)) {
			return folderPath;
		} else {
			return null;
		}
	}

	/**
	 * create folder
	 * 
	 * @param folderPath
	 * @return
	 */
	public static boolean createFolder(final String folderPath) {
		if (TextUtils.isEmpty(folderPath)) {
			return false;
		}
		boolean success = false;
		try {
			File folder = new File(folderPath);
			if (folder.exists() && folder.isDirectory()) {
				success = true;
			} else {
				success = folder.mkdirs();
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return success;
	}

	/**
	 * create new file
	 * 
	 * @param path
	 * @return
	 */
	public static boolean createFile(final String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}
		File file = new File(path);
		if (file.exists() && file.isFile())
			return true;
		try {
			file.createNewFile();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * delete file
	 * 
	 * @param path
	 */
	public static final void deleteFile(final String path) {
		if (TextUtils.isEmpty(path))
			return;
		File file = new File(path);
		if (file.exists())
			file.delete();
	}

	/**
	 * delete file
	 * 
	 * @param path
	 */
	public static final void deleteFile(final File file) {
		if (file == null)
			return;
		if (file.exists())
			file.delete();
	}

	/**
	 * delete folder and the sub folder
	 * 
	 * @param path
	 */
	public static final void deleteFolder(final String path) {
		if (TextUtils.isEmpty(path))
			return;
		File file = new File(path);
		deleteFolder(file);
	}

	/**
	 * delete folder and the sub folder
	 * 
	 * @param root
	 */
	public static final void deleteFolder(final File root) {
		if (root == null)
			return;
		if (!root.exists())
			return;
		if (root.isDirectory()) {
			File[] files = root.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFolder(file);
				} else {
					deleteFile(file);
				}
			}
		}
		root.delete();
	}

	/**
	 * 
	 * return true if file exist , false or not
	 * 
	 * @param path
	 * @return
	 */
	public static boolean fileExist(final String path) {
		if (TextUtils.isEmpty(path))
			return false;
		File file = new File(path);
		if (file.exists() && file.isFile())
			return true;
		return false;
	}

	/**
	 * 文件夹是否存在
	 * 
	 * @param path
	 * @return
	 */
	public static boolean folderExist(final String path) {
		if (TextUtils.isEmpty(path))
			return false;
		File file = new File(path);
		if (file.exists() && file.isDirectory())
			return true;
		return false;
	}

	/**
	 * 获取文件的hash
	 * 
	 * @param fileName
	 * @param hashType
	 * @return
	 * @throws Exception
	 */
	public static String getHash(String fileName, String hashType) {
		try {
			InputStream fis;
			fis = new FileInputStream(fileName);// 读取文件
			byte[] buffer = new byte[1024];
			MessageDigest md5 = MessageDigest.getInstance(hashType);
			int numRead = 0;
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
			fis.close();
			return toHexString(md5.digest());
		} catch (Exception e) {

		}
		return null;
	}

	private static String toHexString(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 2);
		for (int i = 0; i < b.length; i++) {
			sb.append(hexChar[(b[i] & 0xf0) >>> 4]);
			sb.append(hexChar[b[i] & 0x0f]);
		}
		return sb.toString();
	}

	/**
	 * 解压文件
	 * 
	 * @param zipFile
	 *            要解压的文件
	 * @param outfolder
	 *            解压到的文件夹
	 */
	public static boolean unZipFile(String zipFile, String outFolder,
			UnZipCallback callback) {
		if (null == zipFile || null == outFolder)
			return false;
		if (zipFile.length() == 0)
			return false;
		try {
			long startTime = CommonUtil.getNowTime();
			String outFolderPath = outFolder;
			if (!outFolder.endsWith(File.separator)) {
				outFolderPath += File.separator;
			}
			File outPath = new File(outFolderPath);
			if (!outPath.exists() || !outPath.isDirectory()) {
				outPath.mkdirs();
			}
			ZipFile zfile = new ZipFile(zipFile);
			int size = zfile.size();
			Enumeration<? extends ZipEntry> zList = zfile.entries();
			byte[] buf = new byte[4096];
			int progress = 0;
			while (zList.hasMoreElements()) {
				ZipEntry ze = zList.nextElement();
				if (ze.isDirectory()) {
					File file = new File(outFolderPath + ze.getName());
					file.mkdirs();
					continue;
				}
				File outFile = new File(outFolderPath + ze.getName());
				if (!outFile.exists() && !outFile.isFile())
					outFile.createNewFile();
				OutputStream os = new BufferedOutputStream(
						new FileOutputStream(outFile));
				InputStream is = new BufferedInputStream(
						zfile.getInputStream(ze));
				int readLen = 0;
				while ((readLen = is.read(buf)) != -1) {
					os.write(buf, 0, readLen);
				}
				is.close();
				os.flush();
				os.close();
				if (callback != null) {
					callback.update(size, ++progress);
				}
			}
			zfile.close();
			long endTime = CommonUtil.getNowTime();
			if (CommonConfig.DEBUG)
				Log.d("FileUtil", "upzip file time:" + (endTime - startTime));
			return true;
		} catch (IOException e) {
			if (CommonConfig.DEBUG)
				Log.d("FileUtil", e.getMessage());
		}
		return false;
	}

}