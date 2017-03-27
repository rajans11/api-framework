package com.api.cucumber.common;

import com.api.session.ScenarioSession;
import com.api.session.SessionKey;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@ContextConfiguration(locations = "classpath:cucumber.xml")
public class BaseStep {

    @Autowired
    public ScenarioSession scenarioSession;

    @Value("${environment}")
    private String environment;

    public String getEnvironment() {
        return environment;
    }

    public void assertResponse(ResponseSpecBuilder expectedResponseSpec) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        assertResponse(expectedResponseSpec, response);
    }

    public void assertResponse(ResponseSpecBuilder expectedResponseSpec, Response response) {
        expectedResponseSpec.build().validate(response);
    }

    public String getPropValue(String propValue){
        Resource resource = new ClassPathResource("/environments/" + getEnvironment() + ".properties");
        String value = null;
        try {
            value = PropertiesLoaderUtils.loadProperties(resource).getProperty(propValue);
        } catch (IOException e) {
            System.out.println("Failed to get the property: " + propValue + " please check it matches the same value in the active property file.");
        }
        return value;
    }

    public RequestSpecBuilder getNewOrStoredRequestSpec() {

        RequestSpecBuilder requestSpec;
        requestSpec = (RequestSpecBuilder) scenarioSession.getData(SessionKey.REQUEST);
        if (requestSpec == null) {
            requestSpec = new RequestSpecBuilder();
        }
        return requestSpec;
    }

    public String replaceEnvStringDetails(String value) {

        String env;
        if (value.contains("?{env}")) {
            env = getEnvironment();
            if (env.equalsIgnoreCase("prod")) {
                return value.replace("?{env}", "");
            }
            if (env.equalsIgnoreCase("local")) {
                env = getPropValue("local.copyOf");
            }
            return value.replace("?{env}", "." + env);
        } else {
            return value;
        }
    }

    public String replaceStoredData (String value) {
        while (value.contains("?stored{")) {
            int index = value.indexOf("?stored{")+8;
            String var = value.substring(index, value.indexOf("}",index));
            String toReplace = (String)scenarioSession.getData(var);
            value = value.replace("?stored{"+var+"}",toReplace);
        }
        return value;
    }
}
