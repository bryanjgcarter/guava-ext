package com.toonetown.guava_ext.testing;

import org.testng.Assert;

/**
 * Additional helper assert functions
 */
public class ExtraAssert {
    /** Asserts that two double values are equal within the given precision */
    public static void assertEqualsWithin(final double v1, final double v2, final int precision) {
        final String fmt = "%." + (precision - 1) + "e";
        Assert.assertEquals(Double.parseDouble(String.format(fmt, v1)), Double.parseDouble(String.format(fmt, v2)));
    }

}
