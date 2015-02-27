package edu.indiana.d2i.htrc.portal.exception;

/**
 * Created by shliyana on 2/5/15.
 */
public class RoleNameAlreadyExistsException extends Exception {
    public RoleNameAlreadyExistsException(String message) {
        super(message);
    }

    public RoleNameAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public RoleNameAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
