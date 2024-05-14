package com.google.pages;

import com.codeborne.selenide.SelenideElement;
import com.google.dataProvider.EnteredValues;
import org.openqa.selenium.By;
import static com.codeborne.selenide.CollectionCondition.sizeNotEqual;
import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class GoogleMainPage extends BasePage {
    private final SelenideElement searchField  = $(By.name("q"));
    private final SelenideElement searchButtonUnderSearchField = $(By.cssSelector("center:nth-child(1) > input:nth-child(1)"));
    private final SelenideElement searchButtonInDropDownMenu = $(By.cssSelector("center:nth-child(2) > input:nth-child(1)"));
    private final SelenideElement emailLink = $(By.linkText("Gmail"));
    private final SelenideElement searchPopup= $(By.id("Alh6id"));

    GoogleSearchResultPage resultPage = new GoogleSearchResultPage();

    public void fillSearchField() {
        actionFillField("FILL IN SEARCH FIELD", searchField, EnteredValues.value);
        searchPopup.shouldBe(appear);
    }

    public void clickEscapeButton() {
        actionClickButton("CLICK ON ESCAPE BUTTON", searchField, EnteredValues.buttonName.ESCAPE);
    }

    public void clickEnterButton() {
        actionClickButton("CLICK ON ENTER BUTTON", searchField, EnteredValues.buttonName.ENTER);
        checkTextAndElementVisible();
    }

    public void clickSearchButtonUnderSearchFieldWhenSearchFieldIsFilled() {
        actionClickElement("CLICK ON SEARCH BUTTON UNDER SEARCH FIELD", searchButtonUnderSearchField);
        checkTextAndElementVisible();
    }

    public void clickSearchButtonUnderSearchFieldWhenSearchFieldIsEmpty() {
        actionClickElement("CLICK ON SEARCH BUTTON UNDER SEARCH FIELD", searchButtonUnderSearchField);
        emailLink.shouldBe(appear);
    }

    public void clickSearchButtonInDropDownMenu() {
        actionClickElement("CLICK ON SEARCH BUTTON IN DROPDOWN MENU", searchButtonInDropDownMenu);
        checkTextAndElementVisible();
    }

    private void checkTextAndElementVisible() {
        searchField.shouldHave(text(EnteredValues.value));
        resultPage.getSearchBlocks().shouldBe(sizeNotEqual(0));
    }
}
