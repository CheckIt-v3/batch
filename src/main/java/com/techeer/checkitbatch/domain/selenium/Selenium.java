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
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Selenium {
    private ChromeDriver driver;
    private final RedisTemplate<String, String> redisTemplate;
    private final HashMap<String, String> crawlingMap;

    private static final String url = "http://www.yes24.com/main/default.aspx";
    private static final int CRAWLING_MAX_VALUE = 1000;
    private static final int CRAWLING_NEW_BOOK_VALUE = 10;

    public static List<Book> crawledBookList = new ArrayList<>();

    public List bookCrawling() {
        log.info("*** 크롤링 시작 ***");
        System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver"); // 도커 chromedriver 실행할 때

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
        log.info("드라이버 버전 : "+ driver.getCapabilities().getBrowserVersion());

        try {
            setting();
//            getAnotherCategories();
            getBestSellerBooks();
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

//        log.info("*** 신간 도서로 이동 ***");
//        WebElement koreanBooks = driver.findElement(By.xpath("//*[@id=\"yesFixCorner\"]/dl/dd/ul[1]/li[2]/a"));
        log.info("*** 베스트 도서로 이동 ***");
        WebElement koreanBooks = driver.findElement(By.xpath("//*[@id=\"yesFixCorner\"]/dl/dd/ul[1]/li[1]/a"));
        koreanBooks.click();
    }

    private void getBestSellerBooks() throws InterruptedException {
        boolean hasNextPageGroup = true;

        while (hasNextPageGroup) {
            // 현재 페이지 그룹의 모든 페이지 순회
            while (true) {
                // 현재 페이지에서 데이터 크롤링
                crawlCurrentPage();

                // 다음 페이지로 이동
                if (!goToNextPage()) {
                    break; // 다음 페이지가 없으면 페이지 그룹 종료
                }

                if (crawledBookList.size() >= CRAWLING_NEW_BOOK_VALUE) {
                    log.info("크롤링 최대 값에 도달했습니다. 작업을 종료합니다.");
                    return;
                }
            }

            // 다음 페이지 그룹으로 이동
            hasNextPageGroup = goToNextPageGroup();
            if (!hasNextPageGroup) {
                log.info("베스트셀러 전체를 크롤링 완료하였습니다."); // 페이지 그룹이 끝난 경우 로그 출력
            }
        }
    }

    private void crawlCurrentPage() throws InterruptedException {
        Thread.sleep(500);

        List<WebElement> bookList = driver.findElements(By.xpath("//*[@id='yesBestList']/li"));
        for (WebElement book : bookList) {
            try {
                WebElement aTag = book.findElement(By.xpath(".//span[@class='img_grp']/a"));
                String bookUrl = aTag.getAttribute("href");

                // 중복확인
                if (!isAlreadyCrawled(bookUrl)) {
                    getInfo(bookUrl);
                }

                if (crawledBookList.size() >= CRAWLING_NEW_BOOK_VALUE) {
                    log.info("크롤링 최대 값에 도달했습니다.");
                    return;
                }
            } catch (Exception e) {
                log.error("책 크롤링 중 오류 발생", e);
            }
        }
    }

    private boolean goToNextPage() {
        try {
            WebElement nextPage = driver.findElement(By.xpath("//a[@class='num']"));
            if (nextPage != null && nextPage.isDisplayed()) {
                nextPage.click();
                Thread.sleep(1000); // 페이지 로드 대기
                return true;
            }
        } catch (NoSuchElementException e) {
            log.info("다음 페이지 버튼을 찾을 수 없습니다. 페이지 그룹 종료.");
        } catch (Exception e) {
            log.warn("다음 페이지 이동 중 오류 발생: {}", e.getMessage());
        }
        return false;
    }

    private boolean goToNextPageGroup() {
        try {
            WebElement nextPageGroup = driver.findElement(By.xpath("//a[@class='bgYUI next']"));
            if (nextPageGroup != null && nextPageGroup.isDisplayed()) {
                nextPageGroup.click();
                Thread.sleep(1000); // 페이지 로드 대기
                return true;
            }
        } catch (NoSuchElementException e) {
            log.info("다음 페이지 그룹 버튼을 찾을 수 없습니다. 작업 종료.");
        } catch (Exception e) {
            log.warn("다음 페이지 그룹 이동 중 오류 발생: {}", e.getMessage());
        }
        return false;
    }

    private boolean isAlreadyCrawled(String bookUrl) {
        String isCrawled = redisTemplate.opsForValue().get("id:" + bookUrl);
        if (isCrawled != null && isCrawled.equals("crawled")) {
            log.info("이미 크롤링된 책: {}", bookUrl);
            return true;
        }
        crawlingMap.put(bookUrl, "crawled");
        return false;
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

                crawledBookList.add(book);

        } catch (Exception e) {
            log.info(e.getMessage());
            log.info("크롤링 또는 mysql 저장 실패");
        }

        try {
            driver.navigate().back();
        } catch (Exception e) {
            log.info(e.getMessage());
            Thread.sleep(5000);
            driver.navigate().back();
        }
    }
    // 신간 도서
//    private void getAnotherCategories() throws InterruptedException {
//        Thread.sleep(500);
//        WebElement categoryEle = driver.findElement(By.xpath("//*[@id=\"category\"]/ul"));
//        List<WebElement> categories = categoryEle.findElements(By.tagName("li"));
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
//            if(crawledBookList.size() >= CRAWLING_NEW_BOOK_VALUE) break;
//            log.info(url);
//            moveCategories(url);
//        }
//    }
//
//    private void moveCategories(String url) throws InterruptedException {
//        driver.get(url);
//        Thread.sleep(500);
//
//        List<String> urlList = new ArrayList<>();
//
//        List<WebElement> bookList = driver.findElements(By.className("goodsTxtInfo"));
//        List<String> bookUrlList = new ArrayList<>();
//        for(WebElement book : bookList) {
//            WebElement aTag = book.findElement(By.tagName("a"));
//            String bookUrl = aTag.getAttribute("href");
//            bookUrlList.add(bookUrl);
//        }
//
//        for(String bookUrl : bookUrlList) {
//            String isCrawled = (String) redisTemplate.opsForValue().get("id:"+bookUrl);
//            log.info("현재까지 크롤링 된 책 갯수 : "+crawledBookList.size()+ " ******* ");
//            if(crawledBookList.size() >= CRAWLING_NEW_BOOK_VALUE) break;
//            if(isCrawled != null && isCrawled.equals("crawled") || crawlingMap.containsKey(bookUrl)) {
//                log.info("이미 크롤링 된 책입니다.");
//            }
//            else {
//              crawlingMap.put(bookUrl, "crawled");
//              getInfo(bookUrl);
//            }
//        }
//
//    }

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
