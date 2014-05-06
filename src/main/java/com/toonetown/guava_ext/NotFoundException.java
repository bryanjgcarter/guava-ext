package com.toonetown.guava_ext;

/**
 * A checked exception that indicates when an item is not found.  For unchecked exceptions, code can throw 
 * NoSuchElementException.  This exception is for cases where null is not a valid return value, but code still needs
 * to handle the error case.
 */
public class NotFoundException extends Exception {
    /* Overridden constructors */
    public NotFoundException() { super(); }
    public NotFoundException(final String message) { super(message); }
    public NotFoundException(final String message, final Throwable cause) { super(message, cause); }
    public NotFoundException(final Throwable cause) { super(cause); }
    
}
