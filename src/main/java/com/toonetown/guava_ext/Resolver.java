package com.toonetown.guava_ext;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.Iterables;
import com.google.common.net.InetAddresses;

/**
 * A class which will resolve host names into InetAddress objects.
 */
public abstract class Resolver {

    /**
     * Resolves a single host (known not to be an IP address) to a collection of InetAddress objects
     *
     * @param host the host to resolve
     * @return a collection (possibly empty) of InetAddresses that it resolved.
     * @throws UnknownHostException can be thrown
     */
    protected abstract Collection<InetAddress> resolveHost(final String host) throws UnknownHostException;

    /**
     * Resolves the host or ip address to multiple values.
     *
     * @param hostOrIp the host or IP to resolve
     * @return a collection (possibly empty) of InetAddresses that it resolved.
     */
    public Collection<InetAddress> resolveAll(final String hostOrIp) {
        if (InetAddresses.isInetAddress(hostOrIp)) {
            /* It is an IP address - just return a single IP address */
            return Collections.singleton(InetAddresses.forString(hostOrIp));
        }
        try {
            return resolveHost(hostOrIp);
        } catch (UnknownHostException e) {
            return Collections.emptySet();
        }
    }

    /**
     * Resolves the given host or IP address to an InetAddress.  This avoids a lookup if the given value is an ip
     * address.
     *
     * @param hostOrIp the host or IP to resolve
     * @return the resolved address
     * @throws UnknownHostException if the resolver couldn't find the host
     */
    public InetAddress resolve(final String hostOrIp) throws UnknownHostException {
        try {
            return Iterables.get(resolveAll(hostOrIp), 0);
        } catch (IndexOutOfBoundsException e) {
            /* We didn't get one - so we throw now */
            throw new UnknownHostException(hostOrIp);
        }
    }

    /**
     * Same as resolve() but returns null instead of throwing an exception
     *
     * @param hostOrIp the host or IP to resolve
     * @return the resolved address or null if not found
     */
    public InetAddress resolveOrNull(final String hostOrIp) {
        try {
            return resolve(hostOrIp);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Same as resolve() but returns the host resolved to the loopback instead of throwing an exception
     *
     * @param hostOrIp the host or IP to resolve
     * @return the resolved address (possibly resolved to loopback)
     */
    public InetAddress resolveOrLoopback(final String hostOrIp) {
        try {
            return resolve(hostOrIp);
        } catch (UnknownHostException e) {
            return createLoopbackAddr(hostOrIp);
        }
    }

    /**
     * Creates an InetAddress for a given hostName and ipAddress string.  This avoids doing a DNS lookup and can be
     * used by subclasses to create the InetAddress objects.
     *
     * @param hostName the hostname
     * @param ipAddress the ip address
     * @return a new InetAddress
     * @throws UnknownHostException if ipAddress is not a valid IP address.
     */
    protected InetAddress createAddr(final String hostName, final String ipAddress) throws UnknownHostException {
        if (!InetAddresses.isInetAddress(ipAddress)) {
            throw new UnknownHostException(hostName);
        }
        return InetAddress.getByAddress(hostName, InetAddresses.forString(ipAddress).getAddress());
    }

    /**
     * Same as createAddr but uses the loopback address as the ipAddress string
     *
     * @param hostName the hostname
     * @return a new InetAddress
     */
    protected InetAddress createLoopbackAddr(final String hostName) {
        try {
            return InetAddress.getByAddress(hostName, InetAddress.getLoopbackAddress().getAddress());
        } catch (UnknownHostException e) {
            throw new AssertionError("Loopback should always be valid", e);
        }
    }

    /** A default resolver that can be used */
    private static final Resolver DEFAULT_RESOLVER = new DnsResolver();

    /** Returns the default resolver */
    public static Resolver getDefaultResolver() { return DEFAULT_RESOLVER; }

    /**
     * A resolver which uses InetAddress.getAllByName to do DNS lookups
     */
    private static class DnsResolver extends Resolver {
        @Override protected Collection<InetAddress> resolveHost(final String host) throws UnknownHostException {
            return Arrays.asList(InetAddress.getAllByName(host));
        }
    }
}
