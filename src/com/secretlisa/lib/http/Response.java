package com.secretlisa.lib.http;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.secretlisa.lib.utils.Log;

public class Response {

    private String responseAsString = null;

    private InputStream is = null;

    private HttpURLConnection con;

	private int statusCode;

    public Response(HttpURLConnection con) throws IOException {
        this.con = con;
        this.is = con.getInputStream();
        String encoding = con.getContentEncoding();
        this.statusCode = con.getResponseCode();
        if (null != encoding && encoding.toLowerCase().indexOf("gzip") > -1) {
            this.is = new GZIPInputStream(this.is);
        }
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public HttpURLConnection getCon() {
		return con;
	}

    public Map<String, List<String>> getAllHeaders() {
        if (con != null) {
            return con.getHeaderFields();
        } else {
            return null;
        }
    }

    public String getHeader(String key) {
        if (con != null) {
            return con.getHeaderField(key);
        } else {
            return null;
        }
    }

    public InputStream getInputStream() {
        return is;
    }

    public String getString() throws SecretLisaException {
        if (null == responseAsString) {
            BufferedReader br;
            try {
                InputStream stream = getInputStream();
                if (null == stream) {
                    return null;
                }
                br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuffer buf = new StringBuffer();
                String line;
                while (null != (line = br.readLine())) {
                    buf.append(line).append("\n");
                }
                this.responseAsString = buf.toString().trim();
                Log.d("Response", responseAsString);
                stream.close();
            } catch (IOException ioe) {
                throw new SecretLisaException(ioe.getMessage(), ioe);
            }
        }
        return responseAsString;
    }

    public JSONObject asJSONObject() throws SecretLisaException {
        try {
            return new JSONObject(getString());
        } catch (JSONException jsone) {
            throw new SecretLisaException(jsone.getMessage() + ":" + this.responseAsString, jsone);
        }
    }

    public JSONArray asJSONArray() throws SecretLisaException {
        try {
            return new JSONArray(getString());
        } catch (Exception jsone) {
            throw new SecretLisaException(jsone.getMessage() + ":" + this.responseAsString, jsone);
        }
    }

    public void disconnect() {
        if (con != null) {
            con.disconnect();
        }
    }

}
