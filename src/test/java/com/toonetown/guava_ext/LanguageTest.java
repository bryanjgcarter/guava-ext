package com.toonetown.guava_ext;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.util.Locale;

import com.toonetown.guava_ext.testing.DataProviders;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.tests;

/**
 * Unit test for locales
 */
public class LanguageTest {

    public static DataProviders.TestSet getUnitTestData() {
        /* All built-in java constants, except for chinese (unspecified) which should fail */
        return tests(params(Locale.ENGLISH, Language.ENGLISH),
                     params(Locale.FRENCH, Language.FRENCH),
                     params(Locale.GERMAN, Language.GERMAN),
                     params(Locale.ITALIAN, Language.ITALIAN),
                     params(Locale.JAPANESE, Language.JAPANESE),
                     params(Locale.KOREAN, Language.KOREAN),
                     params(Locale.SIMPLIFIED_CHINESE, Language.CHINESE_SIMP),
                     params(Locale.TRADITIONAL_CHINESE, Language.CHINESE_TRAD),
                     params(Locale.FRANCE, Language.FRENCH),
                     params(Locale.GERMANY, Language.GERMAN),
                     params(Locale.ITALY, Language.ITALIAN),
                     params(Locale.JAPAN, Language.JAPANESE),
                     params(Locale.KOREA, Language.KOREAN),
                     params(Locale.CHINA, Language.CHINESE_SIMP),
                     params(Locale.PRC, Language.CHINESE_SIMP),
                     params(Locale.TAIWAN, Language.CHINESE_TRAD),
                     params(Locale.UK, Language.ENGLISH),
                     params(Locale.US, Language.ENGLISH),
                     params(Locale.CANADA, Language.ENGLISH),
                     params(Locale.CANADA_FRENCH, Language.FRENCH),
                     params(Locale.ROOT, Language.UNKNOWN));
    }


    @DataProvider(name = "unitTestData", parallel = true)
    public Object[][] unitTestData() { return getUnitTestData().create(); }

    @Test(dataProvider = "unitTestData")
    public void testLanguage(final Locale locale, final Language language) throws NotFoundException {
        assertEquals(Language.find(locale), language);
        assertEquals(Language.find(locale.toLanguageTag()), language);
    }

    @Test
    public void testLanguageFind() throws NotFoundException {
        assertEquals(Language.find(new Locale("en", "US")), Language.ENGLISH);
        assertEquals(Language.find(new Locale("EN", "us")), Language.ENGLISH);
        assertEquals(Language.find(new Locale("es")), Language.SPANISH);
    }

    @Test
    public void testLanguageFind_string() throws NotFoundException {
        assertEquals(Language.find("en-US"), Language.ENGLISH);
        assertEquals(Language.find("es-mx"), Language.SPANISH);
        assertEquals(Language.find("es"), Language.SPANISH);
    }

    @Test
    public void testLanguageFind_script() throws NotFoundException {
        assertEquals(Language.find("zh-Hant"), Language.CHINESE_TRAD);
        assertEquals(Language.find("zh-hAnS"), Language.CHINESE_SIMP);
        assertEquals(Language.find("zh-Hans-CN"), Language.CHINESE_SIMP);
        assertEquals(Language.find("zh-Hant-CN"), Language.CHINESE_TRAD);
        assertEquals(Language.find("en-cyrl"), Language.ENGLISH);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testLanguageNotFound() throws NotFoundException {
        Language.find(new Locale("xx"));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testLanguageNotFound_string() throws NotFoundException {
        Language.find("xx");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testLanguageNotFound_chinese() throws NotFoundException {
        Language.find(Locale.CHINESE);
    }

}
