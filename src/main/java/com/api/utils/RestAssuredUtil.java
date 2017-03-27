package com.api.utils;

import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.springframework.stereotype.Component;

@Component
public class RestAssuredUtil {
    public static Object getJsonAttributeValue(Response response, String strPath) {
        JsonPath jsonPath = new JsonPath(response.asString());
        Object obj = null;
        try {
            obj = jsonPath.get(strPath);
        } catch (Exception exception) {
//            when field not found, return null
        }
        return obj;
    }


    public static boolean fieldExists(Response responseStr, String strPath) {
        JsonPath jsonPath = new JsonPath(responseStr.asString());
        boolean exists = false;
        try {
            exists = jsonPath.get(strPath) != null;
        } catch (Exception exception) {
//            when field not found, return false
        }
        return exists;
    }

    public static boolean fieldExists(Response response, ResponseSpecBuilder responseSpecBuilder) {
        try {
            responseSpecBuilder.build().validate(response);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}
