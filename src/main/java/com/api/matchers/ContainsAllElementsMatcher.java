package com.api.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Arrays;
import java.util.List;

public class ContainsAllElementsMatcher extends TypeSafeMatcher<List<String>> {

    String[] items;

    ContainsAllElementsMatcher(String items) {
        this.items = items.split(",");
    }

    @Override
    public boolean matchesSafely(List<String> list) {

        if (items.length != list.size()){
            return false;
        }

        for (String s : items) {
            if (list.indexOf(s) == -1 ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(Arrays.asList(items).toString());
    }

    public static <T> Matcher<List<String>> containsAllMatcher(String items) {
        return new ContainsAllElementsMatcher(items);
    }
}
