package base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import org.testng.annotations.BeforeSuite;

import static config.Environment.getBaseUri;
import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.filters;

public class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void setup() {
        baseURI = getBaseUri();

        filters(new AllureRestAssured());

        if (Boolean.parseBoolean(System.getProperty("api.logHttp", "false"))) {
            filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
        }
    }
}
