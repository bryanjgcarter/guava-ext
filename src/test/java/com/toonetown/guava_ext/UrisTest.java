package com.toonetown.guava_ext;

import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import static org.testng.Assert.*;

import java.util.Set;
import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableSet;

import com.toonetown.guava_ext.testing.DataProviders;
import static com.toonetown.guava_ext.testing.DataProviders.concat;
import static com.toonetown.guava_ext.testing.DataProviders.params;
import static com.toonetown.guava_ext.testing.DataProviders.tests;

/**
 * Unit test to check URI utilities
 */
public class UrisTest {

    private static URI u(final String u) throws URISyntaxException { return new URI(u); }

        public static DataProviders.TestSet getTestUrls(final boolean strict) throws URISyntaxException {
        return tests(params(strict, Uris.newUri("site.com", strict), u("http://site.com:80/")),
                     params(strict, Uris.newUri("https://site.com", strict), u("https://site.com:443/")),
                     params(strict, Uris.newUri("http://site.com:8080", strict), u("http://site.com:8080/")),
                     params(strict, Uris.newUri("http://site.com/", strict), u("http://site.com:80/")),
                     params(strict, Uris.newUri("http://site.com/path", strict), u("http://site.com:80/path")),
                     params(strict, Uris.newUri("http://site.com/path/file.html", strict),
                            u("http://site.com:80/path/file.html")),
                     params(strict, Uris.newUri("http://site.com?this=that", strict),
                            u("http://site.com:80/?this=that")),
                     params(strict, Uris.newUri("http://site.com/path?this=that", strict),
                            u("http://site.com:80/path?this=that")),
                     params(strict, Uris.newUri("http://site.com/my path/?this=with space's%26such&things", strict),
                            strict ? u("http://site.com:80/my%20path/?this=with%20space%27s%26such&things") :
                            u("http://site.com:80/my%20path/?this=with+space's%26such&things")),
                     params(strict, Uris.newUri("http://site.com/path?", strict), u("http://site.com:80/path")),
                     params(strict, Uris.newUri("http://user:pass@site.com", strict),
                            u("http://user:pass@site.com:80/")),
                     params(strict, Uris.newUri("http://user:p%40ss@site.com", strict),
                            u("http://user:p%40ss@site.com:80/")));
    }

    public static DataProviders.TestSet getUnitTestData() throws URISyntaxException {
        return concat(getTestUrls(true), getTestUrls(false));
    }

    public static DataProviders.TestSet getStrictnessData() {
        return tests(params(true), params(false));
    }

    @DataProvider(name = "unitTestData", parallel = true)
    public Object[][] unitTestData() throws URISyntaxException { return getUnitTestData().create(); }

    @DataProvider(name = "strictnessData", parallel = true)
    public Object[][] createStrictnessData() { return getStrictnessData().create(); }

    private void assertNormalized(final boolean strict, final URI uri) throws URISyntaxException {
        assertEquals(Uris.getScheme(uri), uri.getScheme());
        assertEquals(Uris.getUserInfo(uri), uri.getUserInfo());
        assertEquals(Uris.getRawUserInfo(uri), uri.getRawUserInfo());
        assertEquals(Uris.getHost(uri), uri.getHost());
        assertEquals(Uris.getPort(uri), uri.getPort());
        assertEquals(Uris.getPath(uri), uri.getPath());
        assertEquals(Uris.getRawPath(uri, strict), uri.getRawPath());
        assertEquals(Uris.getQuery(uri), uri.getQuery());
        assertEquals(Uris.getRawQuery(uri, strict), uri.getRawQuery());
        assertEquals(Uris.getFragment(uri), uri.getFragment());
        assertEquals(Uris.getRawFragment(uri, strict), uri.getRawFragment());
    }
    
    @Test(dataProvider = "unitTestData")
    public void testNew(final boolean strict, final URI newUri, final URI expectedUri) throws URISyntaxException {
        assertEquals(newUri, expectedUri);
        assertNormalized(strict, newUri);
    }

