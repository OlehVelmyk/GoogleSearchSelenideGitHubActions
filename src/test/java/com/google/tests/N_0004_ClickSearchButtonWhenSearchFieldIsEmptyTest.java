package com.google.tests;

import com.google.pages.GoogleMainPage;
import com.google.utils.RetryAnalyzer;
import io.qameta.allure.*;
import io.qameta.allure.testng.Tag;
import org.testng.annotations.Test;

import static io.qameta.allure.SeverityLevel.CRITICAL;

public class N_0004_ClickSearchButtonWhenSearchFieldIsEmptyTest extends BaseTest {

//    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("This test attempts to google search 'Selenium' word. " +
            "Fails if any error happens.\n\nNote that this test doesn't test other search.")
    @Severity(CRITICAL)
    @Owner("Oleh Velmyk")
    @Tag("NewUI")
    @Link(name = "Website", url = "https://www.google.com/")
    @Issue("AUTH-004")
    @TmsLink("TMS-004")
    @Epic("Web interface")
    @Feature("Essential features")
    @Story("Search")
    public void clickSearchButtonWhenSearchFieldIsEmpty() {
        updateTestCaseName("Empty Search Field");

        GoogleMainPage page = new GoogleMainPage();

        page.clickSearchButtonUnderSearchFieldWhenSearchFieldIsEmpty();
    }
}
