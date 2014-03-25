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

package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Entity
public class CompletedJob extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String jobId;
    public String jobName;
    public String lastModified;
    public String status;
    public String jobSavedStr;

    public CompletedJob(String jobId, String jobName, String lastModified, String status, String jobSavedStr){
        this.jobId = jobId;
        this.jobName = jobName;
        this.lastModified = lastModified;
        this.status = status;
        this.jobSavedStr = jobSavedStr;
    }

    public static Finder<Long, CompletedJob> find = new Finder<Long, CompletedJob>(Long.class, CompletedJob.class);

    public static CompletedJob findByJobID(String jobId){
        return find.where().eq("jobId", jobId).findUnique();
    }

    public static void delete(CompletedJob job){
        job.delete();
    }

    public static List<CompletedJob> all(){
        return find.all();
    }
}
