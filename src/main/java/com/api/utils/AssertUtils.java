package com.api.utils;

import com.api.enums.IServiceEnum;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

public class AssertUtils {

    public static ResponseSpecBuilder generateRelativeJsonPath(Response response, ResponseSpecBuilder expResponse, String rootPath, String path, String matcher) {
        if(rootPath.length() > 0 ) {
            rootPath +=".";
        }

        String[] splitPath = path.split("\\.");

        //when asserting an object (splitPath.length == 1)
        //when asserting an field in object (splitPath.length == 2 && !splitPath[0].contains("[]"))
        if(splitPath.length == 1 || (splitPath.length == 2 && !splitPath[0].contains("[]"))){
            if (matcher.equalsIgnoreCase("present")) {
                expResponse.expectBody(rootPath+path, is(notNullValue()));
            } else if (matcher.equalsIgnoreCase("notPresent")) {
                expResponse.expectBody(rootPath+path, is(nullValue()));
            }
        } else {
            String objectPath = splitPath[0].replace("[]", "");
            String fieldPath = splitPath[1].replace("[]", "");

            if (splitPath[0].contains("[]")) {
                int size = response.jsonPath().get(rootPath + objectPath + ".size()");

                for (int i = 0; i <= size - 1; i++) {
                    HashMap map = response.jsonPath().get(rootPath + objectPath + "[" + i + "]");

                    //Call recursive function when field path exists (map.containsKey(fieldPath))
                    //Call recursive function when path is complete for assertion
                    if (map.containsKey(fieldPath) || splitPath.length <= 2) {
                        generateRelativeJsonPath(response,
                                expResponse,
                                rootPath + objectPath + "[" + i + "]",
                                path.replace(splitPath[0] + ".", ""),
                                matcher);
                    }
                }
            }
        }
        return expResponse;
    }

    public static void validateMappings(IServiceEnum serviceUnderTest, ResponseSpecBuilder expResults, Response actualResponse, Map<IServiceEnum, Response> responseMap){

        try {
            expResults.build().validate(actualResponse);
        } catch (AssertionError error){
            throw new AssertionError(error.getMessage() + "\n\n" + printMappingResponses(serviceUnderTest, actualResponse, responseMap));
        }
    }

    private static String printMappingResponses(IServiceEnum serviceUnderTest, Response actualResponse, Map<IServiceEnum, Response> responsesMap){

        StringBuilder errorString = new StringBuilder();
        errorString.append("One or more fields in the " + serviceUnderTest.getName() + " response did not map to it's dependent services correctly.");
        errorString.append("\nResponse for " + serviceUnderTest.getName() + " (scroll down to see responses for dependent services mapped to):\n" + actualResponse.prettyPrint());
        errorString.append("\n\nThe following responses were used to map values to the " + serviceUnderTest.getName() + " response:");

        //print out each of the service names and their responses
        responsesMap.forEach((key, value)-> {
            errorString.append("\n\nResponse for endpoint: " + key.getName());
            errorString.append("\n" +value.prettyPrint());
        });
        return errorString.toString();
    }
}
