package com.toonetown.guava_ext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

/**
 * Static helper class for working with locales
 */
@Getter @RequiredArgsConstructor
public enum Language implements EnumLookup.Keyed<Locale> {
    ARABIC           (Locale.forLanguageTag("ar")),
    BULGARIAN        (Locale.forLanguageTag("bg")),
    CATALAN          (Locale.forLanguageTag("ca")),
    CHINESE_SIMP     (Locale.forLanguageTag("zh-Hans")),
    CHINESE_TRAD     (Locale.forLanguageTag("zh-Hant")),
    CROATIAN         (Locale.forLanguageTag("hr")),
    CZECH_REPUBLIC   (Locale.forLanguageTag("cs")),
    DANISH           (Locale.forLanguageTag("da")),
    DUTCH            (Locale.forLanguageTag("nl")),
    ENGLISH          (Locale.forLanguageTag("en")),
    ESTONIAN         (Locale.forLanguageTag("et")),
    FILIPINO         (Locale.forLanguageTag("tl")),
    FINNISH          (Locale.forLanguageTag("fi")),
    FRENCH           (Locale.forLanguageTag("fr")),
    GERMAN           (Locale.forLanguageTag("de")),
    GREEK            (Locale.forLanguageTag("el")),
    HATIAN_CREOLE    (Locale.forLanguageTag("ht")),
    HEBREW           (Locale.forLanguageTag("he")),
    HINDI            (Locale.forLanguageTag("hi")),
    HUNGARIAN        (Locale.forLanguageTag("hu")),
    INDONESIAN       (Locale.forLanguageTag("id")),
    ITALIAN          (Locale.forLanguageTag("it")),
    JAPANESE         (Locale.forLanguageTag("ja")),
    KOREAN           (Locale.forLanguageTag("ko")),
    LATVIAN          (Locale.forLanguageTag("lv")),
    LITHUANIAN       (Locale.forLanguageTag("lt")),
    MALAYSIAN        (Locale.forLanguageTag("ms")),
    MALTESE          (Locale.forLanguageTag("mt")),
    NORWEGIAN        (Locale.forLanguageTag("no")),
    PERSIAN          (Locale.forLanguageTag("fa")),
    POLISH           (Locale.forLanguageTag("pl")),
    PORTUGUESE       (Locale.forLanguageTag("pt")),
    ROMANIAN         (Locale.forLanguageTag("ro")),
    RUSSIAN          (Locale.forLanguageTag("ru")),
    SLOVAK           (Locale.forLanguageTag("sk")),
    SLOVENIAN        (Locale.forLanguageTag("sl")),
    SPANISH          (Locale.forLanguageTag("es")),
    SWEDISH          (Locale.forLanguageTag("sv")),
    THAI             (Locale.forLanguageTag("th")),
    TURKISH          (Locale.forLanguageTag("tr")),
    UKRANIAN         (Locale.forLanguageTag("uk")),
    URDU             (Locale.forLanguageTag("ur")),
    VIETNAMESE       (Locale.forLanguageTag("vi")),
    UNKNOWN          (Locale.ROOT);

    private final Locale value;

    /* Mapping of countries to scripts for when they are missing */
    private static final ImmutableMap<String, String> scripts = ImmutableMap.of("TW", "Hant",
                                                                                "CN", "Hans");

    /* All our values */
    private static final EnumLookup<Language, Locale> $ALL = EnumLookup.of(Language.class);

    private static Language tryFind(final Locale l) throws NotFoundException {
        final Locale.Builder b = new Locale.Builder().setLanguage(l.getLanguage()).setScript(l.getScript());
        if (Strings.isNullOrEmpty(l.getScript())) {
            b.setScript(scripts.get(l.getCountry().toUpperCase()));
        }
        final Language f = $ALL.find(b.build());
        if (f == UNKNOWN) {
            throw new NotFoundException("Language with locale " + l + " not found");
        }
        return f;
    }

    /** Finds a single language by locale - only the language (and script, if specified) is used to look up */
    public static Language find(final Locale l) throws NotFoundException {
        if (Locale.ROOT.equals(l)) {
            /* We only return unknown if we are exactly an empty locale */
            return UNKNOWN;
        }
        try {
            return tryFind(l);
        } catch (NotFoundException e) {
            if (!Strings.isNullOrEmpty(l.getScript())) {
                /* Let's try and fetch it with just the language and country itself */
                return tryFind(new Locale(l.getLanguage(), l.getCountry()));
            }
            throw e;
        }
    }
    public static Language find(final Locale l, final Language defaultVal) {
        try {
            return find(l);
        } catch (NotFoundException e) {
            return defaultVal;
        }
    }

    /* Find by string */
    public static Language find(final String s) throws NotFoundException {
        return find(Locale.forLanguageTag(s));
    }
    public static Language find(final String s, final Language defaultVal) {
        return find(Locale.forLanguageTag(s), defaultVal);
    }

    /** Returns all our Languages */
    public static Set<Language> all() { return $ALL.keySet(); }
}
