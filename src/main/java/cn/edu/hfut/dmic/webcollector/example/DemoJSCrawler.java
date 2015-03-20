/*
 * Copyright (C) 2014 hu
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package cn.edu.hfut.dmic.webcollector.example;

import cn.edu.hfut.dmic.webcollector.crawler.DeepCrawler;
import cn.edu.hfut.dmic.webcollector.model.Links;
import cn.edu.hfut.dmic.webcollector.model.Page;

import com.gargoylesoftware.htmlunit.BrowserVersion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * 如果爬虫需要抽取Javascript生成的数据，可以使用HtmlUnitDriver
 * HtmlUnitDriver可以用page.getDriver来生成
 * @author hu
 */
public class DemoJSCrawler extends DeepCrawler{

    public DemoJSCrawler(String crawlPath) {
        super(crawlPath);
    }

    @Override
    public Links visitAndGetNextLinks(Page page) {
        /*HtmlUnitDriver可以抽取JS生成的数据*/
        HtmlUnitDriver driver=page.getDriver(BrowserVersion.CHROME);
        /*HtmlUnitDriver也可以像Jsoup一样用CSS SELECTOR抽取数据
          关于HtmlUnitDriver的文档请查阅selenium相关文档*/
        System.out.println(driver.getPageSource());
//        List<WebElement> divInfos=driver.findElementsByCssSelector("h3>a[id^=uigs]");
//        for(WebElement divInfo:divInfos){
//            System.out.println(page.getUrl()+":divInfo="+divInfo.getText());
//            try {
//				saveData(page.getUrl()+":divInfo="+divInfo.getText()+"\n");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
        return null;
    }
    
 	private static void saveData(String data) throws IOException {
 		String fileName="data-sina.txt";
 		File out = new File(
 				"F:\\others\\sougou\\"+fileName);
 		if (!out.exists())
 			out.createNewFile();
 		BufferedWriter bw = new BufferedWriter(new FileWriter(out,true));
// 		bw.append(data);
 		bw.write(data);
// 		bw.newLine();
 		if (bw != null) {
 			bw.close();
 			bw = null;
 		}
 	}
    
    public static void main(String[] args) throws Exception{
        DemoJSCrawler crawler=new DemoJSCrawler("F:\\others\\sogou");
        for(int page=1;page<=5;page++)
//        crawler.addSeed("http://www.sogou.com/web?query="+URLEncoder.encode("编程")+"&page="+page);
        crawler.addSeed("http://finance.sina.com.cn/data/#stock/");
        crawler.start(1);
    }
    
    

    
}
