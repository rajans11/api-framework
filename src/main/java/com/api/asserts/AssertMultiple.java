package com.api.asserts;


import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.response.Response;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.springframework.stereotype.Component;

@Component
public class AssertMultiple {
    StringBuilder stringBuilder;

    public AssertMultiple() {
        stringBuilder = new StringBuilder();
    }

    public void assertEquals(String message, Object expected, Object actual) {
        try {
            Assert.assertEquals(message, expected, actual);
        } catch (AssertionError assertionError) {
            appendError(assertionError);
        }
    }

    public void assertEquals(Object expected, Object actual) {
        try {
            Assert.assertEquals(expected, actual);
        } catch (AssertionError assertionError) {
            appendError(assertionError);
        }
    }

    public void assertNotEquals(String message, Object expected, Object actual) {
        try {
            Assert.assertNotSame(message, expected, actual);
        } catch (AssertionError assertionError) {
            appendError(assertionError);
        }
    }

    public void assertNotNull(String message, Object actual) {
        try {
            Assert.assertNotNull(message, actual);
        } catch (AssertionError assertionError) {
            appendError(assertionError);
        }
    }


    public <T> void assertThat(String errorMessage, T actual, Matcher<? super T> matcher) {
        try {
            MatcherAssert.assertThat(errorMessage, actual, matcher);
        } catch (AssertionError assertionError) {

            appendError(assertionError);
        }
    }

    public <T> void assertThat(T actual, Matcher<? super T> matcher) {
        try {
            MatcherAssert.assertThat(actual, matcher);
        } catch (AssertionError assertionError) {

            appendError(assertionError);
        }
    }


    public void assertTrue(final String message, final boolean condition) {
        try {
            Assert.assertTrue(message, condition);
        } catch (AssertionError assertionError) {
            appendError(assertionError);
        }
    }

    public void assertFalse(final String message, final boolean condition) {
        try {
            Assert.assertFalse(message, condition);
        } catch (AssertionError assertionError) {
            appendError(assertionError);
        }
    }

    private void appendError(AssertionError assertionError) {
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(assertionError.getMessage());
    }

    public void assertVerifyAll() {
        if (!(stringBuilder.length() == 0)) {
            try {
                Assert.fail(stringBuilder.toString());
            } finally {
//                making sting stringBuilder to empty
                stringBuilder.setLength(0);
            }
        }

    }

    public void assertVerifyAll(String errorMessage) {
        if (!(stringBuilder.length() == 0)) {
            try {
                Assert.fail(errorMessage + "\n\n" + stringBuilder.toString());
            } finally {
//                making sting stringBuilder to empty
                stringBuilder.setLength(0);
            }
        }

    }

    public String getStringBugger() {
        return stringBuilder.toString();
    }

    public void assertThat(final String userErrorMessage, final ResponseSpecBuilder responseSpecBuilder, Response response) {
        try {
            responseSpecBuilder.build().validate(response);

        } catch (AssertionError assertionError) {
            appendError(userErrorMessage, assertionError);
        }
    }

    private void appendError(String userErrorMessage, AssertionError assertionError) {
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(System.getProperty("-----------------------------------------------------------"));
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(userErrorMessage);
        stringBuilder.append(System.getProperty("line.separator"));
        stringBuilder.append(assertionError.getMessage());
    }

}