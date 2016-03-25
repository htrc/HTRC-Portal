package edu.indiana.d2i.htrc.portal.config;

import edu.indiana.d2i.htrc.portal.PortalException;

public class ConfigurationException extends PortalException {
    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
