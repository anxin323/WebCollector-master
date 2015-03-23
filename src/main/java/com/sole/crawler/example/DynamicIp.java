package com.sole.crawler.example;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 *
 * 自动更改IP地址反爬虫封锁
 *
 * ADSL拨号上网使用动态IP地址，每一次拨号得到的IP都不一样
 *
 * @author 杨尚川
 */
public class DynamicIp {
    private DynamicIp(){}
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicIp.class);
    private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String ENCODING = "gzip, deflate";
    private static final String LANGUAGE = "zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3";
    private static final String CONNECTION = "keep-alive";
    private static final String HOST = "192.168.0.1";
    private static final String REFERER = "http://192.168.0.1/login.asp";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:36.0) Gecko/20100101 Firefox/36.0";

    public static void main(String[] args) {
        toNewIp();
    }
    public static boolean toNewIp() {
        Map<String, String> cookies = login("username***", "password***", "phonenumber***");
        if("true".equals(cookies.get("success"))) {
            LOGGER.info("登陆成功");
            cookies.remove("success");
            while (!disConnect(cookies)) {
                LOGGER.info("断开连接失败，重试！");
            }
            LOGGER.info("断开连接成功");
            while (!connect(cookies)) {
                LOGGER.info("建立连接失败，重试！");
            }
            LOGGER.info("建立连接成功");
            LOGGER.info("自动更改IP地址成功！");
            return true;
        }
        return false;
    }
    public static boolean connect(Map<String, String> cookies){
        return execute(cookies, "3");
    }
    public static boolean disConnect(Map<String, String> cookies){
        return execute(cookies, "4");
    }
    public static boolean execute(Map<String, String> cookies, String action){
        String url = "http://192.168.0.1/goform/SysStatusHandle";
        Map<String, String> map = new HashMap<>();
        map.put("action", action);
        map.put("CMD", "WAN_CON");
        map.put("GO", "system_status.asp");
        Connection conn = Jsoup.connect(url)
                .header("Accept", ACCEPT)
                .header("Accept-Encoding", ENCODING)
                .header("Accept-Language", LANGUAGE)
                .header("Connection", CONNECTION)
                .header("Host", HOST)
                .header("Referer", REFERER)
                .header("User-Agent", USER_AGENT)
                .ignoreContentType(true)
                .timeout(30000);
        for(String cookie : cookies.keySet()){
            conn.cookie(cookie, cookies.get(cookie));
        }

        String title = null;
        try {
            Connection.Response response = conn.method(Connection.Method.POST).data(map).execute();
            String html = response.body();
            Document doc = Jsoup.parse(html);
            title = doc.title();
            LOGGER.info("操作连接页面标题："+title);
        }catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
        if("LAN | LAN Settings".equals(title)){
            //发出命令5秒之后再检查网络状态
            try{Thread.sleep(5000);}catch (Exception e){LOGGER.error(e.getMessage(), e);}
            if(("3".equals(action) && isConnected())
                    || ("4".equals(action) && !isConnected())){
                return true;
            }
        }
        return false;
    }
    public static boolean isConnected(){
        try {
            Document doc = Jsoup.connect("http://www.baidu.com/s?wd=杨尚川&t=" + System.currentTimeMillis())
                    .header("Accept", ACCEPT)
                    .header("Accept-Encoding", ENCODING)
                    .header("Accept-Language", LANGUAGE)
                    .header("Connection", CONNECTION)
                    .header("Referer", "https://www.baidu.com")
                    .header("Host", "www.baidu.com")
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .timeout(30000)
                    .get();
            LOGGER.info("搜索结果页面标题："+doc.title());
            if(doc.title() != null && doc.title().contains("杨尚川")){
                return true;
            }
        }catch (Exception e){
            if("Network is unreachable".equals(e.getMessage())){
                return false;
            }else{
                LOGGER.error("状态检查失败:"+e.getMessage(), e);
            }
        }
        return false;
    }
    public static Map<String, String> login(String userName, String password, String verify){
        try {
            Map<String, String> map = new HashMap<>();
            map.put("Username", userName);
            map.put("Password", password);
            map.put("checkEn", "0");
            Connection conn = Jsoup.connect("http://192.168.0.1/LoginCheck")
                    .header("Accept", ACCEPT)
                    .header("Accept-Encoding", ENCODING)
                    .header("Accept-Language", LANGUAGE)
                    .header("Connection", CONNECTION)
                    .header("Referer", REFERER)
                    .header("Host", HOST)
                    .header("User-Agent", USER_AGENT)
                    .ignoreContentType(true)
                    .timeout(30000);

            Connection.Response response = conn.method(Connection.Method.POST).data(map).execute();
            String html = response.body();
            Document doc = Jsoup.parse(html);
            LOGGER.info("登陆页面标题："+doc.title());
            Map<String, String> cookies = response.cookies();
            if(html.contains(verify)){
                cookies.put("success", Boolean.TRUE.toString());
            }
            LOGGER.info("*******************************************************cookies start:");
//            cookies.keySet().stream().forEach((cookie) -> {
//                LOGGER.info(cookie + ":" + cookies.get(cookie));
//            });
            LOGGER.info("*******************************************************cookies end:");
            return cookies;
        }catch (Exception e){
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }
}
