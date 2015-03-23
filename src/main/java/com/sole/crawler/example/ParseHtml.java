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

public class ParseHtml {

	public static void fetchJs() throws Exception {
		//目标网页
		String url ="http://finance.sina.com.cn/data/#stock/";
		//模拟特定浏览器FIREFOX_3
		WebClient spider =new WebClient(BrowserVersion.CHROME);
		//获取目标网页
		Page page = spider.getPage(url);
		//打印网页内容
		System.out.println(page.getWebResponse().getContentAsString());//关闭所有窗口
        spider.closeAllWindows();
        }
	
	
	private static String fetchHtml(String url) {
		HttpRequester httpRequester = new HttpRequesterImpl();
		HttpResponse response = null;
		String charset="";
		try {
			response = httpRequester.getResponse(url);
			
			//response = httpRequester.getResponse("http://s.1688.com/caigou/offer_search.htm?spm=b26110225.7145030.0.0.dvaX8D&keywords=%BC%E0%BF%D8%C9%E3%CF%F1%BB%FA&n=y&from=industrySearch&industryFlag=jicai");
			//charset=CharsetDetector.guessEncoding(response.getContent());
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
			Elements elements = doc.getElementsByClass("sm-offerItem");
			String title="";
			for (Element element:elements) {
				int size = element.childNodeSize();
				System.out.println("***************************************************************");
				MonitorInfo monitor = new MonitorInfo();
				for (int i = 0; i < 5; i++) {
					
					Element liNode = element.child(i);
					//System.out.println(liNode.attr("class"));
					if("sm-offerItem-photo".equals(liNode.attr("class"))){
						title="图片信息：";
						System.out.println(title);
						Elements tags = liNode.getElementsByTag("img");
						for (Element tag: tags) {
							//if(tag.attr("href").equals("#")) continue;
							System.out.println("src==="+tag.select("img[src]").text());
							System.out.println("alt==="+tag.attr("alt"));
							monitor.setImgUrl(tag.select("img[src]").text());
						}
					}else if("sm-offerItem-title".equals(liNode.attr("class"))){
						title="产品信息：";
						System.out.println(title);
						Elements tags = liNode.getElementsByTag("a");
						for (Element tag: tags) {
							if(tag.attr("href").equals("#")) continue;
							System.out.println("href==="+tag.attr("href"));
							System.out.println("content==="+tag.text());
							monitor.setProductUrl(tag.attr("href"));
							monitor.setName(tag.text());
						}
					}else if("sm-offerItem-priceInfo".equals(liNode.attr("class"))){
						title="价格信息：";
						System.out.println(title);
						Elements tags = liNode.getElementsByClass("discount-tag");
						for (Element tag: tags) {
							//System.out.println("href==="+tag.attr("href"));
							System.out.println("discount==="+tag.text());
							System.out.println("price==="+tag.nextElementSibling().text());
							monitor.setPrice(tag.nextElementSibling().text());
						}
					}else if("sm-offerItem-alitalk".equals(liNode.attr("class"))){
						title="联系方式：";
						System.out.println(title);
						Elements tags = liNode.getElementsByTag("a");
						for (Element tag: tags) {
							if(tag.attr("href").equals("#")) continue;
							System.out.println("href==="+tag.attr("href"));
							System.out.println("content==="+tag.text());
							monitor.setCompany(tag.text());
							monitor.setWebSite(tag.attr("href"));
						}
					}else if("sm-offerItem-companyInfo".equals(liNode.attr("class"))){
						title="公司信息：";
						System.out.println(title);
						Elements tags = liNode.getElementsByClass("first");
						for (Element tag: tags) {
							System.out.println("address==="+tag.text());
							System.out.println("fund==="+tag.nextElementSibling().text());
							monitor.setAddress(tag.text());
						}
					}
				}
				monitorList.add(monitor);
			}
			return monitorList;
	}
	
	
	private static void saveData(String data) throws IOException {
 		String fileName="163.html";
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
		
//		String url = "http://news.163.com/"; 
//		String html = ParseHtml.fetchHtml(url);
//		ParseHtml.saveData(html);
		
		for (int i = 1; i <= 10; i++) {
			System.out.println("----------------------"+i+"------------------------");
			int page = i+3;
			String url = "http://s.1688.com/caigou/offer_search.htm?spm=b26110225.7145030.0.0.dvaX8D&keywords=%BC%E0%BF%D8%C9%E3%CF%F1%BB%FA&n=y&from=industrySearch&industryFlag=jicai#beginPage=";
			url = url + page;
			System.out.println("----------"+url);
			String html = ParseHtml.fetchHtml(url);
			ParseHtml.saveDB(ParseHtml.parse(html)); 
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		     
	}

}
