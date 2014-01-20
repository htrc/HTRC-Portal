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

import com.avaje.ebean.PagingList;
import edu.indiana.d2i.htrc.portal.PlayConfWrapper;
import play.Logger;
import play.Play;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Workset extends Model {
    public static Integer ROWS_PER_PAGE = PlayConfWrapper.worksetsPerPage();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public String description;

    public String author;

    public String lastModifiedBy;

    public String lastModified;

    public int numberOfVolumes;

    // Workset is public or private
    public boolean shared;

    private static Logger.ALogger log = play.Logger.of("application");

    public Workset(String name, String description, String author, String lastModifiedBy,
                   String lastModified, int numberOfVolumes, boolean shared){
        this.name = name;
        this.description = description;
        this.author = author;
        this.lastModifiedBy = lastModifiedBy;
        this.lastModified = lastModified;
        this.numberOfVolumes = numberOfVolumes;
        this.shared = shared;
    }
    public static Finder<Long, Workset> finder = new Finder<Long, Workset>(Long.class, Workset.class);



    public static List<Workset> all(){
        return finder.all();
    }

    public static void create(Workset workset){
        workset.save();
    }

    public static void delete(Workset workset){
        workset.delete();
    }

    public static Workset findWorkset(String worksetName){
        return finder.where().eq("name", worksetName).findUnique();
    }


    public static List<Workset> listAllShared(){
        return finder.where().eq("shared", true).findList();
    }

    public static List<Workset> listAllOwned(String user){
        return finder.where().eq("author", user).findList();
    }

    public static List<Workset> listShared(int page){
        List<Workset> allSharedWorksets = listAllShared();
        List<Workset> sharedWorkset = new ArrayList<Workset>();
        int volumesPerPage = Play.application().configuration().getInt("worksets.paer.page");
        int startVol = volumesPerPage * page;
        int endVol = Math.min(startVol+volumesPerPage-1,allSharedWorksets.size()-1);

        for (int i = startVol; i <= endVol; i++) {
            for(Workset w: allSharedWorksets){
                sharedWorkset.add(w);
            }
        }
        return sharedWorkset;
    }

    public static PagingList<Workset> owned(User user){
        return finder.where().eq("author", user.userId).findPagingList(ROWS_PER_PAGE);
    }

    public static PagingList<Workset> shared(){
        return finder.where().eq("shared", true).findPagingList(ROWS_PER_PAGE);
    }

    public static List<Workset> listOwned(int page, User user){
        List<Workset> allOwnedWorksets = listAllOwned(user.userId);
        List<Workset> ownedWorkset = new ArrayList<Workset>();
        int volumesPerPage = Play.application().configuration().getInt("worksets.paer.page");
        int startVol = volumesPerPage * page;
        int endVol = Math.min(startVol+volumesPerPage-1,allOwnedWorksets.size()-1);

        for (int i = startVol; i <= endVol; i++) {
            for(Workset w: allOwnedWorksets){
                ownedWorkset.add(w);
            }
        }
        return ownedWorkset;
    }

}
