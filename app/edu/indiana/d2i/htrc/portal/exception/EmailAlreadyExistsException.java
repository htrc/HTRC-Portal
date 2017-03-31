package edu.indiana.d2i.htrc.portal.exception;


public class EmailAlreadyExistsException extends Exception {
    public EmailAlreadyExistsException(String message) {
      super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
      super(message, cause);
    }

    public EmailAlreadyExistsException(Throwable cause) {
      super(cause);
    }
}
