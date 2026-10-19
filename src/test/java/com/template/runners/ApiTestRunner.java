package com.template.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/API",
        glue = {"com.template.steps", "com.template.hooks"},
        plugin = {
                "pretty",
                "html:target/cucumber-api-reports.html",
                "json:target/cucumber-api-reports.json"
        }
)
public class ApiTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
