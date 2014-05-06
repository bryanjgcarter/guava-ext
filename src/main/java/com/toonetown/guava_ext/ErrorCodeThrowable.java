package com.toonetown.guava_ext;

/**
 * An interface that can be used by throwables to indicate that they support "error code" functionality
 */
public interface ErrorCodeThrowable {
    String UNKNOWN_ERR_CODE = "00";

    /** Returns the type of error for this exception */
    String getErrorCode();

    /** Wraps a throwable and includes an unknown error code */
    final class Unknown extends Throwable implements ErrorCodeThrowable {
        private Unknown(Throwable delegate) { super("Unknown Error Code", delegate); }

        @Override public String getErrorCode() { return UNKNOWN_ERR_CODE; }

        /** Returns the given throwable if it is an ErrorCodeThrowable, or an Unknown ErrorCodeThrowable */
        public static ErrorCodeThrowable or(final Throwable t) {
            if (t instanceof ErrorCodeThrowable) {
                return (ErrorCodeThrowable) t;
            }
            return new Unknown(t);
        }
    }
}
