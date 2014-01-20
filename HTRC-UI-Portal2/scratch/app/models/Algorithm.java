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
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Entity
public class Algorithm extends Model {
    public static Integer ROWS_PER_PAGE = PlayConfWrapper.algorithmsPerPage();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String name;

    public String description;

    public String authors;

    public String version;

    private static Logger.ALogger log = play.Logger.of("application");

    public Algorithm(String name, String description, String authors, String version){
        this.name = name;
        this.description = description;
        this.authors = authors;
        this.version = version;
    }

    public static Finder<Long, Algorithm> finder = new Finder<Long, Algorithm>(Long.class, Algorithm.class);

    public static List<Algorithm> all(){
        return finder.all();
    }

    public static PagingList<Algorithm> algorithmPagingList(){
        return finder.where().findPagingList(ROWS_PER_PAGE);
    }

    public static void create(Algorithm algorithm){
        algorithm.save();
    }

    public static void delete(Algorithm algorithm){
        algorithm.delete();
    }

    public static Algorithm findAlgoritm(String algorithmName){
        return finder.where().eq("name", algorithmName).findUnique();
    }





}