    @Test(dataProvider = "strictnessData")
    public void testNormalization(final boolean strict) throws URISyntaxException {
        /* Create a combo of all sorts of stuff */
        final List<Set<String>> sets = Lists.newArrayList();
        sets.add(ImmutableSet.of("//", "http://"));
        sets.add(ImmutableSet.of("", "a:b@", "a@", "@"));
        sets.add(ImmutableSet.of("", "site.com"));
        sets.add(ImmutableSet.of("", ":80"));
        sets.add(ImmutableSet.of("", "/", "/a/b/", "/with%20spaces/"));
        sets.add(ImmutableSet.of("", "c", "other.html", "my%20file.jsp"));
        sets.add(ImmutableSet.of("", "?", "?this=that", strict ? "?val=with%20space%27s" : "?val=with+space%27s"));
        sets.add(ImmutableSet.of("", "#", "#here", "#or%20there"));
        final Set<List<String>> combo = Sets.cartesianProduct(sets);
        for (final List<String> l : combo) {
            /* Whether or not it should be normalized */
            final boolean norm = l.get(0).equals("http://") &&
                                    !l.get(1).equals("@") &&
                                    l.get(2).equals("site.com") &&
                                    l.get(3).equals(":80") &&
                                    !l.get(4).equals("") &&
                                    !l.get(6).equals("?") &&
                                    !l.get(7).equals("#");
            final String url = Joiner.on("").join(l);
            
            /* We can't even create a Java URI from these two */
            if (!url.equals("//") && !url.equals("http://")) {
                final URI u = u(url);
                URI normU;
                try {
                    normU = Uris.normalize(u, strict);
                } catch (Uris.NormalizationException e) {
                    if (!norm) {
                        /* We are fine not normalizing something that we couldn't normalize */
                        normU = null;
                    } else {
                        throw e;
                    }
                }
                if (norm && normU != null) {
                    assertEquals(normU, u);
                    assertTrue(Uris.isNormalized(u, strict));
                    assertNormalized(strict, u);
                } else {
                    assertNotEquals(normU, u);
                    assertFalse(Uris.isNormalized(u, strict));
                }
            }
        }
    }

    @Test(dataProvider = "strictnessData")
    public void testNew_unequal(final boolean strict) throws URISyntaxException {
        assertNotEquals(Uris.newUri("site.com", strict), u("http://site.com:80"));
    }

    @Test(dataProvider = "strictnessData",
          expectedExceptions = Uris.NormalizationException.class,
          expectedExceptionsMessageRegExp = "^No host in URI: .*")
    public void testNew_noHost(final boolean strict) throws URISyntaxException {
        Uris.newUri("file:///", strict);
    }
    
    @Test(dataProvider = "strictnessData",
          expectedExceptions = Uris.NormalizationException.class,
          expectedExceptionsMessageRegExp = "^Cannot determine port: .*")
    public void testNew_unknownPort(final boolean strict) throws URISyntaxException {
        Uris.newUri("fakeproto://site.com", strict);
    }
    
    @Test(dataProvider = "strictnessData",
          expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "^Cannot create URI for null or empty value$")
    public void testNew_null(final boolean strict) throws URISyntaxException {
        Uris.newUri(null, strict);
    }

    @Test(dataProvider = "strictnessData",
          expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "^Cannot create URI for null or empty value$")
    public void testNew_empty(final boolean strict) throws URISyntaxException {
        Uris.newUri("", strict);
    }

    @Test
    public void testGetDirectory() throws URISyntaxException {
        assertEquals(Uris.getDirectory(u("http://site.com/a/b/c.html?this=that")), "/a/b/");
        assertEquals(Uris.getDirectory(u("http://site.com/a/b/c?this=that")), "/a/b/");
        assertEquals(Uris.getDirectory(u("http://site.com/a/b/c/?this=that")), "/a/b/c/");
        assertEquals(Uris.getDirectory(u("http://site.com/")), "/");
        assertEquals(Uris.getDirectory(u("http://site.com")), "/");
    }

    @Test
    public void testGetFile() throws URISyntaxException {
        assertEquals(Uris.getFile(u("http://site.com/a/b/c.html?this=that")), "c.html");
        assertEquals(Uris.getFile(u("http://site.com/a/b/c?this=that")), "c");
        assertEquals(Uris.getFile(u("http://site.com/a/b/c/?this=that")), "");
        assertEquals(Uris.getFile(u("http://site.com/")), "");
        assertEquals(Uris.getFile(u("http://site.com")), "");
    }

