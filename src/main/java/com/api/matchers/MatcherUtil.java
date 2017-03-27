package com.api.matchers;

import com.api.enums.MatcherEnum;
import com.api.session.ScenarioSession;
import com.api.session.SessionKey;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jcabi.matchers.RegexMatchers.matchesPattern;
import static org.hamcrest.Matchers.*;


@Component
public class MatcherUtil {

    @Autowired
    public ScenarioSession scenarioSession;

    public <T> Matcher<?> getMatcher(MatcherEnum matcherEnum, T value) {
        Matcher<?> matcher = null;
        switch (matcherEnum) {
            case equals:
            case equalTo:
                matcher = equalTo(value);
                break;
            case notEqual:
            case notEqualTo:
                matcher = not(equalTo(value));
                break;
            case notNull:
            case present:
                matcher = notNullValue();
                break;
            case isNull:
            case notPresent:
            case isNot:
            case Null:
                matcher = Matchers.nullValue();
                break;
            case containsString:
                matcher = containsString((String) value);
                break;
            case datePattern:
                matcher = DatePatternMatcher.datePatternMatcher(value.toString());
                break;
            case pattern:
                matcher = matchesPattern((String) value);
                break;
            case containsAll:
                matcher = ContainsAllElementsMatcher.containsAllMatcher((String) value);
                break;
            case equalsBoolean:
                boolean val = true;
                if (value.toString().equalsIgnoreCase("false")) {
                    val = false;
                }
                matcher = equalTo(val);
                break;
            case equalsInt:
                try {
                    // try as an Integer
                    matcher = equalTo(Integer.parseInt(value.toString()));
                } catch (NumberFormatException nfe) {
                    // try as a Long
                    matcher = equalTo(Long.parseLong(value.toString()));
                }
                break;
            case path:
            case pathInt:
                Response response = (Response) scenarioSession.getData(SessionKey.LATEST_RESPONSE);
                String pathValue = response.jsonPath().get(value.toString()).toString();
                matcher = equalTo(pathValue);
                break;
            case greaterThan:
                try {
                    // try as an Integer
                    matcher = greaterThan(Integer.parseInt(value.toString()));
                } catch (NumberFormatException nfe) {
                    // try as a Long
                    matcher = greaterThan(Long.parseLong(value.toString()));
                }
                break;
            case lessThan:
                try {
                    // try as an Integer
                    matcher = lessThan(Integer.parseInt(value.toString()));
                } catch (NumberFormatException nfe) {
                    // try as a Long
                    matcher = lessThan(Long.parseLong(value.toString()));
                }
                break;
            case greaterThanOrEqualTo:
            case more:
                try {
                    // try as an Integer
                    matcher = greaterThanOrEqualTo(Integer.parseInt(value.toString()));
                } catch (NumberFormatException nfe) {
                    // try as a Long
                    matcher = greaterThanOrEqualTo(Long.parseLong(value.toString()));
                }
                break;
            case lessThanOrEqualTo:
            case less:
                try {
                    // try as an Integer
                    matcher = lessThanOrEqualTo(Integer.parseInt(value.toString()));
                } catch (NumberFormatException nfe) {
                    // try as a Long
                    matcher = lessThanOrEqualTo(Long.parseLong(value.toString()));
                }
                break;
            case is:
                matcher = is(not(Matchers.nullValue()));
                break;
            case arrayWithSize:
                matcher = hasSize(Integer.parseInt(value.toString()));
                break;
            case instanceOfString:
                matcher = is(instanceOf(String.class));
                break;
            case instanceOfInteger:
                matcher = is(instanceOf(Integer.class));
                break;
            case instanceOfBoolean:
                matcher = is(instanceOf(Boolean.class));
                break;
            case instanceOfArray:
                matcher = is(instanceOf(ArrayList.class));
                break;
            case instanceOfLong:
                matcher = is(instanceOf(Long.class));
                break;
            case emptyString:
                matcher = is(isEmptyString());
                break;
            case hasStringItems:
                //Value passed through as to be comma-separated e.g Value 1, Value 2
                matcher = hasItems(Arrays.asList(value.toString().trim().split("\\s*,\\s*")).toArray());
                break;
            case hasIntegerItems:
                //Value passed through as to be comma-separated e.g Value 1, Value 2
                List<Integer> numbers = Stream.of(value.toString().trim().split(","))
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
                matcher = hasItems(numbers.toArray());
                break;
        }
        return matcher;
    }

    public <T> ResponseSpecBuilder getResponseSpecBuilder(MatcherEnum matcherEnum, String path, T value) {
        ResponseSpecBuilder expSpec = new ResponseSpecBuilder();
        return expSpec.expectBody(path, getMatcher(matcherEnum, value));
    }
    public <T> ResponseSpecBuilder getResponseSpecBuilder(ResponseSpecBuilder responseSpecBuilder, MatcherEnum matcherEnum, String path, T value) {
        return responseSpecBuilder.expectBody(path, getMatcher(matcherEnum, value));
    }
}
