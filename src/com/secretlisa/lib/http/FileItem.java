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

	private File file;

	private String fileName;

	private String paramName;

	public FileItem(String path, String paramName) {
		this.file = new File(path);
		this.fileName = getName();
		this.paramName = paramName;
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
		String contentType = "image/jpg";
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".jpg"))
			contentType = "image/jpg";
		else if (fileName.endsWith(".png"))
			contentType = "image/png";
		else if (fileName.endsWith(".jpeg"))
			contentType = "image/jpeg";
		else if (fileName.endsWith(".gif"))
			contentType = "image/gif";
		else if (fileName.endsWith(".bmp"))
			contentType = "image/bmp";
		else
			throw new RuntimeException("不支持的文件类型'" + fileName + "'(或没有文件扩展名)");
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
