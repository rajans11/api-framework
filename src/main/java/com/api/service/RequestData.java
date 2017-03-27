package com.api.service;

import com.api.enums.HttpMethodEnum;
import com.api.utils.ServiceUtil;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.internal.RequestSpecificationImpl;
import com.jayway.restassured.response.Response;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class RequestData {
    private Response response;
    private String url;

    private RequestSpecificationImpl spec;

    public RequestData(RequestSpecBuilder requestSpec, String url, HttpMethodEnum httpMethod, Response response) {
        setData(requestSpec, url, httpMethod, response);
    }

    public RequestData(String url, HttpMethodEnum httpMethod, Response response) {
        setData(url, httpMethod, response);
    }

    public RequestSpecBuilder getRequestSpecBuilder() {
        return requestSpecBuilder;
    }

    private RequestSpecBuilder requestSpecBuilder;

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setUrl(String url) {
        if (spec != null) {
            final String baseUri = spec.getBaseUri();
            if (baseUri.contains("localhost")) {
                this.url = url;
            } else if (url.startsWith(baseUri)) {
                this.url = url;
            } else {
                this.url = baseUri + url;
            }

        } else {
            this.url = url;
        }
    }

    public void setSpec(RequestSpecificationImpl spec) {
        this.spec = spec;
    }

    public void setRequestSpecBuilder(RequestSpecBuilder requestSpecBuilder) {
        this.requestSpecBuilder = requestSpecBuilder;
    }

    public void setHttpMethod(HttpMethodEnum httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setData(RequestSpecBuilder requestSpec, String url, HttpMethodEnum httpMethod, Response response) {
        setSpec(requestSpec);
        setResponse(response);
        setHttpMethod(httpMethod);
        setUrl(url);
    }

    public void setData(String url, HttpMethodEnum httpMethod, Response response) {
        setResponse(response);
        setHttpMethod(httpMethod);
        setUrl(url);
    }

    private String getBaseUri(RequestSpecificationImpl specification) {
        String baseUri = null;
        if (!specification.getBaseUri().contains("localhost")) {
            baseUri = specification.getBaseUri();
        }
        return baseUri;
    }

    public HttpMethodEnum getHttpMethod() {
        return httpMethod;
    }

    private HttpMethodEnum httpMethod;


    public Response getResponse() {
        return response;
    }

    public String getUrl() {
        return url;
    }

    public RequestSpecificationImpl getSpec() {
        return spec;
    }


    public RequestSpecificationImpl setSpec(RequestSpecBuilder requestSpecBuilder) {
        this.requestSpecBuilder = requestSpecBuilder;
        try {
            final Field specField = ReflectionUtils.findField(RequestSpecBuilder.class, "spec");
            specField.setAccessible(true);
            spec = (RequestSpecificationImpl) ReflectionUtils.getField(specField, requestSpecBuilder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return spec;
    }

    public String getRequestData() {
        return getRequestData(false);
    }

    public String getRequestData(Boolean removeApigeeDetails) {
        return getStringBuilder(removeApigeeDetails).toString();
    }

    private StringBuilder getStringBuilder(Boolean removeApigeeDetails) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n###################################### Test Request Details ######################################\n");
        stringBuilder.append("\nRequest method:\t\t" + getHttpMethod());
        stringBuilder.append("\nRequest Url:\t\t" + getUrl());
        stringBuilder.append("\nResponse Code:\t\t " + getResponse().getStatusCode());
        return stringBuilder;
    }

    public String getRequestDataWithResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getRequestData(true));
        stringBuilder.append("\nResponse Pretty print: \n");
//        not doing pretty pretty pring because, pretty print not working in bamboo logs and printing for non failures too
//        getJsonPrettify(getResponse());
        stringBuilder.append(ServiceUtil.getJsonPrettify(getResponse()));
        stringBuilder.append(getResponse());
        return stringBuilder.toString();
    }
}
