package com.google.actionHelpers;

import com.codeborne.selenide.ElementsCollection;
import com.google.dataProvider.EnteredValues;
import com.google.pages.BasePage;
import com.google.pages.GoogleSearchResultPage;
import com.google.utils.DataConverter;
import io.qameta.allure.Allure;
import org.testng.Assert;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class CountSearchResult extends BasePage {
    GoogleSearchResultPage resultPage = new GoogleSearchResultPage();

    public void countAndEqualsSearchResults() {
        Assert.assertEquals(countSearchResult(), getResultCounter());
    }

    public int getResultCounter() {
        int expectedResult = Integer.parseInt(DataConverter.parseTextValue(resultPage.getTextWithResultCounter(),
                "\\d+"));
//        Allure.step("COUNT SEARCH EXPECTED RESULT = " + expectedResult);
        return expectedResult;
    }

    public int countSearchResult() {
        String text;
        int count = 0;

        ScrollSearchPageToBottom();

        ElementsCollection list = resultPage.getSearchBlocks();
        for (int i = 0; i < list.size(); i++) {
            text = list.get(i).getText();
            if (text.contains(EnteredValues.value)) {
                count += 1;
            }
        }
//        Allure.step("COUNT SEARCH ACTUAL RESULT = " + count);
        return count;
    }

    private void ScrollSearchPageToBottom() {
        boolean reachedBottom = false;
        long lastHeight = (long) (executeJavaScript("return document.body.scrollHeight"));
        while (!reachedBottom) {
            executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
            sleep(2000);
            long newHeight = (long) (executeJavaScript("return document.body.scrollHeight"));
            if (newHeight == lastHeight) {
                if(resultPage.textBlockIsNotPresent()) {
                    resultPage.clickMoreResultsButton();
                }else {
                    reachedBottom = true;
                }
            }
            lastHeight = newHeight;
        }
    }
}
