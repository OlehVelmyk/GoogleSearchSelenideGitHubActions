package com.google.tests;

import com.google.actionHelpers.CountSearchResult;
import com.google.pages.GoogleMainPage;
import com.google.utils.RetryAnalyzer;
import io.qameta.allure.*;
import io.qameta.allure.testng.Tag;
import org.testng.annotations.Test;

import static io.qameta.allure.SeverityLevel.CRITICAL;

public class N_0005_CountSearchResultTest extends BaseTest {

//    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("This test attempts to google search 'Selenium' word. " +
            "Fails if any error happens.\n\nNote that this test doesn't test other search.")
    @Severity(CRITICAL)
    @Owner("Oleh Velmyk")
    @Tag("NewUI")
    @Link(name = "Website", url = "https://www.google.com/")
    @Issue("AUTH-005")
    @TmsLink("TMS-005")
    @Epic("Web interface")
    @Feature("Essential features")
    @Story("Search")
    public void countSearchResult() {
        updateTestCaseName("Count Search Results");

        GoogleMainPage page = new GoogleMainPage();
        CountSearchResult searchResult = new CountSearchResult();

        page.fillSearchField();
        page.clickEnterButton();
        searchResult.countAndEqualsSearchResults();
    }
}
