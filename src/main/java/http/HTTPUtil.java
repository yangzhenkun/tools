package http;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import thread.ThreadPoolManager;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author yangzhenkun
 * @create 2018-10-16 10:48
 */
public class HTTPUtil {

    private static WebClient webClient = null;

    private static ExecutorService pool = ThreadPoolManager.createThreadPoolExecutor("getHTML", 16, 32, 2048);

    static {
        webClient = new WebClient(BrowserVersion.FIREFOX_60);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        webClient.getCookieManager().setCookiesEnabled(true);
    }

    public static String httpGet(String url, String[] params, HttpHost host) throws Exception {
        HttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);


        // 设置请求头信息，鉴权
        httpGet.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
        // 设置配置请求参数
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 连接主机服务超时时间
                .setConnectionRequestTimeout(35000)// 请求超时时间
                .setSocketTimeout(60000).setProxy(host)// 数据读取超时时间
                .build();
        // 为httpGet实例设置配置
        httpGet.setConfig(requestConfig);
        HttpResponse response = null;
        response = client.execute(httpGet);
        HttpEntity httpEntity = response.getEntity();
        return EntityUtils.toString(httpEntity);
    }


    public static String httpPost(String url, Map<String, String> params, HttpHost host) throws Exception {
        HttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> reqParams = null;
        if (params != null && !params.isEmpty()) {
            reqParams = new ArrayList<NameValuePair>();
            for (Map.Entry<String, String> e : params.entrySet()) {
                reqParams.add(new BasicNameValuePair(e.getKey(), e.getValue()));
            }
        }

        if (reqParams != null) {
            httpPost.setEntity(new UrlEncodedFormEntity(reqParams, "UTF-8"));
        }

        // 设置请求头信息，鉴权
        httpPost.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
        // 设置配置请求参数
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(3000)// 连接主机服务超时时间
                .setConnectionRequestTimeout(3000)// 请求超时时间
                .setSocketTimeout(3000);

        if (host != null) {
            builder.setProxy(host);
        }

        RequestConfig requestConfig = builder.build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        HttpResponse response = null;
        response = client.execute(httpPost);
        HttpEntity httpEntity = response.getEntity();
        return EntityUtils.toString(httpEntity);
    }


    public static byte[] getImg(String url, HttpHost host) throws Exception {

        HttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        // 设置请求头信息，鉴权
        httpGet.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
//        httpGet.setHeader("Accept", "*/*");
//        httpGet.setHeader("Accept-Encoding", "gzip,deflate");
//        httpGet.setHeader("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
//        httpGet.setHeader("Connection", "keep-alive");
//        httpGet.setHeader("Host", "thumb.cjsapp.com");
//        httpGet.setHeader("Referer", "http://www.chejiaosuo.com/backstage/Psource/Search");

        // 设置配置请求参数
        RequestConfig.Builder builder = RequestConfig.custom().setConnectTimeout(3000)// 连接主机服务超时时间
                .setConnectionRequestTimeout(3000)// 请求超时时间
                .setSocketTimeout(3000);

        if (host != null) {
            builder.setProxy(host);
        }

        RequestConfig requestConfig = builder.build();
        // 为httpGet实例设置配置
        httpGet.setConfig(requestConfig);
        HttpResponse response = null;
        response = client.execute(httpGet);
        HttpEntity httpEntity = response.getEntity(); //4、获取实体

        if (httpEntity != null) {
            InputStream inputStream = httpEntity.getContent();
            return IOUtils.toByteArray(inputStream);

        }
        return null;
    }


    public static HtmlPage getHTML(String url, HttpHost host, String cookieStr) throws TimeoutException {

        HtmlCallable htmlCallable = new HtmlCallable(webClient, url, host, cookieStr);

        FutureTask<HtmlPage> task = new FutureTask<>(htmlCallable);

        pool.execute(task);

        HtmlPage htmlPage = null;

        try {
            htmlPage = task.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (htmlPage == null) {
            throw new TimeoutException();
        }

        return htmlPage;

    }


    @Getter
    @AllArgsConstructor
    private static class HtmlCallable implements Callable {

        private WebClient webClient;
        private String url;
        private HttpHost host;
        private String cookie;


        @Override
        public Object call() throws Exception {
            WebRequest request = new WebRequest(new URL(url));
            if (StringUtils.isNotBlank(cookie)) {
                request.setAdditionalHeader("Cookie", cookie);
            }
            if (host != null) {
                request.setProxyHost(host.getHostName());
                request.setProxyPort(host.getPort());
            }

            return webClient.getPage(request);
        }
    }


}
