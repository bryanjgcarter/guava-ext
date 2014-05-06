package com.toonetown.guava_ext;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import com.toonetown.guava_ext.testing.DataProviders;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.product;
import static com.toonetown.guava_ext.testing.DataProviders.tests;
import static com.toonetown.guava_ext.testing.ExtraAssert.assertEqualsWithin;

/**
 * Unit test for SizeUnit
 */
public class SizeUnitTest {

    public static DataProviders.TestSet getSizeUnits() {
        return tests(params(SizeUnit.BYTE, 1L),
                     params(SizeUnit.KILOBYTE, 1000L),
                     params(SizeUnit.MEGABYTE, 1000L * 1000L),
                     params(SizeUnit.GIGABYTE, 1000L * 1000L * 1000L),
                     params(SizeUnit.TERABYTE, 1000L * 1000L * 1000L * 1000L),
                     params(SizeUnit.KIBIBYTE, 1024L),
                     params(SizeUnit.MEBIBYTE, 1024L * 1024L),
                     params(SizeUnit.GIBIBYTE, 1024L * 1024L * 1024L),
                     params(SizeUnit.TEBIBYTE, 1024L * 1024L * 1024L * 1024L));
    }
    public static DataProviders.TestSet getUnitCombos() {
        return product(getSizeUnits(), getSizeUnits());
    }

    @DataProvider(name = "sizeUnits", parallel = true)
    public Object[][] sizeUnits() { return getSizeUnits().create(); }

    @DataProvider(name = "unitCombos", parallel = true)
    public Object[][] unitCombos() { return getUnitCombos().create(); }

    @Test(dataProvider = "sizeUnits")
    public void testNumBytes(final SizeUnit unit, final long numBytes) {
        assertEquals(unit.getNumBytes(), numBytes);
    }

    @Test(dataProvider = "sizeUnits")
    public void testDownConvert(final SizeUnit unit, final long numBytes) {
        assertEquals(SizeUnit.BYTE.convert(1.0, unit), (double) numBytes);
    }

    @Test(dataProvider = "sizeUnits")
    public void testUpConvert(final SizeUnit unit, final long numBytes) {
        assertEquals(unit.convert((double) numBytes, SizeUnit.BYTE), 1.0);
    }

    @Test(dataProvider = "unitCombos")
    public void testCrossConvert(final SizeUnit unit1, final long numBytes1,
                                 final SizeUnit unit2, final long numBytes2) {
        /* Equal to within 2 significant figures */
        assertEqualsWithin(unit1.convert(123.45, unit2), 123.45 * (((double) numBytes2) / ((double) numBytes1)), 2);
    }
}
