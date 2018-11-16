package com.ctx142.tool.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ctx142.tool.BalanceDto;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetController {
    private String baseUrl = "http://ctx142.com/";
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

    private String username;
    private String passwd;
    private String checkCode; //登录验证码

    public BetController(String username, String passwd) {
        this.username = username;
        this.passwd = passwd;
    }

    public String getVerfiyPath() {
        String userHome = System.getProperty("user.dir");
        String checkCodeUrl = baseUrl + "caiTianXiaLoginWeb/app/checkCode/image?7";

        HttpGet checkCodeGet = new HttpGet(checkCodeUrl);
        checkCodeGet.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:63.0) Gecko/20100101 Firefox/63.0"));
        checkCodeGet.addHeader(new BasicHeader("Referer", baseUrl + "caiTianXiaLoginWeb/app/home?ref=e90e81"));

        try {
            HttpResponse response = this.client.execute(checkCodeGet);
            HttpEntity entity = response.getEntity();
            InputStream inputStream = entity.getContent();
            File targetFile = new File(userHome + "/" + System.currentTimeMillis() + ".png");
            FileUtils.copyToFile(inputStream, targetFile);
            return targetFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getVerifyCode(String respXml) {

        try {

            SAXReader reader = new SAXReader();


            Document doc = reader.read(new ByteArrayInputStream(respXml
                    .getBytes("UTF8")));

            if (doc.getRootElement().element("Result") == null) {
                System.out.print("验证码识别失败");
                System.exit(0);
            }

            Element rootEle = doc.getRootElement().element("Result");

            if (rootEle != null) {
                return rootEle.getText();
            } else {
                return null;
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run() {
        //保存验证码
        String path = getVerfiyPath();

        //调用模块识别验证码
        String xmlResp = RuoKuai.createByPost(
                RuoKuai.username, RuoKuai.password,
                RuoKuai.typeId, RuoKuai.timeout,
                RuoKuai.softId, RuoKuai.softKey,
                path);

        this.checkCode = getVerifyCode(xmlResp);

        //登录
        login();

        //获取余额
        BalanceDto balanceDto = getBalanceDto();
        System.out.println("余额:"+balanceDto.getBalance());
        //下注 一直下庄 投注满1000 退出账号
        doBet();


        //退出
        logout();
        return;


    }

    private BalanceDto getBalanceDto() {
        String getBalanceUrl = baseUrl + "caiTianXiaLoginWeb/app/getBalance?5186.780782474535";
        HttpPost getBalancePost = new HttpPost(getBalanceUrl);
        getBalancePost.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:63.0) Gecko/20100101 Firefox/63.0"));
        getBalancePost.addHeader(new BasicHeader("Referer", baseUrl + "caiTianXiaLoginWeb/app/electronicGameBYW?ref=e90e81"));
        getBalancePost.addHeader(new BasicHeader("Content-Type", "application/json"));

        Map<String, String> postJsonMap = new HashMap<>();
        postJsonMap.put("pNetwork", "MAIN_WALLET");

        getBalancePost.setEntity(new StringEntity(JSON.toJSONString(postJsonMap), Charset.defaultCharset()));
        String getBalanceStr = HttpClientUtil.getOrPost(getBalancePost, client);
        return JSONObject.parseObject(getBalanceStr, BalanceDto.class);
    }

    private void doBet() {
    }

    private void login() {

        String loginUrl = baseUrl + "caiTianXiaLoginWeb/app/loginVerification?7577.517596599003";
        HttpPost loginUrlPost = new HttpPost(loginUrl);
        loginUrlPost.addHeader(new BasicHeader("Content-Type", "application/json"));
        loginUrlPost.addHeader(new BasicHeader("Referer", baseUrl + "caiTianXiaLoginWeb/app/home?ref=e90e81"));
        loginUrlPost.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:63.0) Gecko/20100101 Firefox/63.0"));


        //{"txtLoginCaptcha":"1711","txtLoginUsername":"freebuf002","txtLoginPassword":"cai795130","txtRememberUser":""}

        Map<String, String> nvps = new HashMap<>();
        nvps.put("txtLoginCaptcha", checkCode);
        nvps.put("txtLoginUsername", username);
        nvps.put("txtLoginPassword", passwd);
        nvps.put("txtRememberUser", "");

        String postParam = JSON.toJSONString(nvps);
        loginUrlPost.setEntity(new StringEntity(postParam, Charset.defaultCharset()));
        HttpClientUtil.getOrPost(loginUrlPost, client);
    }

    public void logout() {
        String logoutUrl = baseUrl + "caiTianXiaLoginWeb/app/logout";
        HttpPost logoutPost = new HttpPost(logoutUrl);
        logoutPost.addHeader(new BasicHeader("Content-Type", "application/json"));
        logoutPost.addHeader(new BasicHeader("Referer", baseUrl + "caiTianXiaLoginWeb/app/home?l=0"));
        logoutPost.addHeader(new BasicHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:63.0) Gecko/20100101 Firefox/63.0"));
        logoutPost.setEntity(new StringEntity("{}", Charset.defaultCharset()));
        HttpClientUtil.getOrPost(logoutPost, client);
        System.out.println("用户已退出!!!");
    }
}
