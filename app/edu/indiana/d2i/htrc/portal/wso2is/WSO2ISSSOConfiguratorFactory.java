package edu.indiana.d2i.htrc.portal.wso2is;

import edu.indiana.d2i.htrc.portal.api.SSOConfigurator;
import edu.indiana.d2i.htrc.portal.api.SSOConfiguratorFactory;
import edu.indiana.d2i.htrc.portal.config.PortalConfiguration;

public class WSO2ISSSOConfiguratorFactory implements SSOConfiguratorFactory {
    @Override
    public SSOConfigurator getSSOConfigurator(PortalConfiguration configuration) {
        return new WSO2ISSSOConfigurator(new WSO2ISConfiguration(configuration.getIDPConfig("wso2").underlying()));
    }
}
