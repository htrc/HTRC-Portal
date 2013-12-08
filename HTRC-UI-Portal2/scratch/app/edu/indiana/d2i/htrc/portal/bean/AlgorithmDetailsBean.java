/**
 * Copyright 2013 The Trustees of Indiana University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express  or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.indiana.d2i.htrc.portal.bean;

import java.util.List;

public class AlgorithmDetailsBean {
    public static final String INFO = "info";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String DESCRIPTION = "description";
    public static final String AUTHOR = "author";
    public static final String PARAMETER = "param";
    public static final String SUPPORTURL = "supportUrl";

    /**
     * This class wraps all the attributes of one parameter, including
     * type, name, label(shown on the web page), description and whether
     * it is required or not.
     */
    public static class Parameter {
        private boolean required;
        private String type;
        private String name;
        private String label;
        private String description;
        private String defaultValue;
        private String validation;
        private String validationError;
        private boolean readOnly;

        public boolean isRequired() {
            return required;
        }
        public void setRequired(boolean required) {
            this.required = required;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getLabel() {
            return label;
        }
        public void setLabel(String label) {
            this.label = label;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getValidation() {
            return validation;
        }

        public void setValidation(String validation) {
            this.validation = validation;
        }

        public String getValidationError() {
            return validationError;
        }

        public void setValidationError(String validationError) {
            this.validationError = validationError;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }

        @Override
        public String toString() {
            return "Parameter [required=" + required + ", type=" + type
                    + ", name=" + name + ", label=" + label + ", description="
                    + description + "]";
        }
    }

    private List<Parameter> parameters;
    private String name = null;
    private List<String> authors;
    private String version;
    private String description;
    private String supportUrl;

    public List<Parameter> getParameters() {
        return parameters;
    }
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<String> getAuthors() {
        return authors;
    }
    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getSupportUrl() {
        return supportUrl;
    }
    public void setSupportUrl(String supportUrl) {
        this.supportUrl = supportUrl;
    }
    @Override
    public String toString() {
        return "AlgorithmDetailBean [parameters=" + parameters + ", name="
                + name + ", authors=" + authors + ", version=" + version
                + ", description=" + description + ", supportUrl=" + supportUrl
                + "]";
    }
}
