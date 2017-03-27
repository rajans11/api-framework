package com.api.utils;

import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class ServiceUtil {
    private static String lastRequestDetails;

    public static int getARandomNumber(int minNumber, int maxNumber) {
        int randomNum = 0;

        try {
            if (minNumber == maxNumber) {
                randomNum = maxNumber;
            } else {
                Random random = new Random();
                randomNum = random.nextInt(maxNumber - minNumber) + 1;
            }

        } catch (Exception IllegalArgumentException) {
            throw new RuntimeException(" Invalid Min value '" + minNumber + "' or Max value '" + maxNumber + "' passed");
        }
        return randomNum;

    }


    public static String getLastRequestDetails() {
        return lastRequestDetails;
    }

    public static void setLastRequestDetails(String lastRequestDetails) {
        lastRequestDetails = lastRequestDetails;
    }

    public static String getJsonPrettify(Response response) {
        final String responseString = response.asString();
        String strPrettify = null;
        String contentType = response.getContentType();
        if (!contentType.contains("xml") && !contentType.contains("atom")) {
            if (!responseString.isEmpty()) {
                JsonPath jsonPath = new JsonPath(responseString);
                strPrettify = jsonPath.prettify();
            }
        } else {
            strPrettify = responseString;
        }

        return strPrettify;
    }

}
