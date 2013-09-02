package com.secretlisa.lib.http;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 上传文件的类型
 * 
 * @author wyz
 * 
 */
public class FileItem {
	
	public static final String IMAGE_PNG = "image/png";
	
	public static final String IMAGE_JPG = "image/jpg";
	
	public static final String IMAGE_JPEG = "image/jpeg";
	
	public static final String IMAGE_GIF = "image/gif";
	
	public static final String IMAGE_BMP = "image/bmp";
	
	public static final String APPLICATION_DOC = "application/msword";
	
	public static final String APPLICATION_XLS = "application/vnd.ms-excel";
	
	public static final String AUDIO_MPEG = "audio/mpeg";
	
	public static final String AUDIO_WAV = "audio/x_wav";
	
	public static final String AUDIO_MP3 = "audio/mpeg";
	
	public static final String TEXT_PLAIN = "'text/plain";
	
	private File file;

	private String paramName;
	
	private String contentType;

	public FileItem(String path, String paramName) {
		this.file = new File(path);
		this.paramName = paramName;
	}
	
	public void seContentType(String contentType){
		this.contentType = contentType;
	}
	
	public String getName() {
		return file.getName();
	}

	public String getPath() {
		return file.getPath();
	}

	public String getParamName() {
		return this.paramName;
	}

	public String getContentType() {
		return contentType;
	}

	public byte[] getBytes() throws SecretLisaException {
		FileInputStream inputStream;
		byte[] content = null;
		ByteArrayOutputStream baos = null;
		BufferedInputStream bis = null;
		try {
			inputStream = new FileInputStream(file);
			baos = new ByteArrayOutputStream();
			bis = new BufferedInputStream(inputStream);
			byte[] buffer = new byte[1024];
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
			throw new SecretLisaException("找不到文件",
					SecretLisaException.NETWORK_BAD_FILE);
		}
		return content;
	}

}
