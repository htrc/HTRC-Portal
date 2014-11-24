package controllers;


import edu.indiana.d2i.htrc.portal.HTRCAgentClient;
import edu.indiana.d2i.htrc.portal.PortalConstants;
import edu.indiana.d2i.htrc.portal.bean.JobDetailsBean;
import models.ActiveJob;
import models.CompletedJob;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.gotopage;
import views.html.jobdetails;
import views.html.joblist;

import java.io.IOException;
import java.util.*;

public class JobManagement extends Controller {
    private static Logger.ALogger log = play.Logger.of("application");

    @Security.Authenticated(Secured.class)
    public static Result listJobs() {
        User loggedInUser = User.findByUserID(request().username());
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
                    + loggedInUser.userId);
            return ok(gotopage.render("Sorry!! Cannot get Job details from Job Agent right now.",null,null,loggedInUser));
        }
       return ok(joblist.render(loggedInUser, activeJobsList,completedJobsList));


    }

    @Security.Authenticated(Secured.class)
    public static Result cancelJobs() {
        List<String> activeJobIds = new ArrayList<String>();
        Map<String, String[]> form = request().body().asFormUrlEncoded();
        Set<String> keys = form.keySet();

        for (String key : keys) {
            if (form.get(key).length > 0) {
                activeJobIds.add(form.get(key)[0]);
            }
        }
        User loggedInUser = User.findByUserID(request().username());
        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        boolean response = agentClient.cancelJobs(activeJobIds);
        if (response) {
//            for (String jobId : activeJobIds) {
//                ActiveJob.delete(ActiveJob.findByJobID(jobId));
//            }
            log.info("Following ActiveJob Ids are canceled successfully :" + activeJobIds);
            return redirect(routes.JobManagement.listJobs());
        } else {
            log.error("Error occurred during ActiveJob cancellation process");
            return ok(gotopage.render("Error occurred during ActiveJob cancellation process. Please try again later.", "routes.JobManagement.listJobs()","View Job Results", loggedInUser));
        }

    }

    @Security.Authenticated(Secured.class)
    public static Result updateJobs() throws IOException {
        List<String> completedJobIds = new ArrayList<>();
        Map<String, String[]> form = request().body().asFormUrlEncoded();
        Set<String> keys = form.keySet();
//        String actionType = form.get("update-type")[0].trim();
        for (String key : keys) {
            if (form.get(key).length > 0 && !key.equals("update-type")) {
                completedJobIds.add(form.get(key)[0]);
            }
        }
        User loggedInUser = User.findByUserID(request().username());
        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        boolean response = agentClient.deleteJobs(completedJobIds);
        if (response) {
//            for (String jobId : activeJobIds) {
//                ActiveJob.delete(ActiveJob.findByJobID(jobId));
//            }
            log.info("Following Completed Job Ids are deleted successfully :" + completedJobIds);
            return redirect(routes.JobManagement.listJobs());
        } else {
            log.error("Error occurred during Completed Job cancellation process");
            return ok(gotopage.render("Error occurred during Job deletion process. Please try again later.", "routes.JobManagement.listJobs()","View Job Results", loggedInUser));
        }
//        if (actionType.equals("delete")) {
//            log.info("Deleting jobs: " + completedJobIds);
//            response = agentClient.deleteJobs(completedJobIds);
//            if (response) {
//                for (String jobId : completedJobIds) {
//                    CompletedJob.delete(CompletedJob.findByJobID(jobId));
//                }
//                log.info("Following Completed Job Ids are deleted successfully :" + completedJobIds);
//            } else {
//                log.error("Error occured during Completed Job deletion process");
//            }
//        }
//        if (actionType.equals("save")) {
//            log.info("Saving jobs: " + completedJobIds);
//            response = agentClient.saveJobs(completedJobIds);
//            if (response) {
//                log.info("Following Completed Job Ids are saved successfully :" + completedJobIds);
//            } else {
//                log.error("Error occured during Completed Job saving process");
//            }
//        }


    }

    @Security.Authenticated(Secured.class)
    public static Result viewJobDetails(String jobId) {
        User loggedInUser = User.findByUserID(request().username());
        HTRCAgentClient agentClient = new HTRCAgentClient(session());
        List<String> jobIds = new ArrayList<>();
        jobIds.add(jobId);
        Map<String, JobDetailsBean> jobsDetails = agentClient.getJobsDetails(jobIds);
        return ok(jobdetails.render(loggedInUser, jobsDetails.values()));
    }


//    public static void updateJobList(User loggedInUser) {
//        HTRCAgentClient agentClient = new HTRCAgentClient(session());
//        Map<String, JobDetailsBean> activeJobs = agentClient.getActiveJobsDetails();
//        Map<String, JobDetailsBean> completedJobs = agentClient.getCompletedJobsDetails();
//        if (completedJobs == null) {
//            log.error(PortalConstants.CANNOT_GETDATA_FROM_AGENT + " for user "
//                    + loggedInUser.userId);
//        } else {
//            List<JobDetailsBean> completedJobsList = new ArrayList<>(completedJobs.values());
//            for (JobDetailsBean job : completedJobsList) {
//                if (ActiveJob.findByJobID(job.getJobId()) != null) {
//                    ActiveJob.delete(ActiveJob.findByJobID(job.getJobId()));
//                } else if (CompletedJob.findByJobID(job.getJobId()) == null) {
//                    CompletedJob completedJob = new CompletedJob(job.getJobId(), job.getJobTitle(), job.getLastUpdatedDate(), job.getJobStatus(), job.getJobSavedStr());
//                    completedJob.save();
//                }
//            }
//        }
//        if (activeJobs == null) {
//            log.error(PortalConstants.CANNOT_GETDATA_FROM_AGENT + " for user "
//                    + loggedInUser.userId);
//        } else {
//            List<JobDetailsBean> activeJobsList = new ArrayList<>(activeJobs.values());
//            for (JobDetailsBean job : activeJobsList) {
//                if (ActiveJob.findByJobID(job.getJobId()) != null) {
//                    ActiveJob.delete(ActiveJob.findByJobID(job.getJobId()));
//                }
//                ActiveJob activeJob = new ActiveJob(job.getJobId(), job.getJobTitle(), job.getLastUpdatedDate(), job.getJobStatus());
//                activeJob.save();
//            }
//        }
//
//    }

}
