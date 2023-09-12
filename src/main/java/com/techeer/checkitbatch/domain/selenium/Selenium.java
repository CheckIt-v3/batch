package com.techeer.checkitbatch.domain.selenium;

import com.techeer.checkitbatch.domain.book.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Selenium {
    private ChromeDriver driver;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashMap<String, String> crawlingMap;

    private static final String url = "http://www.yes24.com/main/default.aspx";
    private static final int CRAWLING_MAX_VALUE = 1000;
    private static final int CRAWLING_NEW_BOOK_VALUE = 100;

    public static List<Book> crawledBookList = new ArrayList<>();

    public List crawling() {
        log.info("*** 크롤링 시작 ***");

//        System.setProperty("webdriver.chrome.driver", "/Users/misis1/myProject/Techeer-Book/checkitbatch/src/main/java/com/techeer/checkitbatch/crawling/chromedriver");
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");

        ChromeOptions chromeOptions = new ChromeOptions();
        // websocket 허용
        chromeOptions.addArguments("--remote-allow-origins=*");
        chromeOptions.setHeadless(true);
        chromeOptions.addArguments("--lang=ko");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.setCapability("ignoreProtectedModeSettings", true);

        driver = new ChromeDriver(chromeOptions);
        log.info("크롬 버전 : " + driver.getCapabilities().getCapability("chrome").toString());
        log.info("드라이버 버전 : "+driver.getCapabilities().getBrowserVersion());

        try {
            setting();
            getAnotherCategories();
            log.info("크롤링 끝");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.close();
        driver.quit();
        log.info("*** 크롬 드라이버 종료 ***");
        return crawledBookList;
    }

    private void setting() throws InterruptedException {
        log.info("*** YES24 홈페이지로 이동 ***");
        Thread.sleep(500);
        driver.get(url);

        log.info("*** 광고 배너 제거 ***");
        try {
            driver.findElement(By.xpath("//*[@id=\"chk_info\"]")).click();

        } catch (Exception e) {
            log.info(e.getMessage());
            log.info("*** 광고 배너 제거 실패 ***");
        }

//        log.info("*** 국내 도서로 이동 ***");
//        WebElement koreanBooks = driver.findElement(By.xpath("//*[@id=\"ulCategoryList\"]/li[1]/a/em"));

        log.info("*** 신간 도서로 이동 ***");
        WebElement koreanBooks = driver.findElement(By.xpath("//*[@id=\"yesFixCorner\"]/dl/dd/ul[1]/li[2]/a"));
        koreanBooks.click();
    }



    private void getAnotherCategories() throws InterruptedException {
        Thread.sleep(500);
        WebElement categoryEle = driver.findElement(By.xpath("//*[@id=\"category\"]/ul"));
        List<WebElement> categories = categoryEle.findElements(By.tagName("li"));

        List<String> urlList = new ArrayList<>();

        for(WebElement category : categories) {
            WebElement aTag = category.findElement(By.tagName("a"));
            String url = aTag.getAttribute("href");
            urlList.add(url);
        }
        for(String url : urlList) {
            Thread.sleep(500);
            if(crawledBookList.size() >= CRAWLING_NEW_BOOK_VALUE) break;
            log.info(url);
            moveCategories(url);
        }
    }

    private void moveCategories(String url) throws InterruptedException {
        driver.get(url);
        Thread.sleep(500);

        List<String> urlList = new ArrayList<>();

        List<WebElement> bookList = driver.findElements(By.className("goodsTxtInfo"));
        List<String> bookUrlList = new ArrayList<>();
        for(WebElement book : bookList) {
            WebElement aTag = book.findElement(By.tagName("a"));
            String bookUrl = aTag.getAttribute("href");
            bookUrlList.add(bookUrl);
        }

        for(String bookUrl : bookUrlList) {
            String isCrawled = (String) redisTemplate.opsForValue().get("id:"+bookUrl);
            log.info("현재까지 크롤링 된 책 갯수 : "+crawledBookList.size()+ " ******* ");
            if(crawledBookList.size() >= CRAWLING_NEW_BOOK_VALUE) break;
            if(isCrawled != null && isCrawled.equals("crawled") || crawlingMap.containsKey(bookUrl)) {
                log.info("이미 크롤링 된 책입니다.");
            }
            else {
              crawlingMap.put(bookUrl, "crawled");
              getInfo(bookUrl);
            }
        }

    }

    private void getInfo(String bookUrl) throws InterruptedException {
        Thread.sleep(500);
        driver.get(bookUrl);

        try {
            String title = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[2]/div[1]/div/h2")).getText();
            String author = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[2]/div[1]/span[2]/span[1]")).getText();
            String publisher = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[2]/div[1]/span[2]/span[2]/a")).getText();

            // image url의 경로를 얻는 방법이 2가지 있음, 1번이 안되면 2번으로 얻는 로직
            boolean flag = driver.findElements(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[1]/div/span/em/img")).size() > 0;
            String coverImageUrl = "";
            if (flag) {
                coverImageUrl = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[1]/div/span/em/img")).getAttribute("src");
            }
            else {
                coverImageUrl = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[1]/div/div[2]/div/span[1]/em/img")).getAttribute("src");
            }

            String infoText = driver.findElement(By.xpath("//*[@id=\"infoset_specific\"]/div[2]/div/table/tbody")).getText();

            String[] bookSize = infoText.split("쪽수, 무게, 크기 ")[1].split("mm")[0].split("\\|");
            String[] size = {};

            if(bookSize.length > 2) size = bookSize[2].replace(" ","").split("\\*");
            else size = bookSize[1].replace(" ","").split("\\*");

            String pages = bookSize[0].split("쪽 ")[0];

            String width = size[0];
            String height = size[1];
            String thickness = size[2];

            String category = driver.findElement(By.xpath("//*[@id=\"infoset_goodsCate\"]/div[2]/dl[1]/dd/ul")).getText();

            LocalDateTime createdAt = LocalDateTime.now();

            Book book = Book.builder()
                    .title(title)
                    .author(author)
                    .publisher(publisher)
                    .coverImageUrl(coverImageUrl)
                    .pages(Integer.parseInt(pages))
                    .width(Integer.parseInt(width))
                    .height(Integer.parseInt(height))
                    .thickness(Integer.parseInt(thickness))
                    .category(category)
                    .createdAt(createdAt)
                    .build();

                log.info(title);
//                log.info(author);
//                log.info(publisher);
//                log.info(coverImageUrl);
//                log.info(pages);
//                log.info(width);
//                log.info(height);
//                log.info(thickness);
//                log.info(category);

                crawledBookList.add(book);

        } catch (Exception e) {
            log.info(e.getMessage());
            log.info("크롤링 또는 mongodb에 저장 실패");
        }

        try {
            driver.navigate().back();
        } catch (Exception e) {
            log.info(e.getMessage());
            Thread.sleep(5000);
            driver.navigate().back();
        }
    }

//    국내도서 크롤링 코드, 혹시 모르니 삭제 x
//    private void getAnotherCategories() throws InterruptedException {
//        Thread.sleep(500);
//        WebElement categoryEle = driver.findElement(By.xpath("//*[@id=\"mCateLi\"]"));
//        List<WebElement> categories = categoryEle.findElements(By.className("cate2d"));
//
//        List<String> urlList = new ArrayList<>();
//
//        for(WebElement category : categories) {
//            WebElement aTag = category.findElement(By.tagName("a"));
//            String url = aTag.getAttribute("href");
//            urlList.add(url);
//        }
//        for(String url : urlList) {
//            Thread.sleep(500);
//            if(crawledBookList.size() >= CRAWLING_MAX_VALUE) break;
//            moveCategories(url);
//        }
//    }
//
//    private void moveCategories(String url) throws InterruptedException {
//        driver.get(url);
//        Thread.sleep(500);
//
//        Boolean flag = driver.findElements(By.xpath("//*[@id=\"cateSubListWrap\"]")).size() > 0;
//        List<String> urlList = new ArrayList<>();
//
//        // 서브 카테고리가 있으면
//        if (flag) {
//            WebElement subCategoriesEle  = driver.findElement(By.xpath("//*[@id=\"cateSubListWrap\"]"));
//            int size = subCategoriesEle.findElements(By.tagName("dl")).size();
//            for(int i = 1; i <= size; i++) {
//                WebElement subCategory = driver.findElement(By.xpath("//*[@id=\"cateSubListWrap\"]/dl["+ i +"]/dt/a"));
//                String subCateUrl = subCategory.getAttribute("href");
//                urlList.add(subCateUrl);
//            }
//            for(String subUrl : urlList) {
//                if(crawledBookList.size() >= CRAWLING_MAX_VALUE) break;
//                moveCategories(subUrl);
//            }
//        }
//        else {
//            List<WebElement> bookList = driver.findElements(By.className("imgBdr"));
//            List<String> bookUrlList = new ArrayList<>();
//            for(WebElement book : bookList) {
//                WebElement aTag = book.findElement(By.tagName("a"));
//                String bookUrl = aTag.getAttribute("href");
//                bookUrlList.add(bookUrl);
//            }
//
//            for(String bookUrl : bookUrlList) {
//                String isCrawled = (String) redisTemplate.opsForValue().get("id:"+bookUrl);
//                if(crawledBookList.size() >= CRAWLING_MAX_VALUE) break;
//                if(isCrawled != null && isCrawled.equals("crawled") || crawlingMap.containsKey(bookUrl)) {
//                    log.info("이미 크롤링 된 책입니다.");
//                }
//                else {
//
////                    redisTemplate.opsForValue().set("id:" + bookUrl, "crawled");
//                    crawlingMap.put(bookUrl, "crawled");
//                    getInfo(bookUrl);
//                }
//            }
//
//        }
//    }

}
