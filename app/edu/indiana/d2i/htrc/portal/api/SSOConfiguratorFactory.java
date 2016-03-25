package edu.indiana.d2i.htrc.portal.api;

import edu.indiana.d2i.htrc.portal.config.PortalConfiguration;

public interface SSOConfiguratorFactory {
    SSOConfigurator getSSOConfigurator(PortalConfiguration configuration);
}
