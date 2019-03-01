package com.hyanzz.utils;

import com.hyanzz.domain.MyHttpClientResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Administrator on 2019/2/19.
 */
@Slf4j
public class MyHttpClientUtils {

    private static PoolingHttpClientConnectionManager cm;
    private static SSLConnectionSocketFactory socketFactory;
    private static RequestConfig requestConfig;

    private static final String EMPTY_STR = "";
    private static final String CHARSET = "UTF-8";

    // 初始化PoolingHttpClientConnectionManager
    private static void init() {
        if (cm == null) {

            Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE).register("https", socketFactory).build();

            cm = new PoolingHttpClientConnectionManager(r);
            cm.setMaxTotal(200);// 整个连接池最大连接数
            cm.setDefaultMaxPerRoute(20);// 每路由最大连接数

            // 默认的配置
            RequestConfig.Builder configBuilder = RequestConfig.custom();
            configBuilder.setConnectTimeout(5000);
            configBuilder.setSocketTimeout(10000);
            configBuilder.setConnectionRequestTimeout(10000);
            requestConfig = configBuilder.build();
        }
    }

    /**
     * 通过连接池获取HttpClient： CloseableHttpClient
     *
     * @return
     */
    private static CloseableHttpClient getHttpClient() {
        return HttpClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig).build();
    }


    /**
     * 处理需要参数的post请求，json参数
     *
     * @param url               请求地址
     * @param jsonParams        json参数
     * @param connTimeOutSecond 连接超时时间：秒，一般1-2秒
     * @param readTimeOutSecond 读取超时时间：秒，一般2-5秒
     *
     * @return 请求返回值
     *
     * @throws URISyntaxException
     */
    public static MyHttpClientResult httpPostRequest(String url, String jsonParams, int connTimeOutSecond, int readTimeOutSecond)
            throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (jsonParams != null && jsonParams.trim().length() > 0) {
            StringEntity entity = new StringEntity(jsonParams, ContentType.APPLICATION_JSON);
            entity.setContentEncoding(CHARSET);
            httpPost.setEntity(entity);
        }
        return getResult(httpPost, connTimeOutSecond, readTimeOutSecond);
    }

    /**
     * 处理Http get/post请求
     *
     * @param request
     *
     * @return
     */
    private static MyHttpClientResult getResult(HttpRequestBase request, int connTimeOutSecond, int readTimeOutSecond) throws Exception {
        CloseableHttpClient httpClient = getHttpClient();
        MyHttpClientResult rstObj = new MyHttpClientResult();
        try {

            // 设置超时时间
            if (connTimeOutSecond < 1)
                connTimeOutSecond = 1;
            if (readTimeOutSecond < 1)
                readTimeOutSecond = 1;

            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(connTimeOutSecond * 1000)
                        .setSocketTimeout(readTimeOutSecond * 1000).build();
            request.setConfig(requestConfig);

            log.debug("request url: {} ", request.toString());
            CloseableHttpResponse response = httpClient.execute(request);

            rstObj.setCode(response.getStatusLine().getStatusCode());
            log.debug("response statusLine: {}", response.getStatusLine().toString());

            HttpEntity entity;
            String result = EMPTY_STR;
            try {
                entity = response.getEntity();
                if (entity != null) {
                    log.debug("resultContentLength: {}", entity.getContentLength());// -1 表示长度未知
                    result = EntityUtils.toString(entity, CHARSET);// 设置为utf-8编码，否则服务器端没有设置编码的话会出现中文乱码
                }
            } finally {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException e) {
                    log.error("response error ", e);
                }
            }

            log.info("request url: {}，response result: {} ", request.toString(), result == null ? "" : (result.trim().length() > 100 ? result
                    .trim().subSequence(0, 100) : result.trim()));
            rstObj.setResult(result);
            return rstObj;
        } catch (ClientProtocolException e) {
            log.error("getResult error: {}", e.toString());
            throw e;
        } catch (IOException e) {
            log.error("getResult error: {}", e.toString());
            throw e;
        } finally {
            // httpClient.close(); //TODO 需要手动关闭吗？
        }
    }
}