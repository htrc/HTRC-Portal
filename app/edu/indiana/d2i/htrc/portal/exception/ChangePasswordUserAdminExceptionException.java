package edu.indiana.d2i.htrc.portal.exception;


public class ChangePasswordUserAdminExceptionException extends Exception {
    public ChangePasswordUserAdminExceptionException() {
        super();
    }

    public ChangePasswordUserAdminExceptionException(String message) {
        super(message);
    }

    public ChangePasswordUserAdminExceptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChangePasswordUserAdminExceptionException(Throwable cause) {
        super(cause);
    }

    protected ChangePasswordUserAdminExceptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
