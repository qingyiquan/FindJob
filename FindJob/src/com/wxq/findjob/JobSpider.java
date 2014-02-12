package com.wxq.findjob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class JobSpider
{
  public static boolean hasKeyWords(String title, String[] keyWords)
  {
    if (keyWords == null) {
      return true;
    }

    title = title.toLowerCase();

    for (String keyword : keyWords) {
      keyword = keyword.toLowerCase();
      if (title.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  public static String getSendTime(String text) {
    int start = text.lastIndexOf(')') + 2;
    int end = text.indexOf("|", start);
    String mixString = text.substring(start, end);
    String[] mixArray = mixString.split(",");
    return mixArray[0];
  }

  public static List<Map<String, String>> getUrlHrefs(String baseUrl, String targetUrl, String[] keywords) {
    List data = new ArrayList();
    Document doc = null;
    try {
      doc = Jsoup.connect(targetUrl)
        //.header("X-Requested-With", "XMLHttpRequest")
        .userAgent("I just want a good job")
        .get();

      Elements lines = doc.getElementsByTag("li");
      int lineCount = lines.size();
      System.out.println("共抓取:" + lineCount + "条数据");
      if (lineCount == 0)
      {
        System.out.println(doc.toString());
        return data;
      }
      for (Element line : lines) {
        String lineText = line.text();
        String timeString = getSendTime(lineText);

        Elements links = line.getElementsByTag("a");
        Element link = links.get(0);
        String linkHref = link.attr("href");
        String linkText = link.text();
        if (baseUrl != null) {
          linkHref = baseUrl + linkHref;
        }
        if (hasKeyWords(linkText, keywords)) {
          Map item = new HashMap();
          item.put("url", linkHref);
          item.put("title", linkText);
          item.put("time", timeString);
          data.add(item);
        }
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return data;
  }

  public static List<Map<String, String>> getUrlHrefs(String baseUrl, String targetUrl, String[] keywords, int pageCount)
  {
    List data = new ArrayList();
    if ((pageCount <= 0) || (pageCount > 20)) {
      pageCount = 1;
    }
    for (int i = 0; i < pageCount; ++i) {
      List subData = getUrlHrefs(baseUrl, targetUrl + "?p=" + (i + 1), keywords);
      data.addAll(subData);
    }
    return data;
  }

  public static List<Map<String, String>> getUrlHrefs(String[] baseUrls, String[] targetUrls, String[] keywords, int[] pageCounts)
  {
    List data = new ArrayList();
    if (baseUrls.length != targetUrls.length) {
      return data;
    }

    if (baseUrls.length != pageCounts.length) {
      int[] fullCounts = new int[baseUrls.length];
      for (int i = 0; (i < pageCounts.length) && (i < fullCounts.length); ++i) {
        fullCounts[i] = pageCounts[i];
      }
      for (int i = pageCounts.length; i < fullCounts.length; ++i) {
        fullCounts[i] = 1;
      }
      pageCounts = fullCounts;
    }
    for (int i = 0; i < pageCounts.length; ++i) {
      String baseUrl = baseUrls[i];
      String targetUrl = targetUrls[i];
      int pageCount = pageCounts[i];

      List subData = getUrlHrefs(baseUrl, targetUrl, keywords, pageCount);
      data.addAll(subData);
    }
    return data;
  }

  public static void main(String[] args)
  {
    String[] KEYWORDS = { 
      "校园", "校招", "应届", "移动" , "android", "ios", "java" };

    List<Map<String, String>> list = getUrlHrefs(
      new String[] { "http://m.byr.cn", "http://m.newsmth.net" }, 
      new String[] { "http://m.byr.cn/board/JobInfo", "http://m.newsmth.net/board/Career_Campus" }, 
      KEYWORDS, 
      new int[] { 2, 2 });
    for (Map map : list)
      System.out.println((String)map.get("url") + "," + (String)map.get("title") + "," + (String)map.get("time"));
  }
}