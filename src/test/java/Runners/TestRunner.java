package Runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features= {"src/test/resources/FeatureFiles"},
glue= {"StepDefinitions", "TestSetup"},
plugin= {"pretty", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"}
)

 public class TestRunner extends AbstractTestNGCucumberTests{



}


