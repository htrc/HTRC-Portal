package controllers;


import edu.indiana.d2i.htrc.portal.HTRCAgentClient;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.bean.JobDetailsBean;
import org.pac4j.play.java.JavaController;
import org.pac4j.play.java.RequiresAuthentication;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.gotopage;
import views.html.jobdetails;
import views.html.joblist;

import java.io.IOException;
import java.util.*;

public class JobManagement extends JavaController {
    private static Logger.ALogger log = play.Logger.of("application");

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result listJobs() {
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCAgentClient agentClient = new HTRCAgentClient(session());

        Map<String, JobDetailsBean> activeJobs = agentClient.getActiveJobsDetails();
        Map<String, JobDetailsBean> completedJobs = agentClient.getCompletedJobsDetails();
        List<JobDetailsBean> activeJobsList;
        List<JobDetailsBean> completedJobsList;
        if(activeJobs != null && completedJobs != null) {
            if (activeJobs.isEmpty()) {
                activeJobsList = Collections.emptyList();
                log.info("Active jobs are empty.");
            } else {
                activeJobsList = new ArrayList<JobDetailsBean>(activeJobs.values());
            }
            if (completedJobs.isEmpty()) {
                completedJobsList = Collections.emptyList();
                log.info("Completed jobs are empty.");
            } else {
                completedJobsList = new ArrayList<JobDetailsBean>(completedJobs.values());
            }
        }else{

            log.error(PortalConstants.CANNOT_GETDATA_FROM_AGENT + " for user "
                    + userId);
            return ok(gotopage.render("Sorry!! Cannot get Job details from Job Agent right now.",null,null,userId));
        }
       return ok(joblist.render(userId, activeJobsList,completedJobsList));


    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result cancelJobs() {
        List<String> activeJobIds = new ArrayList<String>();
        Map<String, String[]> form = request().body().asFormUrlEncoded();
        Set<String> keys = form.keySet();

        for (String key : keys) {
            if (form.get(key).length > 0) {
                activeJobIds.add(form.get(key)[0]);
            }
        }
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        boolean response = agentClient.cancelJobs(activeJobIds);
        if (response) {
            log.info("Following Active Job Ids are canceled successfully :" + activeJobIds);
            return redirect(routes.JobManagement.listJobs());
        } else {
            log.error("Error occurred during Active Job cancellation process");
            return ok(gotopage.render("Error occurred during Active Job cancellation process. Please try again later.", "routes.JobManagement.listJobs()","View Job Results", userId));
        }

    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result updateJobs() throws IOException {
        List<String> completedJobIds = new ArrayList<String>();
        Map<String, String[]> form = request().body().asFormUrlEncoded();
        Set<String> keys = form.keySet();
//        String actionType = form.get("update-type")[0].trim();
        for (String key : keys) {
            if (form.get(key).length > 0 && !key.equals("update-type")) {
                completedJobIds.add(form.get(key)[0]);
            }
        }
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        boolean response = agentClient.deleteJobs(completedJobIds);
        if (response) {
            log.info("Following Completed Job Ids are deleted successfully :" + completedJobIds);
            return redirect(routes.JobManagement.listJobs());
        } else {
            log.error("Error occurred during Completed Job cancellation process");
            return ok(gotopage.render("Error occurred during Job deletion process. Please try again later.", "routes.JobManagement.listJobs()","View Job Results", userId));
        }
    }

    @RequiresAuthentication(clientName = "Saml2Client")
    public static Result viewJobDetails(String jobId) {
        String userId = session(PortalConstants.SESSION_USERNAME);
        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        List<String> jobIds = new ArrayList<String>();
        jobIds.add(jobId);
        Map<String, JobDetailsBean> jobsDetails = agentClient.getJobsDetails(jobIds);
        Map<String, String> jobResults = jobsDetails.get(jobId).getResults();
        List<Map.Entry<String,String>> outputs = new ArrayList<Map.Entry<String, String>>();

        Map<String, String> htmls = new TreeMap<String, String>();
        Map<String, String> csvs = new TreeMap<String, String>();
        Map<String, String> zips = new TreeMap<String, String>();
        Map<String, String> xmls = new TreeMap<String, String>();
        Map<String, String> txts = new TreeMap<String, String>();
        Map<String, String> scripts = new TreeMap<String, String>();
        Map<String, String> others = new TreeMap<String, String>();
        for(Map.Entry<String, String> result : jobResults.entrySet()){
            if(result.getKey().contains(".html")){
                htmls.put(result.getKey(),result.getValue());
            } else if(result.getKey().contains(".csv")){
                csvs.put(result.getKey(),result.getValue());
            } else if(result.getKey().contains(".zip")){
                zips.put(result.getKey(),result.getValue());
            } else if(result.getKey().contains(".xml")){
                xmls.put(result.getKey(),result.getValue());
            } else if(result.getKey().contains(".sh")){
                scripts.put(result.getKey(),result.getValue());
            }else if(result.getKey().contains(".txt")){
                txts.put(result.getKey(),result.getValue());
            }else {
                others.put(result.getKey(),result.getValue());
            }
        }

        for(Map.Entry<String, String> e: htmls.entrySet()){
            outputs.add(e);
        }

        for(Map.Entry<String, String> e: csvs.entrySet()){
            outputs.add(e);
        }

        for(Map.Entry<String, String> e: zips.entrySet()){
            outputs.add(e);
        }

        for(Map.Entry<String, String> e: xmls.entrySet()){
            outputs.add(e);
        }

        for(Map.Entry<String, String> e: scripts.entrySet()){
            outputs.add(e);
        }

        for(Map.Entry<String, String> e: txts.entrySet()){
            outputs.add(e);
        }

        for(Map.Entry<String, String> e: others.entrySet()){
            outputs.add(e);
        }

        return ok(jobdetails.render(userId, jobsDetails.get(jobId), outputs));
    }
}
