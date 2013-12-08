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

public class JobSubmitBean {
    /**
     * This class wraps parameter name, parameter type and its value.
     */
    public static class Parameter {
        private String paramName;
        private String paramType;
        private String paramValue;

        public String getParamName() {
            return paramName;
        }
        public void setParamName(String paramName) {
            this.paramName = paramName;
        }
        public String getParamType() {
            return paramType;
        }
        public void setParamType(String paramType) {
            this.paramType = paramType;
        }
        public String getParamValue() {
            return paramValue;
        }
        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        @Override
        public String toString() {
            return "Parameter [paramName=" + paramName + ", paramType="
                    + paramType + ", paramValue=" + paramValue + "]";
        }
    }

    private String jobName;
    private String userName;
    private String algorithmName;
    private List<Parameter> parameters;

    private final String jobTemplate = "<job><name>%s</name>" +
            "<username>%s</username><algorithm>%s</algorithm>" +
            "%s</job>";
    private final String oneParamTemplate = "<param name=\"%s\" type=\"%s\" value=\"%s\"/>";
    private final String paramsTemplate = "<parameters>%s</parameters>";

    public String getJobName() {
        return jobName;
    }
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getAlgorithmName() {
        return algorithmName;
    }
    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }
    public List<Parameter> getParameters() {
        return parameters;
    }
    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * It returns the job submission info
     * @return an XML string
     */
    public String toXML() {
        StringBuilder params = new StringBuilder();
        if (parameters != null) {
            for (Parameter elem : parameters) {
                String paramValue = elem.getParamValue();
                if (paramValue.equals("")) continue;
                params.append(String.format(oneParamTemplate,
                        elem.getParamName(), elem.getParamType(), paramValue));
            }
        }

        String allparams = String.format(paramsTemplate, params.toString());
        String res = String.format(jobTemplate, getJobName(), getUserName(),
                getAlgorithmName(), allparams);
        return res;
    }

    @Override
    public String toString() {
        return "JobSubmitBean [jobName=" + jobName + ", userName=" + userName
                + ", algorithmName=" + algorithmName + ", parameters="
                + parameters + "]";
    }
}
