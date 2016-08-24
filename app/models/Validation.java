package models;

/**
 * Created by ramarvab on 8/22/16.
 */
public class Validation {
    private String volumeID;
    private String title;

    public Validation(String volumeID,String title)
    {
        this.volumeID = volumeID;
        this.title = title;
    }

    public String getVolumeID()
    {
        return volumeID;
    }

    public String getTitle()
    {
        return title;
    }

}
