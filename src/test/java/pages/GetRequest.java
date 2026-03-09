package pages;

import org.testng.annotations.Test;
import io.cucumber.java.en.*;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;


import static io.restassured.RestAssured.given;
public class GetRequest {
@Test
public void getrequest() {
	given()
    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
.when()
    .get("https://reqres.in") // Use a valid ID like '2' instead of 'id'
.then()
    .statusCode(200); 
}}