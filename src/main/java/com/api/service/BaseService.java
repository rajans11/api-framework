package com.api.service;

import com.api.enums.HttpMethodEnum;
import com.api.utils.RequestsLog;
import com.api.utils.ServiceUtil;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.LogConfig.logConfig;
import static org.hamcrest.CoreMatchers.anything;

@Component
public class BaseService {

    //main services baseurl
    @Value("${services.baseurl}")
    private String servicesBaseUrl;
    @Autowired
    public RequestsLog requestsLog;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseService.class);

    public String getServicesBaseUrl() {
        return servicesBaseUrl;
    }

//    private static Boolean isProd = true;

    protected RequestData sendRequest(RequestSpecBuilder requestSpec, String url, HttpMethodEnum httpMethod) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        RestAssuredConfig config = new RestAssuredConfig();
        config = config.logConfig(logConfig().defaultStream(ps));

        Response response = null;

        //start timer
//        long startTime = System.currentTimeMillis();
        switch (httpMethod) {
            case GET:
                response = given().config(config).log().all().urlEncodingEnabled(false).spec(requestSpec.build()).expect().spec(buildEmptyResponseSpec()).get(url);
                break;
            case POST:
                response = given().config(config).log().all().urlEncodingEnabled(false).spec(requestSpec.build()).expect().spec(buildEmptyResponseSpec()).post(url);
                break;
            case PUT:
                response = given().config(config).log().all().urlEncodingEnabled(false).spec(requestSpec.build()).expect().spec(buildEmptyResponseSpec()).put(url);
                break;
            case DELETE:
                response = given().config(config).log().all().urlEncodingEnabled(false).spec(requestSpec.build()).expect().spec(buildEmptyResponseSpec()).delete(url);
                break;
            case OPTIONS:
                response = given().config(config).log().all().urlEncodingEnabled(false).spec(requestSpec.build()).expect().spec(buildEmptyResponseSpec()).options(url);
                break;
        }

//        long endTime = System.currentTimeMillis();
//        System.out.println("\n Time taken for response was " + (endTime - startTime) + " milliseconds \n");

        //get the request details from the print stream
        String requestDetails = getRequestDetails(os, ps);
        String requestUrl = getUrlFromRequestDetails(requestDetails);

        final RequestData requestData = new RequestData(requestSpec, requestUrl, httpMethod, response);
        requestsLog.setRequestsLog(requestData);

        //print request
        LOGGER.info("\nFull request details:\n\n" + requestDetails);
        LOGGER.info("Response status code: " +response.getStatusCode());
        //print response
        if(response.getContentType().contains("json")) {
            LOGGER.debug("\n" + ServiceUtil.getJsonPrettify(response));
        }
        return requestData;
    }

    public RequestData sendRequest(String url, HttpMethodEnum httpMethod) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        RequestSpecification specLogAll = getRequestSpecification(ps);
//        specLogAll = isOnProd(specLogAll);
        Response response = null;
        switch (httpMethod) {
            case GET:
                response = specLogAll.expect().spec(buildEmptyResponseSpec()).get(url);
                break;
            case POST:
                response = specLogAll.expect().spec(buildEmptyResponseSpec()).post(url);
                break;
            case PUT:
                response = specLogAll.expect().spec(buildEmptyResponseSpec()).put(url);
                break;
            case DELETE:
                response = specLogAll.expect().spec(buildEmptyResponseSpec()).delete(url);
                break;
            case OPTIONS:
                response = specLogAll.expect().spec(buildEmptyResponseSpec()).options(url);
                break;
        }
        final RequestData requestData = new RequestData(url, httpMethod, response);
        requestsLog.setRequestsLog(requestData);

        return requestData;
    }

    private String getRequestDetails(ByteArrayOutputStream os, PrintStream ps) {

        String requestString = null;
        try {
            requestString = os.toString("UTF8");
            ServiceUtil.setLastRequestDetails(requestString);
            ps.close();
//            LOGGER.debug("Full request details:\n" + requestString);
//            System.out.println(requestString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return requestString;
    }

    private String getUrlFromRequestDetails(String requestDetails){

        String url;

        if (requestDetails != null){
            url = requestDetails.substring(requestDetails.indexOf("http"), requestDetails.indexOf("Proxy")).trim();
        } else {
            throw new RuntimeException("Request details was null so could not retrieve request url");
        }
        return url;
    }

    private static RequestSpecification getRequestSpecification(PrintStream ps) {

        RestAssuredConfig config = new RestAssuredConfig();
        config = config.logConfig(logConfig().defaultStream(ps));
        return given().config(config);
    }

    private static ResponseSpecification buildEmptyResponseSpec() {
        return new ResponseSpecBuilder().expectBody(anything()).build();
    }
}