    @Test(dataProvider = "strictnessData")
    public void testToDomain(final boolean strict) throws URISyntaxException {
        final URI u = Uris.newUri("http://site.com/", strict);
        
        assertEquals(Uris.toDomain(Uris.newUri("site.com/a/b/c.html?this=that", false), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/a/b/c.html?this=that"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/a/b/c?this=that"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/a/b/c/?this=that"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com/"), strict), u);
        assertEquals(Uris.toDomain(u("http://site.com"), strict), u);
    }

    @Test(dataProvider = "strictnessData")
    public void testToDirectory(final boolean strict) throws URISyntaxException {
        final URI u1 = Uris.newUri("http://site.com/a/b/", strict);
        final URI u2 = Uris.newUri("http://site.com/a/b/c/", strict);
        final URI u3 = Uris.newUri("http://site.com/", strict);
        
        assertEquals(Uris.toDirectory(Uris.newUri("site.com/a/b/c.html?this=that", false), strict), u1);
        assertEquals(Uris.toDirectory(u("http://site.com/a/b/c.html?this=that"), strict), u1);
        assertEquals(Uris.toDirectory(u("http://site.com/a/b/c?this=that"), strict), u1);
        assertEquals(Uris.toDirectory(u("http://site.com/a/b/c/?this=that"), strict), u2);
        assertEquals(Uris.toDirectory(u("http://site.com/"), strict), u3);
        assertEquals(Uris.toDirectory(u("http://site.com"), strict), u3);
        assertEquals(Uris.toDirectory(Uris.newUri("http://site.com/with space/file?this=that", false), strict),
                                      Uris.newUri("http://site.com/with space/", strict));
        assertEquals(Uris.toDirectory(Uris.newUri("http://site.com/with space/file?this=that", false), strict),
                                      Uris.newUri("http://site.com/with%20space/", strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testToPath(final boolean strict) throws URISyntaxException {
        final URI u1 = Uris.newUri("http://site.com/a/b/c.html", strict);
        final URI u2 = Uris.newUri("http://site.com/a/b/c", strict);
        final URI u3 = Uris.newUri("http://site.com/a/b/c/", strict);
        final URI u4 = Uris.newUri("http://site.com/", strict);
        
        assertEquals(Uris.toPath(Uris.newUri("site.com/a/b/c.html?this=that", false), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c.html?this=that"), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c.html?"), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c.html"), strict), u1);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c?this=that"), strict), u2);
        assertEquals(Uris.toPath(u("http://site.com/a/b/c/?this=that"), strict), u3);
        assertEquals(Uris.toPath(u("http://site.com/"), strict), u4);
        assertEquals(Uris.toPath(u("http://site.com"), strict), u4);
        assertEquals(Uris.toPath(Uris.newUri("http://site.com/with space/f s?this=that", false), strict),
                     Uris.newUri("http://site.com/with space/f%20s", strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testResolve_absolute(final boolean strict) throws URISyntaxException {
        final URI base = u("http://site.com/a/b/c.html?this=that");
        
        assertEquals(Uris.resolve(base, "/other.html", strict),
                     Uris.newUri("http://site.com/other.html", strict));
        assertEquals(Uris.resolve(base, "/d/e/f?more=true", strict),
                     Uris.newUri("http://site.com/d/e/f?more=true", strict));
        assertEquals(Uris.resolve(base, "/?more=true", strict),
                     Uris.newUri("http://site.com/?more=true", strict));
        assertEquals(Uris.resolve(base, "/with space", strict),
                     Uris.newUri("http://site.com/with%20space", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(base, "/other.html", strict), strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testResolve_relative(final boolean strict) throws URISyntaxException {
        final URI base = u("http://site.com/a/b/c.html?this=that");
        
        assertEquals(Uris.resolve(base, "other.html", strict),
                     Uris.newUri("http://site.com/a/b/other.html", strict));
        assertEquals(Uris.resolve(base, "d/e/f?more=true", strict),
                     Uris.newUri("http://site.com/a/b/d/e/f?more=true", strict));
        assertEquals(Uris.resolve(base, "with space", strict),
                     Uris.newUri("http://site.com/a/b/with%20space", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(base, "other.html", strict), strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testResolve_relativeDir(final boolean strict) throws URISyntaxException {
        final URI base = u("http://site.com/a/b/c.html?this=that");

        assertEquals(Uris.resolve(base, "./other.html", strict),
                     Uris.newUri("http://site.com/a/b/c.html/other.html", strict));
        assertEquals(Uris.resolve(base, "./d/e/f?more=true", strict),
                     Uris.newUri("http://site.com/a/b/c.html/d/e/f?more=true", strict));
        assertEquals(Uris.resolve(base, "./with space", strict),
                     Uris.newUri("http://site.com/a/b/c.html/with%20space", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(base, "./other.html", strict), strict));
    }


    @Test(dataProvider = "strictnessData")
    public void testResolve_query(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "?more=true", strict),
                        Uris.newUri("http://site.com/a/b/c.html?more=true", strict));
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html"), "?more=true", strict),
                        Uris.newUri("http://site.com/a/b/c.html?more=true", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "?more=true", strict),
                                     strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testResolve_queryRelative(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "&more=true", strict),
                     Uris.newUri("http://site.com/a/b/c.html?this=that&more=true", strict));
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html"), "&more=true", strict),
                     Uris.newUri("http://site.com/a/b/c.html?more=true", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "&more=true", strict),
                                     strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testResolve_fragment(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "#here", strict),
                        Uris.newUri("http://site.com/a/b/c.html?this=that#here", strict));
        assertEquals(Uris.resolve(u("http://site.com/a/b/c.html?this=that#there"), "#here", strict),
                        Uris.newUri("http://site.com/a/b/c.html?this=that#here", strict));
        assertEquals(Uris.resolve(u("http://site.com"), "#here", strict),
                     Uris.newUri("http://site.com/#here", strict));
        assertTrue(Uris.isNormalized(Uris.resolve(u("http://site.com/a/b/c.html?this=that"), "#here", strict),
                                     strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testResolveParams(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.resolveParams(u("http://site.com/a/b/c.html?this=that"),
                                        ImmutableMap.of("more", "true"),
                                        strict),
                     Uris.newUri("http://site.com/a/b/c.html?this=that&more=true", strict));
        assertEquals(Uris.resolveParams(u("http://site.com/a/b/c.html"),
                                        ImmutableMap.of("more", "true", "money", "20$ bill&note"),
                                        strict),
                     Uris.newUri("http://site.com/a/b/c.html?more=true&money=20" +
                                 (strict ? "%24" : "$") +
                                 "+bill%26note",
                                 strict));
        assertTrue(Uris.isNormalized(Uris.resolveParams(u("http://site.com/a/b/c.html"),
                                                        ImmutableMap.of("more", "true", "money", "20$ bill&note"),
                                                        strict),
                                     strict));
    }


    private void assertEncoding(final boolean strict,
                                final String url,
                                final String expected) throws URISyntaxException {
        final URI u = Uris.newUri(url, strict);
        assertEquals(u.toString(), expected);
        assertEquals(Uris.toRawString(u, strict), expected);
        assertTrue(Uris.isNormalized(u, strict));
        assertEquals(u, URI.create(expected));
    }
    
    @Test(dataProvider = "strictnessData")
    public void testEncoding(final boolean strict) throws URISyntaxException {
        assertEncoding(strict,
                       "site.com/a b/?c=d e's&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d+e's&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
        assertEncoding(strict,
                       "site.com/a b/?c=d%20e's&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d%20e's&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
        assertEncoding(strict,
                       "site.com/a b/?c=d+e's&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d+e's&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
        assertEncoding(strict,
                       "site.com/a%20b/?c=d e%27s&m=$/\u20AC&r=5%apr%26t%a#b m",
                       strict ?
                           "http://site.com:80/a%20b/?c=d%20e%27s&m=$%2F%E2%82%AC&r=5%25apr%26t%25a#b%20m" :
                           "http://site.com:80/a%20b/?c=d+e%27s&m=$/%E2%82%AC&r=5%25apr%26t%25a#b%20m");
    }

    @Test(dataProvider = "strictnessData")
    public void testReplaceHost(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.replaceHost(new URI("http://test.com"), "site.com", strict),
                     Uris.newUri("http://site.com", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/"), "site.com", strict),
                     Uris.newUri("http://site.com/", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com:8080"), "site.com", strict),
                     Uris.newUri("http://site.com:8080", strict));
        assertEquals(Uris.replaceHost(new URI("https://test.com"), "site.com", strict),
                     Uris.newUri("https://site.com/", strict));
        assertEquals(Uris.replaceHost(new URI("http://a:b@test.com"), "site.com", strict),
                     Uris.newUri("http://a:b@site.com", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/a/b/c.html?this=that"), "site.com", strict),
                     Uris.newUri("http://site.com/a/b/c.html?this=that", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/#bm"), "site.com", strict),
                     Uris.newUri("http://site.com/#bm", strict));
        assertEquals(Uris.replaceHost(new URI("http://test.com/a/b/c.html?me=test.com"), "site.com", strict),
                     Uris.newUri("http://site.com/a/b/c.html?me=test.com", strict));
    }

    @Test(dataProvider = "strictnessData")
    public void testPrependHost(final boolean strict) throws URISyntaxException {
        assertEquals(Uris.replaceHost(new URI("http://test.com"), "site.test.com", strict),
                     Uris.newUri("http://site.test.com", strict));
    }

    @Test(dataProvider = "strictnessData",
          expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "^Cannot create URI for null or empty value$")
    public void testReplaceHost_null(final boolean strict) throws URISyntaxException {
        Uris.replaceHost(new URI("http://test.com"), null, strict);
    }

    @Test(dataProvider = "strictnessData",
          expectedExceptions = NullPointerException.class,
          expectedExceptionsMessageRegExp = "^Cannot create URI for null or empty value$")
    public void testReplaceHost_empty(final boolean strict) throws URISyntaxException {
        Uris.replaceHost(new URI("http://test.com"), "", strict);
    }
}
