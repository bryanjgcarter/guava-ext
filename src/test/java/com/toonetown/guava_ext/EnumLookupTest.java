package com.toonetown.guava_ext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Set;

import com.google.common.collect.Sets;

import com.toonetown.guava_ext.EnumLookup;
import com.toonetown.guava_ext.NotFoundException;

/**
 * Unit test for EnumLookup
 */
public class EnumLookupTest {
    
    @Getter @RequiredArgsConstructor
    public static enum IntEnum implements EnumLookup.Keyed<Integer> {
        FIRST(1),
        SECOND(2),
        NONE(0);
        
        /** The value of this enum */
        private final Integer value;
        
        /* All our values */
        private static final EnumLookup<IntEnum, Integer> $ALL = EnumLookup.of(IntEnum.class);
        public static IntEnum find(final Integer i) throws NotFoundException { return $ALL.find(i); }
        public static Set<IntEnum> all() { return $ALL.keySet(); }
    }

    @Getter @RequiredArgsConstructor
    public static enum StringEnum implements EnumLookup.Keyed<String> {
        MINE("MyValue");
        
        /** The value of this enum */
        private final String value;
        
        /* All our values */
        private static final EnumLookup<StringEnum, String> $ALL = EnumLookup.of(StringEnum.class);
        public static StringEnum find(final String s) throws NotFoundException { return $ALL.find(s); }
        public static Set<StringEnum> all() { return $ALL.keySet(); }
        
        /* Case-sensitive lookup */
        private static final EnumLookup<StringEnum, String> $ALL_CASE = EnumLookup.of(StringEnum.class, true);
        public static StringEnum findCase(final String s) throws NotFoundException { return $ALL_CASE.find(s); }
    }
    
    @Getter @RequiredArgsConstructor
    public static enum DoubleEnum implements EnumLookup.MultiKeyed {
        DOUBLE("MyValue", 42);
        
        /** The value of this enum */
        private final String stringValue;
        
        /** The integer value of this enum */
        private final Integer intValue;
        
        @Override public Object[] getValue() { return new Object[] {stringValue, intValue}; }

        /* All our values */
        private static final EnumLookup<DoubleEnum, String> $ALL_BY_STRING = EnumLookup.of(DoubleEnum.class, 0);
        public static DoubleEnum find(final String s) throws NotFoundException { return $ALL_BY_STRING.find(s); }
        public static Set<DoubleEnum> all() { return $ALL_BY_STRING.keySet(); }
        
        /* Case-sensitive lookup */
        private static final EnumLookup<DoubleEnum, String> $ALL_CASE = EnumLookup.of(DoubleEnum.class, 0, true);
        public static DoubleEnum findCase(final String s) throws NotFoundException { return $ALL_CASE.find(s); }

        /* Lookup by integer */
        private static final EnumLookup<DoubleEnum, Integer> $ALL_BY_INT = EnumLookup.of(DoubleEnum.class, 1);
        public static DoubleEnum find(final Integer i) throws NotFoundException { return $ALL_BY_INT.find(i); }
    }

    @Test
    public void testFind() throws NotFoundException {
        assertEquals(IntEnum.find(1), IntEnum.FIRST);
        assertEquals(IntEnum.find(0), IntEnum.NONE);
    }
    
    @Test(expectedExceptions = NotFoundException.class)
    public void testNotFound() throws NotFoundException {
        IntEnum.find(3);
    }
    
    @Test
    public void testMapKeys() throws NotFoundException {
        assertEquals(IntEnum.all(), Sets.newHashSet(IntEnum.NONE, IntEnum.SECOND, IntEnum.FIRST));
    }
    
    @Test
    public void testCaseInsensitive() throws NotFoundException {
        assertEquals(StringEnum.find("MyValue"), StringEnum.MINE);
        assertEquals(StringEnum.find("MYVALUE"), StringEnum.MINE);
        assertEquals(StringEnum.find("myvalue"), StringEnum.MINE);
    }

    @Test
    public void testCaseSensitive() throws NotFoundException {
        assertEquals(StringEnum.findCase("MyValue"), StringEnum.MINE);

    }
    
    @Test(expectedExceptions = NotFoundException.class)
    public void testCaseSensitive_notFound() throws NotFoundException {
        StringEnum.findCase("MYVALUE");
    }

    @Test
    public void testDoubleKeyed() throws NotFoundException {
        assertEquals(DoubleEnum.find("MyValue"), DoubleEnum.DOUBLE);
        assertEquals(DoubleEnum.findCase("MyValue"), DoubleEnum.DOUBLE);
        assertEquals(DoubleEnum.find("myvalue"), DoubleEnum.DOUBLE);
        assertEquals(DoubleEnum.find(42), DoubleEnum.DOUBLE);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testDoubleKeyed_stringNotFound() throws NotFoundException {
        DoubleEnum.find("gone");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testDoubleKeyed_caseStringNotFound() throws NotFoundException {
        DoubleEnum.findCase("myvalue");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testDoubleKeyed_intNotFound() throws NotFoundException {
        DoubleEnum.find(1);
    }

}
