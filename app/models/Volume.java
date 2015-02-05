package models;

import play.Logger;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shliyana on 2/3/15.
 */
@Entity
public class Volume extends Model {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(columnDefinition = "TEXT")
    public String title;

    public String volumeId;

    @Column(columnDefinition = "TEXT")
    public String maleAuthor;

    @Column(columnDefinition = "TEXT")
    public String femaleAuthor;

    @Column(columnDefinition = "TEXT")
    public String genderUnkownAuthor;
    public String pageCount;
    public String wordCount;


    private static Logger.ALogger log = play.Logger.of("application");



    public static Finder<Long, Volume> find = new Finder<Long, Volume>(
            Long.class, Volume.class
    );

    public static Volume findByVolumeID(String volumeId) {
        return find.where().eq("volumeId", volumeId).findUnique();
    }


}
