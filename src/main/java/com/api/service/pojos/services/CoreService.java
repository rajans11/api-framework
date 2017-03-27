package com.api.service.pojos.services;


import com.api.enums.HttpMethodEnum;
import com.api.service.BaseService;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.response.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CoreService extends BaseService {

    @Value("${service.direct.url}")
    private String baseUrl;

    private static final String endpoint = "/url/{param}/url/{id}.json";

    /**
     * returns the json response details for the request to the Core Service
     * @param request - the request in the form of a Rest Assured RequestSpecBuilder
     *                The request takes the following path parameters:
     *                - id (required)
     *
     *                The request takes the following request parameters:
     *                - client (required): Client Key
     *
     * @return the json response in the form of a Rest Assured Response object
     */
    public Response getEndpoint(RequestSpecBuilder request) {
        return sendRequest(setRequest(request), endpoint, HttpMethodEnum.GET).getResponse();
    }

    private RequestSpecBuilder setRequest(RequestSpecBuilder request){
        request.setBaseUri(baseUrl);
        return request;
    }
}
