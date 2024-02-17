package com.google.pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class GoogleSearchResultPage extends BasePage {
    private final ElementsCollection searchBlocks = $$("div.MjjYud");
    private final SelenideElement moreResultsButton = $("div > a > h3 > div");
    private final ElementsCollection textBlock = $$("#ofr");

    public ElementsCollection getSearchBlocks() {
        return searchBlocks;
    }

    public void clickMoreResultsButton() {
        actionClickElement("CLICK ON MORE RESULTS BUTTON", moreResultsButton);
    }

    public boolean textBlockIsNotPresent() {
        return textBlock.isEmpty();
    }

    public String getTextWithResultCounter() {
        return textBlock.get(0).getText();
    }
}