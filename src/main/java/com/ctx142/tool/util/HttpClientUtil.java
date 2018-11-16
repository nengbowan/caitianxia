package com.ctx142.tool.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class HttpClientUtil {
    public static String getOrPost(HttpRequestBase req , HttpClient client){
        try {
            HttpResponse httpResponse = client.execute(req);
            HttpEntity httpEntity = httpResponse.getEntity();
            String resp = EntityUtils.toString(httpEntity , Charset.defaultCharset());
            return resp;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
