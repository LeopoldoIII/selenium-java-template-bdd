package com.template.hooks;

import com.template.api.ApiClient;
import com.template.context.ScenarioContext;
import com.template.driver.DriverFactory;
import com.template.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class Hooks {

    @Before(order = 1)
    public void resetSeedData() {
        try {
            ApiClient.given().post("/plans/reset");
        } catch (Exception e) {
            System.err.println("WARN: Could not reset seed data: " + e.getMessage());
        }
    }

    @Before(value = "@UI", order = 2)
    public void setUpUi() {
        WebDriver driver = DriverFactory.createDriver();
        DriverManager.setDriver(driver);
    }

    @After("@UI")
    public void tearDownUi(Scenario scenario) {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            try {
                if (scenario.isFailed()) {
                    byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
                    scenario.attach(screenshot, "image/png", "failure-screenshot");
                }
            } finally {
                DriverManager.quitDriver();
            }
        }
    }

    @After(order = 10000)
    public void cleanUpContext() {
        ScenarioContext.clear();
    }
}
