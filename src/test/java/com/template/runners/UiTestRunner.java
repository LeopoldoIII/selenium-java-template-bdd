package com.template.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/UI",
        glue = {"com.template.steps", "com.template.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-ui-reports.html",
                "json:target/cucumber-ui-reports.json"
        }
)
public class UiTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
