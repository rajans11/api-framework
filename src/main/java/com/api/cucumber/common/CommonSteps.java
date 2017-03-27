package com.api.cucumber.common;

import com.api.enums.MatcherEnum;
import com.api.matchers.MatcherUtil;
import com.api.session.SessionKey;
import com.api.utils.AssertUtils;
import com.api.utils.ServiceUtil;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.response.Response;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import gherkin.formatter.model.DataTableRow;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Component
public class CommonSteps extends BaseStep {

    @Autowired
    MatcherUtil matcherUtil;

    @Given("^I wait for '(.*)' seconds$")
    public void waitForSeconds(int seconds){
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e){}
    }

//    have left this step below in as commented out in case people find it useful for schema validation testing

//    @And("^I validate the response matches the json schema in the describedBy link$")
//    public void assertJsonSchema() throws Throwable {
//        // Get response
//        Response response = (Response) scenarioSession.getData(SessionKey.latestResponse);
//        // Get describedBy link URL
//        String schemaUrl = response.jsonPath().getString("_links.describedBy.href");
//        URL url = new URL(schemaUrl);
//
//        // Build base uri (namespace) for Json Schema Validator
//        String describedByTemplate = "{protocol}://{host}/{servicePath}/{schemaName}.schema.json";
//        UriTemplate template = new UriTemplate(describedByTemplate);
//        Map<String, String> variables = template.match(schemaUrl);
//        String baseUri = buildBaseUri(variables.get("protocol"), variables.get("host"), variables.get("servicePath"));
//        final URITranslatorConfigurationBuilder builder = URITranslatorConfiguration.newBuilder().setNamespace(baseUri);
//        final LoadingConfiguration cfg = LoadingConfiguration.newBuilder().setURITranslatorConfiguration(builder.freeze()).freeze();
//        JsonSchemaFactory factory = JsonSchemaFactory.newBuilder().setLoadingConfiguration(cfg).freeze();
//
//        // Validation
//        assertThat(response.asString(), (JsonSchemaValidator) matchesJsonSchema(url).using(factory));
//    }


    @Then("^the status code (\\d+) is returned$")
    public void assertStatusCode(int statusCode) throws Throwable {
        assertResponse(new ResponseSpecBuilder().expectStatusCode(statusCode));
    }

    @Then("^the status code (\\d+) or (\\d+) is returned$")
    public void assertStatusCodeOr(int statusCode, int statusCode2) throws Throwable {
        assertResponse(new ResponseSpecBuilder().expectStatusCode(isOneOf(statusCode, statusCode2)));
    }

    @Then("^the field with jsonPath '(.*)' equals '(.*)'$")
    public void assertField(String path, String value) {
        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();

        //update the value to include the env specific value if specified otherwise just return the same value
        value = replaceEnvStringDetails(value);

        if (value.equalsIgnoreCase("null")) {
            expResponse.expectBody(path, nullValue());
        } else {
            expResponse.expectBody(path, equalTo(value));
        }

        assertResponse(expResponse);
    }

    @Then("^the field with jsonPath '(.*)' equals boolean '(.*)'$")
    public void assertBooleanField(String path, Boolean value) {
        assertResponse(new ResponseSpecBuilder().expectBody(path, equalTo(value)));
    }

    @Then("^the field with jsonPath \'(.*)\' equals int \'(.*)\'$")
    public void assertBooleanField(String path, int value) {
        this.assertResponse((new ResponseSpecBuilder()).expectBody(path, Matchers.equalTo(value)));
    }

    @Then("^all of the fields with jsonPath '(.*)' are '(present|not present)'$")
    public void assertAllFieldsPresent(String path, String value) {

        if (value.equalsIgnoreCase("present")){
            assertResponse(new ResponseSpecBuilder().expectBody(path, everyItem(notNullValue())));
        } else if (value.equalsIgnoreCase("not present")){
            assertResponse(new ResponseSpecBuilder().expectBody(path, everyItem(nullValue())));
        }
    }

    @Then("^the boolean field with jsonPath '(.*)' equals '(.*)'$")
    public void assertBoolField(String path, Boolean value) {
        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();
        expResponse.expectBody(path, equalTo(value));
        assertResponse(expResponse);
    }

    @Then("^all of the the boolean fields with jsonPath '(.*)' equals '(.*)'$")
    public void assertAllBoolField(String path, Boolean value) {
        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();
        expResponse.expectBody(path, everyItem(equalTo(value)));
        assertResponse(expResponse);
    }

    @Then("^the wrapped field (.*) equals value (.*) for path (.*)")
    public void assertWrappedField(String field, Object value, String path) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        String responseStr = response.asString();
        // Check for wrapped response
        if (!path.equals("")) {
            responseStr = responseStr.substring((path + "(").length(), responseStr.length() - 1);
        }

        JsonPath jsonPath = new JsonPath(responseStr);
        Object obj = jsonPath.get(field);

        if (obj instanceof Integer){
            int val = (Integer)obj;
            assertThat("Check values", val, equalTo(value) );
        } else if (obj instanceof String){
            String val = (String)obj;
            assertThat("Check values", val, equalTo(value) );
        }
    }

    @Then("^the field with jsonPath '(.*)' is not equal to '(.*)'$")
    public void assertFieldNotEqual(String path, int value) {
        assertResponse(new ResponseSpecBuilder().expectBody(path, not(equalTo(value))));
    }

    @Then("^the field count with jsonPath (.*) is (equalTo|greaterThan|lessThan) (.*)$")
    public void assertFieldCount(String path, String matcher, int value) {
        assertResponse(matcherUtil.getResponseSpecBuilder(MatcherEnum.valueOf(matcher), path + ".size()", value));
    }

    @Then("^the field with jsonPath (.*) is of type (String|Integer|Array|Boolean)$")
    public void assertFieldType(String path, String type) {
        assertResponse(matcherUtil.getResponseSpecBuilder(MatcherEnum.valueOf("instanceOf"+type), path, null));
    }

    @Then("^the field with jsonPath '(.*)' is (not equal to|equal to) the number '(-?\\d+)'$")
    public void assertNumberField(String path, String matcher, Integer number) {

        ResponseSpecBuilder expSpec = new ResponseSpecBuilder();
        if (matcher.equalsIgnoreCase("not equal to")) {
            expSpec.expectBody(path, not(equalTo(number)));
        } else {
            expSpec.expectBody(path, equalTo(number));
        }

        assertResponse(expSpec);
    }

    @Then("^the content type (xml|json) is returned$")
    public void assertContentType(String contentType) {

        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();
        if(contentType.equalsIgnoreCase("xml")) {
            expResponse.expectContentType(ContentType.XML);
        } else if (contentType.equalsIgnoreCase("json")) {
            expResponse.expectContentType(ContentType.JSON);
        }

        assertResponse(expResponse);
    }

    @And("^the error code (.*) and developer msg (.*) is returned$")
    public void assertStatusCode(String errorCode, String devMsg) {

        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();
        expResponse.expectBody("errorCode", equalTo(errorCode));

        if (devMsg.equalsIgnoreCase("The endpoint is not well-formed")) {
            expResponse.expectBody("developerMessage", is(both(containsString("The endpoint [")).and(containsString("] is not well-formed"))));
        } else {
            expResponse.expectBody("developerMessage", equalTo(devMsg));
        }
        assertResponse(expResponse);
    }

    @Given("^I have the test (.*)$")
    public void writeTestName(String testName) {
        System.out.println("********\nStarting test: '" + testName + "'\n");
    }

    @And("^I add the request parameter (.*) with value (.*)$")
    public void addRequestParam(String name, String value) {
        RequestSpecBuilder request = (RequestSpecBuilder) scenarioSession.getData(SessionKey.REQUEST);
        request.addParameter(name, value);
        scenarioSession.putData(SessionKey.REQUEST, request);
    }

    @And("^I add the path parameter (.*) with value (.*)$")
    public void addPathParam(String name, String value) {
        RequestSpecBuilder request = getNewOrStoredRequestSpec();
        request.addPathParam(name, value);
        scenarioSession.putData(SessionKey.REQUEST, request);
    }

    @And("^I add the request parameter (.*) with an empty value$")
    public void addRequestParam(String name) {
        addRequestParam(name, "");
    }

    @Given("I add the following request parameters$")
    public void addRequestParams(DataTable table) {

        RequestSpecBuilder request = (RequestSpecBuilder) scenarioSession.getData(SessionKey.REQUEST);

        DataTableRow values = table.getGherkinRows().get(1);

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        int i=0;
        for (String cellHeading : table.topCells()) {
            if((! values.getCells().get(i).isEmpty())) {
                params.put(cellHeading, values.getCells().get(i));
            }
            i++;
        }

        request.addParameters(params);
        request.setUrlEncodingEnabled(false);
        scenarioSession.putData(SessionKey.REQUEST, request);
        scenarioSession.putData(SessionKey.REQUEST_PARAM_MAP, params);
    }

    @Given("I have a new request with the following details")
    public void newQuestWithDetails(DataTable table) {
        scenarioSession.putData(SessionKey.REQUEST, new RequestSpecBuilder());
        addRequestDetails(table);
    }

    @Given("I add the following details to the request")
    public void addRequestDetails(DataTable table) {

        String type;
        String name;
        String value;

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        LinkedHashMap<String, String> pathParams = new LinkedHashMap<>();
        LinkedHashMap<String, String> headers = new LinkedHashMap<>();

        RequestSpecBuilder request = getNewOrStoredRequestSpec();

        List<DataTableRow> rows = table.getGherkinRows();

        for (int i = 1; i < rows.size(); i++) {

            type = rows.get(i).getCells().get(0);
            name = rows.get(i).getCells().get(1);
            value = rows.get(i).getCells().get(2);

            if (type.equalsIgnoreCase("REQUEST_PARAM")) {
                params.put(name, value);
            } else if (type.equalsIgnoreCase("PATH_PARAM")) {
                pathParams.put(name, value);
            } else if (type.equalsIgnoreCase("HEADER")) {
                headers.put(name, value);
            } else {
                throw new RuntimeException("Type '" + type + "' is not a valid type. Allowed values are: REQUEST_PARAM, PATH_PARAM, HEADER");
            }
        }

        request.addParameters(params);
        request.addPathParams(pathParams);
        request.addHeaders(headers);
        request.setUrlEncodingEnabled(false);
        scenarioSession.putData(SessionKey.REQUEST, request);
    }

    @And("the following fields and their values are returned successfully$")
    public void assertFields(DataTable table) {
        assertFieldsWithRootPath("", table);
    }

    /*
    This method takes a datatable that is allowed 3 rows - field, matcher and value.
    field = the jsonPath to the field you want to test
    matcher = corresponds to a Hamcrest Matcher. Values allowed are "equals", "notEqual", "notNull", "pattern" and "containsString"
    or corresponds to a Custom Matcher. Values allowed are "datePattern", "containsAll"
    or corresponds to values "equalsBoolean", "equalsInt", "path"
    Note - the first row of the table is skipped as it assumes this has the titles of the columns in
     */
    @And("the following fields and their values are returned successfully for the root jsonPath (.*)")
    public void assertFieldsWithRootPath(String rootPath, DataTable table) {

        String field;
        String matcher;
        Object value;

        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();
        if (! rootPath.isEmpty()) {
            expResponse.rootPath(rootPath);
        }

        List<DataTableRow> rows = table.getGherkinRows();

        for(int i=1; i<rows.size(); i++) {

            field = rows.get(i).getCells().get(0);
            matcher = rows.get(i).getCells().get(1);
            value = rows.get(i).getCells().get(2);

            value = replaceEnvStringDetails(value.toString());
            field = replaceStoredData(field);
            value = replaceStoredData(value.toString());
            expResponse.expectBody(field, matcherUtil.getMatcher(MatcherEnum.valueOf(matcher), value));
        }
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        expResponse.build().validate(response);
    }

    /**
     * This method is an intermediary to the {@link #assertFieldsWithRootPath(String, DataTable)} method, it gets the jsonpath
     * and randomly get an object in the list and sets that as the root path.
     * @param jsonPath a json path of the array of objects you want to target
     * @param table the cucumber datatable where you pass three columns: Field, Matcher, Value
     *              see {@link #assertFieldsWithRootPath(String, DataTable)} for more information.
     */
    @And("^I get (\\d+) random item from the list with json path (.*)$")
    public void iGetARandomItemFromTheList(int count, String jsonPath, DataTable table) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        int jsonPathSize = response.getBody().jsonPath().get(jsonPath + ".size()");

        //The upper limit of the count can only be the same value as a the json path size
        if (count > jsonPathSize) {
            count = jsonPathSize;
        }

        Set<Integer> list = new LinkedHashSet<>();
        while(list.size() < count - 1){
            list.add(ServiceUtil.getARandomNumber(0, jsonPathSize - 1));
        }
        for (Integer path : list) {
            this.assertFieldsWithRootPath(jsonPath + "[" + path + "]" , table);
        }
    }

    @And("^I compare the following fields against the specified fields returned from the response stored as '(.*)'$")
    public void compareFieldsWithAgainstStoredResponse(String responseKey, DataTable table) {

        String field1;
        String field2;
        String matcher;
        Object value;

        Response storedResponse = (Response) scenarioSession.getData(responseKey);
        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();
        List<DataTableRow> rows = table.getGherkinRows();

        for (int i = 1; i < rows.size(); i++) {

            field1 = rows.get(i).getCells().get(0);
            matcher = rows.get(i).getCells().get(1);
            field2 = rows.get(i).getCells().get(2);

            try {
                value = storedResponse.jsonPath().get(field2);
            } catch (Exception e) {
                throw new RuntimeException("There was a problem retrieving value of field '" + field2 + "'. Please check the path details are correct");
            }

            //check matcher is an allowed value
            assertThat("The specified matcher '" + matcher + "' isn't an allowable matcher for this step", matcher, anyOf(equalToIgnoringCase("equals"), equalToIgnoringCase("notEqual")));
            expResponse.expectBody(field1, matcherUtil.getMatcher(MatcherEnum.valueOf(matcher), value));
        }
        assertResponse(expResponse);
    }

    @And("^I add the request parameter '(.*)' from stored '(.*)'$")
    public void addParamUsingStore(String param, String key ){
        RequestSpecBuilder request = (RequestSpecBuilder) scenarioSession.getData(SessionKey.REQUEST);
        String storedValue = (String)scenarioSession.getData(key);

        request.addParameter(param, String.valueOf(storedValue));
        scenarioSession.putData(SessionKey.REQUEST, request);
    }

    @Given("^I have a new request$")
    public void baseAvailabilityRequest() {
        scenarioSession.putData(SessionKey.REQUEST, new RequestSpecBuilder().setUrlEncodingEnabled(false));
    }

    @Then("^the field with jsonPath (.*) contains string (.*)$")
    public void assertFieldContains(String jsonPath, String value) {
        assertResponse(new ResponseSpecBuilder().expectBody(jsonPath, containsString(value)));
    }

    @Then("^the field with jsonPath (.*) (is|is not) present$")
    public void assertFieldIsPresent(String jsonPath, String isPresent) {

        if (isPresent.equalsIgnoreCase("is")) {
            assertResponse(new ResponseSpecBuilder().expectBody(jsonPath, is(not(nullValue()))));
        } else if (isPresent.equalsIgnoreCase("is not")) {
            assertResponse(new ResponseSpecBuilder().expectBody(jsonPath, is(nullValue())));
        } else {
            throw new RuntimeException("The value to determine whether to check the field is present or not is not one of the allowed values" +
                    "of 'is' or 'is not'");
        }
    }

    @Given("^the response header (.*) is equal to (.*)$")
    public void assertResponseHeader(String name, String value) {
        assertResponse(new ResponseSpecBuilder().expectHeader(name, value));
    }

    @Given("^the response header (.*) contains the string (.*)$")
    public void assertResponseHeaderContains(String name, String value) {
        assertResponse( new ResponseSpecBuilder().expectHeader(name, containsString(value)));
    }

    @Given("^the response header (.*) matches the date pattern (.*)$")
    public void assertResponseHeaderMatches(String name, String pattern) throws Exception {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        String val = response.getHeader(name);

        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date d = null;
        try {
            d = formatter.parse(val);
        } catch (ParseException e) {
        }
        assertThat(d, is(notNullValue()));
    }

    @And("^The Expires header is calculated correctly$")
    public void calculateExpiresHeader() throws Exception {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        // Get Values from response
        String expiresHeader = response.getHeader("Expires");
        String cacheControl = response.getHeader("Cache-Control");
        int maxAgeSecs = Integer.valueOf(cacheControl.substring(cacheControl.indexOf("=") + 1));
        String dateHeader = response.getHeader("Date");

        // Calculate dates
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        Date date = formatter.parse(dateHeader);
        Date expires = formatter.parse(expiresHeader);
        Date expectedExpires = new Date(date.getTime() + maxAgeSecs * 1000L);

        // Check
        assertThat(expires, is(equalTo(expectedExpires)));
    }

    @And("^I store the details for the jsonPath (.*) from the response$")
    public void storeJsonDetails(String jsonPath) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);

        String json = response.jsonPath().get(jsonPath).toString();
        if (json == null) {
            throw new RuntimeException("The json with jsonPath '" + jsonPath + "' retrieved null results from the following response: \n \n" + response.prettyPrint());
        } else {
            scenarioSession.putData(SessionKey.STORED_JSON, json);
        }
    }

    @And("^I store the (jsonPath|header) '(.*)' as '(.*)' in store$")
    public void storeWithKey(String which, String what, String key){
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        String value = "";
        if (which.equalsIgnoreCase("jsonPath")) {
            value = response.jsonPath().get(what).toString();
        } else if (which.equalsIgnoreCase("header")) {
            value = response.getHeader(what);
        }
        scenarioSession.putData(key, value);
    }

    @And("^I store the jsonPath '(.*)' as '(.*)' in store as a List$")
    public void storeWithKey(String path, String key) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        List<String> value = response.jsonPath().getList(path);
        scenarioSession.putData(key, value);
    }

    @And("^I calculate and store value '(.*)' from stored '(.*)' then '(add|subtract|divide)' '(.*)'$")
    public void calculateAndStoreValue(String valueKey, String storedKey, String action, String operand) {
        String storedValue = (String) scenarioSession.getData(storedKey);
        int newValue = -1;
        if (action.equalsIgnoreCase("add")) {
            newValue = Integer.parseInt(storedValue) + Integer.parseInt(operand);
        } else if (action.equalsIgnoreCase("subtract")) {
            newValue = Integer.parseInt(storedValue) - Integer.parseInt(operand);
        } else if (action.equalsIgnoreCase("divide")) {
            newValue = Integer.parseInt(storedValue) / Integer.parseInt(operand);
        }
        scenarioSession.putData(valueKey, String.valueOf(newValue));
    }

    @And("^the details for the (jsonPath|header) (.*) are (equal|notEqual) to those previously stored with key (.*)$")
    public void assertPreviouslyStored(String which, String what, String match, String key) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);

        String current = "";
        if (which.equalsIgnoreCase("jsonPath")) {
            current = response.jsonPath().get(what).toString();
        } else if (which.equalsIgnoreCase("header")) {
            current = response.getHeader(what);
        }
        String expected = (String) scenarioSession.getData(key);
        Matcher matcher = matcherUtil.getMatcher(MatcherEnum.valueOf(match), expected);
        assertThat(current, matcher);
    }

    @And("^the (Cache-Control) response header is (less|more) than (.*)$")
    public void assertCacheControlMaxAgeValue(String headerName, String matcher, int value) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        String header = response.getHeader(headerName);
        Integer maxAge = Integer.parseInt(header.substring(header.lastIndexOf("=") + 1));

        Matcher match = matcherUtil.getMatcher(MatcherEnum.valueOf(matcher), value);
        assertThat("The " + headerName + " header's max age was not less than 0. Actual age = '" + maxAge + "'", maxAge, match);
    }

    @And("^I store the last response as '(.*)'$")
    public void storeLastResponse(String key) {
        scenarioSession.putData(key, (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE));
    }

    @And("^the field with jsonPath (.*) equals the field with jsonPath from the response stored as (.*)$")
    public void compareTwoResponseValues(String jsonPath1, String jsonPath2, String key) {
        Response olderResponse = (Response) scenarioSession.getData(key);

        String value1 = olderResponse.jsonPath().get(jsonPath2);
        assertResponse(new ResponseSpecBuilder().expectBody(jsonPath1, equalTo(value1)));
    }

    @And("^I add the request header (.*) with value (.*)$")
    public void addRequestHeader(String name, String value) {
        RequestSpecBuilder request = (RequestSpecBuilder) scenarioSession.getData(SessionKey.REQUEST);
        request.addHeader(name, value);
        scenarioSession.putData(SessionKey.REQUEST, request);
    }

    @And("^The field with json path '(.*)' equals one of '(.*)'$")
    public void assertOneOf(String path, List<String> expected) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        new ResponseSpecBuilder().expectBody(path, isIn(expected)).build().validate(response);
    }

    @And("^The field with json path '(.*)' equals one of the numbers '(.*)$")
    public void assertOneOfTheNumber(String path, List<Integer> expected) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        new ResponseSpecBuilder().expectBody(path, isIn(expected)).build().validate(response);
    }

    private String buildBaseUri(String protocol, String host, String servicePath) {
        return protocol + "://" + host + "/" + servicePath + "/";
    }

    @Then("^the field with relative path (.*) is (present|notPresent)$")
    public void the_field_is_not_present(String path, String matcher) throws Throwable {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);

        ResponseSpecBuilder expResponse = new ResponseSpecBuilder();

        expResponse = AssertUtils.generateRelativeJsonPath(response, expResponse, "", path, matcher);

        assertResponse(expResponse);
    }

    @Then("^the following fields in the '(.*)' response match the specified fields in the previously stored response with key '(.*)'$")
    public void assertFieldMappings(String service, String key, DataTable table) {

        String path;
        String sourcePath;
        Object sourceFieldValue;
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        Response sourceResponse = (Response) scenarioSession.getData(key);

        ResponseSpecBuilder expResults = new ResponseSpecBuilder();

        List<DataTableRow> rows = table.getGherkinRows();
        for (int i = 1; i < rows.size(); i++) {

            path = rows.get(i).getCells().get(0);
            sourcePath = rows.get(i).getCells().get(1);

            sourceFieldValue = sourceResponse.jsonPath().get(sourcePath);
            if(sourceFieldValue == null || sourceFieldValue.toString().isEmpty()) {
                expResults.expectBody(path, isEmptyOrNullString());
            } else {
                expResults.expectBody(path, equalTo(sourceFieldValue));
            }

        }

        //validate results
        expResults.build().validate(response);
    }

    @Then("^I store the response with session key '(.*)'$")
    public void storeResponse(String key) {
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        scenarioSession.putData(key, response);
    }

    @Then("^the following response headers and values are returned successfully$")
    public void assertResponseHeaders(DataTable table) {

        String header;
        String value;
        ResponseSpecBuilder expectedHeaders = new ResponseSpecBuilder();

        List<DataTableRow> rows = table.getGherkinRows();
        for (int i = 1; i < rows.size(); i++) {

            header = rows.get(i).getCells().get(0);
            value = rows.get(i).getCells().get(1);
            expectedHeaders.expectHeader(header, equalTo(value));
        }

        //assert the headers
        Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
        expectedHeaders.build().validate(response);
    }
}
