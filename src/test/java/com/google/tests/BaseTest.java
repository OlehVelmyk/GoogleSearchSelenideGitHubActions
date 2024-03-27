package com.google.tests;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import com.google.dataProvider.DateProvider;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import org.testng.Assert;
import org.testng.annotations.*;

import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;

public abstract class BaseTest {
    private String baseUrl = "https://www.google.com/";

    @BeforeClass
    @Parameters("browser")
    public void setUp(@Optional("chrome") String browser) {
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true)
                .savePageSource(false)
                .includeSelenideSteps(true)
        );
        Configuration.reportsFolder = "reports/screenshots/" + DateProvider.currentDate() +
                "_" + DateProvider.currentTime();
        Configuration.downloadsFolder = "";
        Configuration.browser = browser;
        Configuration.headless = true; //                            need for github actions CI
    }

    @BeforeMethod
    protected void goToPage() {
        Allure.step("GO TO " + baseUrl);
        open(baseUrl);
        getWebDriver().manage().window().maximize(); //              need for github actions CI

        String currentUrl = WebDriverRunner.getWebDriver().getCurrentUrl();
        Assert.assertEquals(currentUrl, baseUrl);
    }

    protected  void updateTestCaseName(String testName){
        Allure.getLifecycle().updateTestCase(result -> {
            result.setName(testName);
        });
    }
}
