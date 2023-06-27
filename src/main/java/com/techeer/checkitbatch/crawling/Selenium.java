package com.techeer.checkitbatch.crawling;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Selenium {
    private WebDriver driver;

    private static final String url = "http://www.yes24.com/main/default.aspx";

    public void process() {
        log.info("*** 크롤링 시작 ***");

        System.setProperty("webdriver.chrome.driver", "/Users/misis1/myProject/Techeer-Book/checkitbatch/src/main/java/com/techeer/checkitbatch/crawling/chromedriver");
        //크롬 드라이버 셋팅 (드라이버 설치한 경로 입력)

        ChromeOptions chromeOptions = new ChromeOptions();
        // websocket 허용
        chromeOptions.addArguments("--remote-allow-origins=*");
//        chromeOptions.setHeadless(true);
        chromeOptions.addArguments("--lang=ko");
        chromeOptions.addArguments("--no-sandbox");
        chromeOptions.addArguments("--disable-dev-shm-usage");
        chromeOptions.addArguments("--disable-gpu");
        chromeOptions.setCapability("ignoreProtectedModeSettings", true);

        driver = new ChromeDriver(chromeOptions);

        try {
            setting();
            getAnotherCategories();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.close();
        driver.quit();
        log.info("*** 크롤링 끝 ***");
    }

    private void setting() throws InterruptedException {
        Thread.sleep(500);
        driver.get(url);    //브라우저에서 url로 이동한다.

//        driver.findElement(By.xpath("//*[@id=\"swrap\"]/div/div/div/div[2]/a[2]/em")).click();
        driver.findElement(By.xpath("//*[@id=\"chk_info\"]")).click();

        WebElement koreanBooks = driver.findElement(By.xpath("//*[@id=\"ulCategoryList\"]/li[1]/a/em"));
        koreanBooks.click();
    }

    private void getAnotherCategories() throws InterruptedException {
        Thread.sleep(500);
        WebElement categoryEle = driver.findElement(By.xpath("//*[@id=\"mCateLi\"]"));
        List<WebElement> categories = categoryEle.findElements(By.className("cate2d"));

        List<String> urlList = new ArrayList<>();

        for(WebElement category : categories) {
            WebElement aTag = category.findElement(By.tagName("a"));
            String url = aTag.getAttribute("href");
            urlList.add(url);
        }

        for(String url : urlList) {
            Thread.sleep(500);
            moveCategories(url);
        }
    }

    private void moveCategories(String url) throws InterruptedException {
        driver.get(url);
        Thread.sleep(500);

        Boolean flag = driver.findElements(By.xpath("//*[@id=\"cateSubListWrap\"]")).size() > 0;
        List<String> urlList = new ArrayList<>();

        // 서브 카테고리가 있으면
        if (flag) {
            WebElement subCategoriesEle  = driver.findElement(By.xpath("//*[@id=\"cateSubListWrap\"]"));
            int size = subCategoriesEle.findElements(By.tagName("dl")).size();
            for(int i = 1; i <= size; i++) {
                WebElement subCategory = driver.findElement(By.xpath("//*[@id=\"cateSubListWrap\"]/dl["+ i +"]/dt/a"));
                String subCateUrl = subCategory.getAttribute("href");
                urlList.add(subCateUrl);
            }
            for(String subUrl : urlList) {
                moveCategories(subUrl);
            }
        }
        else {
            List<WebElement> bookList = driver.findElements(By.className("imgBdr"));
            List<String> bookUrlList = new ArrayList<>();
            for(WebElement book : bookList) {
                WebElement aTag = book.findElement(By.tagName("a"));
                String bookUrl = aTag.getAttribute("href");
                bookUrlList.add(bookUrl);
            }

            for(String bookUrl : bookUrlList) {
                getInfo(bookUrl);
            }

        }
    }

    private void getInfo(String bookUrl) throws InterruptedException {
        Thread.sleep(500);
        driver.get(bookUrl);

        String title = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[2]/div[1]/div/h2")).getText();
        String author = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[2]/div[1]/span[2]/span[1]")).getText();
        String publisher = driver.findElement(By.xpath("//*[@id=\"yDetailTopWrap\"]/div[2]/div[1]/span[2]/span[2]/a")).getText();


        log.info(title);
        log.info(author);
        log.info(publisher);

        driver.navigate().back();
    }
}
