/**
 * Copyright 2016 The Trustees of Indiana University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.indiana.d2i.htrc.portal.config;

import com.csvreader.CsvReader;
import com.google.common.io.Files;
import com.typesafe.config.Config;
import play.Configuration;
import buildinfo.BuildInfo;
import play.Logger;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class PortalConfiguration extends Configuration {
    private static Logger.ALogger log = play.Logger.of("config");

    private static final String CONF_URL = "portal.url";
    private String version;

    public PortalConfiguration(Config conf) {
        super(conf);
        log.info(conf.toString());
    }

    public Configuration getIDPConfig(String idpPrefix) {
        return getConfig(idpPrefix);
    }

    public String getURL() {
        return getString(CONF_URL, "https://localhost:9000");
    }

    public String getVersion() {
        if (version == null) {
            version = "v" + BuildInfo.gitVersion();
            if (BuildInfo.gitDirty()) {
                version += "*";
            }
        }

        return version;
    }

    public EmailValidationConfiguration getEmailValidationConfiguration() {
        return new EmailValidationConfiguration(getConfig("emailvalidator").underlying());
    }

    /**
     * TODO: This class should wrap a RESTful service which validates emails. Should be renamed to EmailValidator?
     */
    public static class EmailValidationConfiguration extends Configuration {
        private static final String CONF_VALID_EMAIL_DOMAINS = "valid.email.domains";
        private static final String CONF_APPROVED_EMAIL_DOMAINS = "approved.email.domains";
        private static final String CONF_APPROVED_EMAILS = "approved.emails";
        private Set<String> validDomains = new HashSet<>();

        public EmailValidationConfiguration(Config conf) {
            super(conf);
            validDomains.addAll(readDomainsFromCSV(Paths.get(getString(CONF_VALID_EMAIL_DOMAINS))));
        }

        public Set<String> getValidDomainsList() {
            // Read from recently approved email domains file.
            validDomains.addAll(readDomainsFromCSV(Paths.get(getString(CONF_APPROVED_EMAIL_DOMAINS))));

            return validDomains;
        }

        public Set<String> getApprovedEmails() {
            return readEmailAddressesFromCSV(Paths.get(getString(CONF_APPROVED_EMAILS)));
        }

        private static Set<String> readDomainsFromCSV(Path csv) {
            CsvReader reader = null;
            try {
                reader = new CsvReader(Files.newReader(csv.toFile(), Charset.defaultCharset()));
                Set<String> validEmailDomains = new HashSet<>();

                reader.readHeaders();
                while (reader.readRecord()) {
                    String institutionWebAddress = reader.get("Institution_Web_Address");
                    String institutionDomain = institutionWebAddress.replaceFirst("www.", "");
                    if (institutionDomain.contains("/")) {
                        institutionDomain = institutionDomain.substring(0, institutionDomain.indexOf("/"));
                    }

                    validEmailDomains.add(institutionDomain);
                }

                return validEmailDomains;
            } catch (Exception e) {
                throw new ConfigurationException("Couldn't read valid email domains file: " + csv.toString(), e);
            } finally {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }

        private static Set<String> readEmailAddressesFromCSV(Path csv) {
            CsvReader reader = null;
            try {
                reader = new CsvReader(Files.newReader(csv.toFile(), Charset.defaultCharset()));
                Set<String> approvedEmails = new HashSet<>();

                reader.readHeaders();
                while (reader.readRecord()) {
                    approvedEmails.add(reader.get("Approved_Emails"));
                }

                return approvedEmails;
            } catch (Exception e) {
                throw new ConfigurationException("Couldn't read valid email domains file: " + csv.toString(), e);
            } finally {
                try {
                    reader.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
