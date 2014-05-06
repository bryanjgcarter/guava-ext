package com.toonetown.guava_ext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * A smart enum which represents various network protocols and ports for URLs
 */
@Getter @RequiredArgsConstructor
public enum NetProtocol implements EnumLookup.MultiKeyed {
    AFP     ("afp", 548),
    DNS     ("dns", 53),
    FTP     ("ftp", 21),
    GOPHER  ("gopher", 70),
    HTTP    ("http", 80),
    HTTPS   ("https", 443),
    IMAP    ("imap", 143),
    IPP     ("ipp", 631),
    IRIS    ("iris", 702),
    LDAP    ("ldap", 389),
    NNTP    ("nntp", 119),
    POP     ("pop", 110),
    RTSP    ("rtsp", 554),
    SMB     ("smb", 445),
    SMTP    ("smtp", 25),
    SNMP    ("snmp", 162),
    SSH     ("ssh", 22),
    TELNET  ("telnet", 23);

    /** The scheme of this protocol */
    private final String scheme;

    /** The port for this protocol */
    private final Integer port;

    @Override public Object[] getValue() { return new Object[] {scheme, port}; }

    /* All our values */
    private static final EnumLookup<NetProtocol, String> $BY_SCHEME = EnumLookup.of(NetProtocol.class, 0);
    private static final EnumLookup<NetProtocol, Integer> $BY_PORT = EnumLookup.of(NetProtocol.class, 1);

    /** Finds a single NetProtocol by scheme */
    public static NetProtocol find(final String s) throws NotFoundException { return $BY_SCHEME.find(s); }
    public static NetProtocol find(final String s, final NetProtocol d) { return $BY_SCHEME.find(s, d); }

    /** Finds a single NetProtocol by scheme */
    public static NetProtocol find(final Integer p) throws NotFoundException { return $BY_PORT.find(p); }
    public static NetProtocol find(final Integer p, final NetProtocol d) { return $BY_PORT.find(p, d); }

    /** Returns all our NetProtocols */
    public static Set<NetProtocol> all() { return $BY_SCHEME.keySet(); }
}
