package com.toonetown.guava_ext;

import lombok.Getter;

import java.math.RoundingMode;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.common.math.DoubleMath;
import com.google.common.math.LongMath;

/**
 * A class - similar to java's TimeUnit class for converting/storing/etc of sizes
 */
@Getter
public enum SizeUnit implements EnumLookup.Keyed<String>, Formattable {
    BYTE        ("B", true, 0),
    /* Decimal-based versions */
    KILOBYTE    ("KB", true, 1),
    MEGABYTE    ("MB", true, 2),
    GIGABYTE    ("GB", true, 3),
    TERABYTE    ("TB", true, 4),
    /* Plus binary versions */
    KIBIBYTE    ("KiB", false, 1),
    MEBIBYTE    ("MiB", false, 2),
    GIBIBYTE    ("GiB", false, 3),
    TEBIBYTE    ("TiB", false, 4);

    private static final long BASE_DECIMAL = 1000;
    private static final long BASE_BINARY = 1024;

    private final String value;
    private final long numBytes;
    private final boolean isDecimal;
    private final boolean isBinary;

    private SizeUnit(final String value, final boolean isDecimal, final int pow) {
        this.value = value;
        this.numBytes = LongMath.pow((isDecimal ? BASE_DECIMAL : BASE_BINARY), pow);
        this.isDecimal = (pow > 0) ? isDecimal : true;
        this.isBinary = (pow > 0) ? (!isDecimal) : true;
    }

    public long toBytes(final double size) {
        return DoubleMath.roundToLong(size * numBytes, RoundingMode.HALF_EVEN);
    }

    public double convert(final double sourceSize, final SizeUnit sourceUnit) {
        return ((double) sourceUnit.toBytes(sourceSize)) / ((double) numBytes);
    }

    @Override public void formatTo(final Formatter formatter, final int flags, final int width, final int precision) {
        //TODO-NTOONE: Allow for formatting different lengths
        //TODO-NTOONE: Localize
        formatter.format("%s", value);
    }

    /* All our values */
    private static final EnumLookup<SizeUnit, String> $ALL = EnumLookup.of(SizeUnit.class);

    /** Finds a single SizeUnit by name, or ERROR if not found */
    public static SizeUnit find(final String c) throws NotFoundException { return $ALL.find(c); }

    /** Returns all our ResponseCodes */
    public static Set<SizeUnit> all() { return $ALL.keySet(); }
    public static Set<SizeUnit> allDecimal() { return Sets.filter(all(), Predicates.isDecimal()); }
    public static Set<SizeUnit> allBinary() { return Sets.filter(all(), Predicates.isBinary()); }

    /** A wrapper class for our predicates we use */
    private static class Predicates {
        public static Predicate<SizeUnit> isDecimal() {
            return new Predicate<SizeUnit>() {
                @Override public boolean apply(final SizeUnit input) {
                    return input == null ? false : input.isDecimal();
                }
            };
        }
        public static Predicate<SizeUnit> isBinary() {
            return new Predicate<SizeUnit>() {
                @Override public boolean apply(final SizeUnit input) {
                    return input == null ? false : input.isBinary();
                }
            };
        }
    }
}
