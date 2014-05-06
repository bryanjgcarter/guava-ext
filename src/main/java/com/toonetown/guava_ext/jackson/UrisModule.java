package com.toonetown.guava_ext.jackson;

import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.PackageVersion;

import com.toonetown.guava_ext.Uris;

/**
 * A Jackson module that will handle deserialization of URIs based on the Uris class
 */
public class UrisModule extends SimpleModule {
    private static final long serialVersionUID = 1L;

    public UrisModule(final boolean strict) {
        super(PackageVersion.VERSION);

        addDeserializer(URI.class, new UrisDeserializer(strict));
    }

    private static class UrisDeserializer extends FromStringDeserializer<URI> {
        private final boolean strict;
        public UrisDeserializer(final boolean strict) {
            super(URI.class);
            this.strict = strict;
        }

        @Override
        protected URI _deserialize(final String value,
                                   final DeserializationContext ctxt) throws IllegalArgumentException {
            try {
                return Uris.newUri(value, strict);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid URI: " + value, e);
            }
        }
    }
}
