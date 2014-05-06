package com.toonetown.guava_ext;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.escape.Escaper;

import com.toonetown.guava_ext.UriEscaper;

/**
 * Unit test to check UriEscaper
 */
public class UriEscaperTest {
    @Test
    public void testEscape() {
        final Escaper e = UriEscaper.instance();
        assertEquals(e.escape("http://site.com/my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                 "http://site.com/my%20path/?this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escape("site.com:8080/a:/b/c"), "site.com:8080/a:/b/c");
        assertEquals(e.escape("http://site.com:8000/a:/b/c"), "http://site.com:8000/a:/b/c");
        assertEquals(e.escape("site.com/?1 2+3%204"), "site.com/?1+2+3%204");
        assertEquals(e.escape("a:b@site.com:8080/a:@/b/c"), "a:b@site.com:8080/a:@/b/c");
        assertEquals(e.escape("http://a:b@site.com:8000/a:@/b/c"), "http://a:b@site.com:8000/a:@/b/c");
        assertEquals(e.escape("http://site.com:8080/a/b/c.html?me=http://test.com:8000/a/b"),
                              "http://site.com:8080/a/b/c.html?me=http://test.com:8000/a/b");
    }

    @Test
    public void testEscape_strict() {
        final Escaper e = UriEscaper.strictInstance();
        assertEquals(e.escape("http://site.com/my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                 "http://site.com/my%20path/?this=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escape("site.com:8080/a:/b/c"), "site.com:8080/a%3A/b/c");
        assertEquals(e.escape("http://site.com:8000/a:/b/c"), "http://site.com:8000/a%3A/b/c");
        assertEquals(e.escape("site.com/?1 2+3%204"), "site.com/?1%202%203%204");
        assertEquals(e.escape("a:b@site.com:8080/a:@/b/c"), "a:b@site.com:8080/a%3A%40/b/c");
        assertEquals(e.escape("http://a:b@site.com:8000/a:@/b/c"), "http://a:b@site.com:8000/a%3A%40/b/c");
        assertEquals(e.escape("http://site.com:8080/a/b/c.html?me=http://test.com:8000/a/b"),
                              "http://site.com:8080/a/b/c.html?me=http%3A%2F%2Ftest.com%3A8000%2Fa%2Fb");
    }

    @Test
    public void testStrictness() throws URISyntaxException {
        final Escaper esc = UriEscaper.instance();
        final Escaper strict = UriEscaper.strictInstance();
        final String s = "http://site.com/my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m";
        final String sStrict = strict.escape(s);
        /* Encoding a strict string should yield the same string */
        assertEquals(esc.escape(sStrict), sStrict);

        final String sEsc = esc.escape(s);
        /* Strictly-encoding a non-strict string does not necessarily yield the same string */
        assertNotEquals(strict.escape(sEsc), sEsc);
        /* But it should be the same as the strict string */
        assertEquals(strict.escape(sEsc), sStrict);
    }

    @Test
    public void testEscapePath() {
        final UriEscaper e = UriEscaper.instance();
        assertEquals(e.escapePath("my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                  "my%20path/?this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
    }

    @Test
    public void testEscapePath_strict() {
        final UriEscaper e = UriEscaper.strictInstance();
        assertEquals(e.escapePath("my path/?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                 "my%20path/?this=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
    }

    @Test
    public void testEscapeQuery() {
        final UriEscaper e = UriEscaper.instance();
        assertEquals(e.escapeQuery("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                   "this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escapeQuery("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                   "?this=with+space's&money=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escapeQuery("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                   "this=with+space's&money?=$/%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escapeQuery("http://test.com:8000/a/b"), "http://test.com:8000/a/b");
    }

    @Test
    public void testEscapeQuery_strict() {
        final UriEscaper e = UriEscaper.strictInstance();
        assertEquals(e.escapeQuery("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                   "this=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escapeQuery("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                   "%3Fthis=with%20space%27s&money=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escapeQuery("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                                   "this=with%20space%27s&money%3F=$%2F%E2%82%AC&rate=5%25apr%26terms%25a#b%20m");
        assertEquals(e.escapeQuery("http://test.com:8000/a/b"), "http%3A%2F%2Ftest.com%3A8000%2Fa%2Fb");
    }

    @Test
    public void testEscapeQueryParam() {
        final UriEscaper e = UriEscaper.instance();
        assertEquals(e.escapeQueryParam("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                     "this%3Dwith+space's%26money%3D$/%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b+m");
        assertEquals(e.escapeQueryParam("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                     "?this%3Dwith+space's%26money%3D$/%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b+m");
        assertEquals(e.escapeQueryParam("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                     "this%3Dwith+space's%26money?%3D$/%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b+m");
        assertEquals(e.escapeQueryParam("http://test.com:8000/a/b"), "http://test.com:8000/a/b");
    }

    @Test
    public void testEscapeQueryParam_strict() {
        final UriEscaper e = UriEscaper.strictInstance();
        assertEquals(e.escapeQueryParam("this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                     "this%3Dwith%20space%27s%26money%3D%24%2F%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b%20m");
        assertEquals(e.escapeQueryParam("?this=with space's&money=$/\u20AC&rate=5%apr%26terms%a#b m"),
                     "%3Fthis%3Dwith%20space%27s%26money%3D%24%2F%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b%20m");
        assertEquals(e.escapeQueryParam("this=with space's&money?=$/\u20AC&rate=5%apr%26terms%a#b m"),
                     "this%3Dwith%20space%27s%26money%3F%3D%24%2F%E2%82%AC%26rate%3D5%25apr%26terms%25a%23b%20m");
        assertEquals(e.escapeQueryParam("http://test.com:8000/a/b"), "http%3A%2F%2Ftest.com%3A8000%2Fa%2Fb");
    }

    @Test
    public void testEscapeFragment() {
        final UriEscaper e = UriEscaper.instance();
        assertEquals(e.escapeFragment("b m"), "b%20m");
        assertEquals(e.escapeFragment("#b m"), "%23b%20m");
        assertEquals(e.escapeFragment("b #m"), "b%20%23m");
    }
    
    @Test
    public void testEscapeFragment_strict() {
        final UriEscaper e = UriEscaper.strictInstance();
        assertEquals(e.escapeFragment("b m"), "b%20m");
        assertEquals(e.escapeFragment("#b m"), "%23b%20m");
        assertEquals(e.escapeFragment("b #m"), "b%20%23m");
    }
    
    @Test
    public void testProperEscaping() throws URISyntaxException {
        /* Tests whether or not we are able to make a URI from any possible character */
        final Escaper e = UriEscaper.instance();
        /* For speed, we just test *some* of the higher-level characters */
        for (char c = 0x0000; c <= 0x0FFF; c++) {
            assertNotNull(new URI(e.escape("http://site.com/" + c + "/file.html?val=" + c + "#" + c)));
        }
    }
    
    @Test
    public void testEscapeNull() throws URISyntaxException {
        assertNull(UriEscaper.instance().escape(null));
    }

    @Test
    public void testEscapeStrictChars() {
        // This is the set of characters from the BOSS API.  When we want a plus, it should be encoded, since
        // we lean towards "+" meaning space (relying upon the browser to encode it for us)
        assertEquals(UriEscaper.instance().escapeQueryParam("/?&;:@,$= %\"%2B#*<>{}|[]^\\`()"),
                     "/?%26;:@,$%3D+%25%22%2B%23*%3C%3E%7B%7D%7C%5B%5D%5E%5C%60()");
        assertEquals(UriEscaper.strictInstance().escapeQueryParam("/?&;:@,$= %\"%2B#*<>{}|[]^\\`()"),
                     "%2F%3F%26%3B%3A%40%2C%24%3D%20%25%22%2B%23%2A%3C%3E%7B%7D%7C%5B%5D%5E%5C%60%28%29");
        assertEquals(UriEscaper.strictInstance().escapeQueryParam("/?&;:@,$= %\"+#*<>{}|[]^\\`()"),
                     "%2F%3F%26%3B%3A%40%2C%24%3D%20%25%22%2B%23%2A%3C%3E%7B%7D%7C%5B%5D%5E%5C%60%28%29");
    }

}
