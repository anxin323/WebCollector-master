package com.sole.crawler.example;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.jdbc.core.JdbcTemplate;

import cn.edu.hfut.dmic.webcollector.net.HttpRequester;
import cn.edu.hfut.dmic.webcollector.net.HttpRequesterImpl;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.util.CharsetDetector;
import cn.edu.hfut.dmic.webcollector.util.JDBCHelper;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.sole.crawler.vo.MonitorInfo;

public class Parse58 {

	
	private static String fetchHtml(String url) {
		HttpRequester httpRequester = new HttpRequesterImpl();
		HttpResponse response = null;
		String charset="";
		try {
			response = httpRequester.getResponse(url);
			charset=CharsetDetector.guessEncoding(response.getContent());
			charset=new String(response.getContent(),charset);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return charset;
	}
	
	private static List<MonitorInfo> parse(String html) {
			Document doc = Jsoup.parse(html);
			List<MonitorInfo> monitorList = new ArrayList<MonitorInfo>();
			Elements elements = doc.getElementsByAttributeValue("class", "t qj-rentd");
			String title="";
			int index = 0;
			for (Element element:elements) {
				++index;
				int size = element.childNodeSize();
				System.out.println("******************************index="+index+"*********************************");
				Elements nodes = element.getElementsByTag("a");
				for(Element node:nodes){
					System.out.println("href==="+node.attr("href"));
					System.out.println("node==="+node.text());
					//continue;
					
				}
				System.out.println("price==="+element.nextElementSibling().text());
//				monitorList.add(monitor);
			}
			return monitorList;
	}
	
	
	private static void saveData(String data, String fileName) throws IOException {
 		//String fileName="163.html";
 		File out = new File(
 				"F:\\others\\sougou\\"+fileName);
 		if (!out.exists())
 			out.createNewFile();
 		BufferedWriter bw = new BufferedWriter(new FileWriter(out,true));
 		bw.write(data);
 		if (bw != null) {
 			bw.close();
 			bw = null;
 		}
 	}
	
	private static void saveDB(List<MonitorInfo> monitorList){
		JdbcTemplate jdbcTemplate = null;
		 try {
	            jdbcTemplate = JDBCHelper.createMysqlTemplate("mysql",
	                    "jdbc:mysql://localhost/test?useUnicode=true&characterEncoding=utf8",
	                    "root", "root", 5, 30);

	            //创建数据表
	            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tb_alibaba ("
	                    + "id int(11) NOT NULL AUTO_INCREMENT,"
	                    + "name varchar(100),product_url varchar(100),address varchar(200),"
	                    + "company varchar(100),price varchar(20),img_url varchar(100),website varchar(100),"
	                    + "PRIMARY KEY (id)"
	                    + ") ENGINE=MyISAM DEFAULT CHARSET=utf8;");
	            System.out.println("成功创建数据表 tb_alibaba");
	        } catch (Exception ex) {
	            jdbcTemplate = null;
	            System.out.println("mysql未开启或JDBCHelper.createMysqlTemplate中参数配置不正确!");
	        }
		 
		 for(MonitorInfo monitor:monitorList){
		 /*将数据插入mysql*/
	        if (jdbcTemplate != null) {
	            int updates=jdbcTemplate.update("insert into tb_alibaba (name,product_url,address,company,price,img_url,website) value(?,?,?,?,?,?,?)",
	            		monitor.getName(),monitor.getProductUrl(),monitor.getAddress(),monitor.getCompany(),monitor.getPrice(),monitor.getImgUrl(),monitor.getWebSite());
	            if(updates==1){
	                System.out.println("mysql插入成功");
	            }
	        }
		 }
	}

	public static void main(String[] args) throws IOException {
//		String url = "http://sz.58.com/chuzu/?PGTID=14270973093080.2821234096772969&ClickID=1";
//		String html = Parse58.fetchHtml(url);
//		Parse58.parse(html);
//		//ParseWangYiYun.saveData(html,"58.html");
//		System.out.println("download success.");
	
		for (int i = 1; i <= 3; i++) {
			System.out.println("----------------------"+i+"------------------------");
			int page = i;
			String url = "http://sz.58.com/chuzu/?PGTID=14270973093080.2821234096772969&ClickID=";
			url = url + page;
			System.out.println("----------"+url);
			String html = Parse58.fetchHtml(url);
			Parse58.parse(html);
			//Parse58.saveDB(Parse58.parse(html)); 
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		     
	}
}
