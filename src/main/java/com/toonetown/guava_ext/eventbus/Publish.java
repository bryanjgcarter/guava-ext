package com.toonetown.guava_ext.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation which is used to indicate a method that will call post for the given method type.  This is an optional 
 * annotation, and is used mainly for documentation purposes.  The annotation is retained for the source only.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Publish {
    /**
     * The events that this method publishes
     */
    Class<? extends Events.Base>[] value();
}
