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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JobDetailsBean implements Comparable<JobDetailsBean> {

    // for XML parsing
    public static final String PARAMETERS = "parameters";
    public static final String STATUS = "status";
    public static final String RESULT = "result";
    public static final String MESSAGE = "message";
    public static final String JOBNAME = "job_name";
    public static final String USER = "user";
    public static final String ALGORITHMNAME = "algorithm";
    public static final String JOBID = "job_id";
    public static final String DATE = "date";
    public static final String ONEPARAM = "param";

    private String jobId;
    private String startingTime;
    private String endingTime;
    private String lastUpdatedDate;
    private String jobTitle;
    private String jobStatus;
    private String algorithmName;
    private String userName;
    private String message;
    private Map<String, String> jobParams;
    private Map<String, String> results;
    private List<String> linksToResults;

    public static String SAVEDORNOT = "saved";
    private String jobSavedStr = "unsaved"; // temporary use

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStartingTime() {
        return startingTime;
    }

    public void setStartingTime(String startingTime) {
        this.startingTime = startingTime;
    }

    public String getEndingTime() {
        return endingTime;
    }

    public void setEndingTime(String endingTime) {
        this.endingTime = endingTime;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getJobParams() {
        return jobParams;
    }

    public void setJobParams(Map<String, String> jobParams) {
        this.jobParams = jobParams;
    }

    public Map<String, String> getResults() {

        // return results;

        // a weird requirement to sort some of the links' name
        // directory, stdout, stderr
        Map<String, String> res = new LinkedHashMap<String, String>();
        if (results.containsKey("directory")) {
            res.put("directory", results.get("directory"));
            results.remove("directory");
        }
        if (results.containsKey("stdout")) {
            res.put("stdout", results.get("stdout"));
            results.remove("stdout");
        }
        if (results.containsKey("stderr")) {
            res.put("stderr", results.get("stderr"));
            results.remove("stderr");
        }

        for (Map.Entry<String, String> entry : results.entrySet()) {
            res.put(entry.getKey(), entry.getValue());
        }
        return res;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

    public List<String> getLinksToResults() {
        return linksToResults;
    }

    public void setLinksToResults(List<String> linksToResults) {
        this.linksToResults = linksToResults;
    }

    public static String getSAVEDORNOT() {
        return SAVEDORNOT;
    }

    public static void setSAVEDORNOT(String SAVEDORNOT) {
        JobDetailsBean.SAVEDORNOT = SAVEDORNOT;
    }

    public String getJobSavedStr() {
        return jobSavedStr;
    }

    public void setJobSavedStr(String jobSavedStr) {
        this.jobSavedStr = jobSavedStr;
    }

    @Override
    public String toString() {
        return "JobDetailBean [jobId=" + jobId + ", lastUpdatedDate="
                + lastUpdatedDate + ", jobTitle=" + jobTitle + ", jobStatus="
                + jobStatus + ", algorithmName=" + algorithmName
                + ", userName=" + userName + ", message=" + message
                + ", jobParams=" + jobParams + ", results=" + results + "]";
    }

    @Override
    public int compareTo(JobDetailsBean o) {
        return o.lastUpdatedDate.compareTo(lastUpdatedDate);
    }
}
