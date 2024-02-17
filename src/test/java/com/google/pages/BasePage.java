package com.google.pages;

import com.codeborne.selenide.SelenideElement;
import com.google.dataProvider.EnteredValues;
import io.qameta.allure.Allure;

public abstract class BasePage {

    public void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void actionClickElement(String logAction, SelenideElement locator) {
        Allure.step(logAction);
        locator.click();
    }

    public void actionFillField(String logAction, SelenideElement locator, String inputValue) {
        Allure.step(logAction);
        locator.sendKeys(inputValue);
    }

    public void actionClickButton(String logAction, SelenideElement locator,
                                  EnteredValues.buttonName buttonName) {
        Allure.step(logAction);
        switch(buttonName) {
            case ESCAPE:
                locator.pressEscape();
                break;
            case ENTER:
                locator.pressEnter();
                break;
            case TAB:
                locator.pressTab();
                break;
            default:
                Allure.step("BUTTON " + "'" + buttonName + "'" + " IS ABSENT IN THE LIST");
        }
    }
}
