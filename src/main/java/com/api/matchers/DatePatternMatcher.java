package com.api.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DatePatternMatcher extends TypeSafeMatcher<String> {

        String datePattern;

        public DatePatternMatcher(String datePattern) {
            this.datePattern = datePattern;
        }

        @Override
        public boolean matchesSafely(String matchItem) {
            SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
            try {
                formatter.parse(matchItem);
            } catch (ParseException e) {
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(datePattern);
        }


        public static <T> Matcher<String> datePatternMatcher(String pattern) {
            return new DatePatternMatcher(pattern);
        }
}
