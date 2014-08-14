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

public class VolumeDetailsBean {


    private String volumeId;
    private String title;
    private String maleAuthor;
    private String femaleAuthor;
    private String genderUnkownAuthor;
    private String pageCount;
    private String wordCount;

    public static int VOLUMES_PER_PAGE = 10;

    public List<VolumePropertiesBean> getProperties() {
        return properties;
    }

    public void setProperties(List<VolumePropertiesBean> properties) {
        this.properties = properties;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMaleAuthor() {
        return maleAuthor;
    }

    public void setMaleAuthor(String maleAuthor) {
        this.maleAuthor = maleAuthor;
    }

    public String getFemaleAuthor() {
        return femaleAuthor;
    }

    public void setFemaleAuthor(String femaleAuthor) {
        this.femaleAuthor = femaleAuthor;
    }

    public String getGenderUnkownAuthor() {
        return genderUnkownAuthor;
    }

    public void setGenderUnkownAuthor(String genderUnkownAuthor) {
        this.genderUnkownAuthor = genderUnkownAuthor;
    }

    public String getPageCount() {
        return pageCount;
    }

    public void setPageCount(String pageCount) {
        this.pageCount = pageCount;
    }

    public String getWordCount() {
        return wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }

    private List<VolumePropertiesBean> properties;

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }


//    public List<VolumeProperty> properties;

}
