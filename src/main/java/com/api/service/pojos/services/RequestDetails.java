package com.api.service.pojos.services;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RequestDetails {

    private String requestString;

    private String requestMethod;
    private String requestPath;
    private Map<String, String> requestParams;
    private Map<String, String> queryParams;
    private Map<String, String> formParams;
    private Map<String, String> pathParams;
    private Map<String, String> headers;
    private Map<String, String> cookies;
    private String body;

    public RequestDetails(String requestString) {

        this.requestString = requestString;
        this.requestMethod = getRequestValue("Request method");
        this.requestPath = getRequestValue("Request path");
        this.requestParams = getMultiLineRequestValues("Request params");
        this.queryParams = getMultiLineRequestValues("Query params");
        this.formParams = getMultiLineRequestValues("Form params");
        this.pathParams = getMultiLineRequestValues("Path params");
        this.headers = getMultiLineRequestValues("Headers");
        this.cookies = getMultiLineRequestValues("Cookies");
        this.body = getRequestValue("Body");
    }

    private String getRequestValue(String valueKey) {

        String rS = this.requestString;
        String value = null;

        if (rS.contains(valueKey)) {
            String requestDetailsArray[] = rS.split("\n");

            for (int i = 0; i <= requestDetailsArray.length; i++) {

                //split into each line within log and get key and list of values
                if (Arrays.asList(requestDetailsArray).get(i).contains(valueKey)) {
                    String requestKeyAndValue[] = Arrays.asList(requestDetailsArray).get(i).split("\t");
                    value = Arrays.asList(requestKeyAndValue).get(1).trim();
                    break;
                }
            }
        } else {
            throw new RuntimeException("Could not find the request detail '" + valueKey + "' in the main request log.");
        }

        return value;
    }

    private Map<String, String> getMultiLineRequestValues(String valueKey) {

        String rS = this.requestString;
        Map<String, String> requestMap = new HashMap<>();

        if(rS.contains(valueKey)) {
            String requestDetailsArray[] = rS.split("\n");

            for (int i = 0; i <= requestDetailsArray.length; i++) {

                //split into each line within log and get key and list of values
                if (Arrays.asList(requestDetailsArray).get(i).contains(valueKey)) {

                    String requestKeyAndValue[] = Arrays.asList(requestDetailsArray).get(i).split("\t");
                    if (Arrays.asList(requestKeyAndValue).get(requestKeyAndValue.length - 1).trim().equalsIgnoreCase("<none>")) {
                        break;
                    } else {
                        String keyAndValue[] = Arrays.asList(requestKeyAndValue).get(requestKeyAndValue.length-1).split("=");
                        requestMap.put(Arrays.asList(keyAndValue).get(0).trim().toLowerCase(), Arrays.asList(keyAndValue).get(1).trim());
                    }

                    //also check if next line in request log is part of the current request details key
                    int n = 1;
                    while (i <= requestDetailsArray.length && Arrays.asList(requestDetailsArray).get(i + n).startsWith("\t")) {
                        String keyAndValue[] = Arrays.asList(requestDetailsArray).get(i + n).split("=");
                        requestMap.put(Arrays.asList(keyAndValue).get(0).trim().toLowerCase(), Arrays.asList(keyAndValue).get(1).trim());
                        n++;
                    }
                    break;
                }
            }
        } else {
            throw new RuntimeException("Could not find the request detail '" + valueKey + "' in the main request log.");
        }
        return requestMap;
    }


    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getFormParams() {
        return formParams;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String getBody() {
        return body;
    }
}
