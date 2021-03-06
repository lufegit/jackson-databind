package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Unit tests for checking that alternative settings for
 * {@link JsonSerialize#include} annotation property work
 * as expected.
 */
public class TestNullProperties
    extends BaseMapTest
{
    /*
    /**********************************************************
    /* Helper beans
    /**********************************************************
     */

    static class SimpleBean
    {
        public String getA() { return "a"; }
        public String getB() { return null; }
    }
    
    @JsonInclude(JsonInclude.Include.ALWAYS) // just to ensure default
    static class NoNullsBean
    {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getA() { return null; }

        public String getB() { return null; }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultBean
    {
        String _a = "a", _b = "b";

        NonDefaultBean() { }

        public String getA() { return _a; }
        public String getB() { return _b; }
    }

    static class MixedBean
    {
        String _a = "a", _b = "b";

        MixedBean() { }

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public String getA() { return _a; }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String getB() { return _b; }
    }

    // to ensure that default values work for collections as well
    static class ListBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public List<String> strings = new ArrayList<String>();
    }
    
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class ArrayBean {
        public int[] ints = new int[] { 1, 2 };
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    
    public void testGlobal() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new SimpleBean());
        assertEquals(2, result.size());
        assertEquals("a", result.get("a"));
        assertNull(result.get("b"));
        assertTrue(result.containsKey("b"));
    }

    public void testNonNullByClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        Map<String,Object> result = writeAndMap(m, new NoNullsBean());
        assertEquals(1, result.size());
        assertFalse(result.containsKey("a"));
        assertNull(result.get("a"));
        assertTrue(result.containsKey("b"));
        assertNull(result.get("b"));
    }

    public void testNonDefaultByClass() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        NonDefaultBean bean = new NonDefaultBean();
        // need to change one of defaults
        bean._a = "notA";
        Map<String,Object> result = writeAndMap(m, bean);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("a"));
        assertEquals("notA", result.get("a"));
        assertFalse(result.containsKey("b"));
        assertNull(result.get("b"));
    }

    public void testMixedMethod() throws IOException
    {
        ObjectMapper m = new ObjectMapper();

        MixedBean bean = new MixedBean();
        bean._a = "xyz";
        bean._b = null;
        Map<String,Object> result = writeAndMap(m, bean);
        assertEquals(1, result.size());
        assertEquals("xyz", result.get("a"));
        assertFalse(result.containsKey("b"));

        bean._a = "a";
        bean._b = "b";
        result = writeAndMap(m, bean);
        assertEquals(1, result.size());
        assertEquals("b", result.get("b"));
        assertFalse(result.containsKey("a"));
    }

    public void testDefaultForEmptyList() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("{}", m.writeValueAsString(new ListBean()));
    }

    // [JACKSON-531]: make NON_DEFAULT work for arrays too
    public void testNonEmptyDefaultArray() throws IOException
    {
        ObjectMapper m = new ObjectMapper();
        assertEquals("{}", m.writeValueAsString(new ArrayBean()));
    }
}
