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

public class Doc {
    private String id;
    private String author;
    private String authorTop;
    private String title;
    private String titleA;
    private String titleAB;
    private String published;
    private String publisher;
    private String htrcPageCount;
    private String htrcCharCount;
    private String language;
    private String mainAuthor;
    private String publishDate;
    private String publishDateRange;
    private String wordCount;

    public String getWordCount() {
        return wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorTop() {
        return authorTop;
    }

    public void setAuthorTop(String authorTop) {
        this.authorTop = authorTop;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleA() {
        return titleA;
    }

    public void setTitleA(String titleA) {
        this.titleA = titleA;
    }

    public String getTitleAB() {
        return titleAB;
    }

    public void setTitleAB(String titleAB) {
        this.titleAB = titleAB;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getHtrcPageCount() {
        return htrcPageCount;
    }

    public void setHtrcPageCount(String htrcPageCount) {
        this.htrcPageCount = htrcPageCount;
    }

    public String getHtrcCharCount() {
        return htrcCharCount;
    }

    public void setHtrcCharCount(String htrcCharCount) {
        this.htrcCharCount = htrcCharCount;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getMainAuthor() {
        return mainAuthor;
    }

    public void setMainAuthor(String mainAuthor) {
        this.mainAuthor = mainAuthor;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getPublishDateRange() {
        return publishDateRange;
    }

    public void setPublishDateRange(String publishDateRange) {
        this.publishDateRange = publishDateRange;
    }
}
