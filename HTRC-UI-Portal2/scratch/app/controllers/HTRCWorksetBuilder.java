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

package controllers;

import models.Doc;
import models.SearchOption;
import models.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.data.Form;
import play.libs.F.Function;
import play.libs.WS;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTRCWorksetBuilder extends Controller {
    private static Logger.ALogger log = play.Logger.of("application");

    private static final String solrMeta = "http://chinkapin.pti.indiana.edu:9994/solr/meta/select?q=";
    private static final String solrOcr = "http://chinkapin.pti.indiana.edu:9994/solr/ocr/select?q=";

    private static final Map<String, String> searchOptions;
    static {
        searchOptions = new HashMap<String, String>();
        searchOptions.put("fulltext", "Full Text");
        searchOptions.put("title", "Title");
        searchOptions.put("author", "Author");
        searchOptions.put("subject", "Subject");
    }

    @Security.Authenticated(Secured.class)
    public static Result searchCorpus(){
        final User loggedInUser = User.findByUserID(request().username());
        final List<Doc> searchResults = new ArrayList<Doc>();
        final Form<SearchCorpus> searchCorpusForm = Form.form(SearchCorpus.class).bindFromRequest();
        if(searchCorpusForm.hasErrors()){
            return badRequest(search.render(searchResults,loggedInUser,searchCorpusForm));
        }
        String context = searchCorpusForm.get().context;
        final String searchString = searchCorpusForm.get().searchString;
        final SearchOption currentOption = new SearchOption(context, searchOptions.get(context));
        final List<SearchOption> otherSearchOptions = new ArrayList<SearchOption>() {
        };

        for(Map.Entry<String, String> entry : searchOptions.entrySet()){
            if (!entry.getKey().equals(context)){
                otherSearchOptions.add(new SearchOption(entry.getKey(), entry.getValue()));
            }
        }

        String solrURL = null;
        String query = "*:*";

        System.out.println(searchString);
        System.out.println(context);

        if(context.equals("fulltext")){
            solrURL = solrOcr;
            query ="ocr:" + searchString;
        } else if(context.equals("subject")){
            solrURL = solrMeta;
            query = "topic:" + searchString;
        } else {
            solrURL = solrMeta;
            query = context + ":" + searchString;
        }

        System.out.println(solrURL);

        return async( WS.url(solrURL).setQueryParameter("q", query).get().map(
                new Function<WS.Response, Result>() {
                    public Result apply(WS.Response response) {
                        System.out.println("URI: " + response.getUri().toString());
                        System.out.println("Response received.");
                        System.out.println(new String(response.asByteArray()));
                        Document solrResponse = response.asXml();
                        NodeList results = solrResponse.getElementsByTagName("result");
                        Node result = results.item(0);
                        NodeList docs = result.getChildNodes();


                        for(int i = 0; i < 20; i++){
                            if(i < docs.getLength()){
                                Element doc = (Element)docs.item(i);
                                Doc d = new Doc();
                                NodeList arrs = doc.getElementsByTagName("arr");

                                for(int j = 0; j < arrs.getLength(); j++){
                                    Element arr = (Element)arrs.item(j);
                                    if(arr.getAttribute("name").equals("title")){
                                        d.setTitle(arr.getFirstChild().getTextContent());
                                    } else if(arr.getAttribute("name").equals("author")){
                                        d.setAuthor(arr.getFirstChild().getTextContent());
                                    }
                                }

                                NodeList strs = doc.getElementsByTagName("str");

                                for(int j = 0; j < strs.getLength(); j++){
                                    Element str = (Element)strs.item(j);
                                    if(str.getAttribute("name").equals("id")){
                                        d.setId(str.getTextContent());
                                    }
                                }

                                NodeList ints = doc.getElementsByTagName("int");

                                for(int j = 0; j < ints.getLength(); j++){
                                    Element el = (Element)ints.item(j);
                                    if(el.getAttribute("name").equals("htrc_pageCount")){
                                        d.setHtrcPageCount(el.getTextContent());
                                    }
                                }

                                NodeList longs = doc.getElementsByTagName("long");

                                for(int j = 0; j < longs.getLength(); j++){
                                    Element el = (Element)longs.item(j);
                                    if(el.getAttribute("name").equals("htrc_wordCount")){
                                        d.setWordCount(el.getTextContent());
                                    }
                                }


                                searchResults.add(d);
                            } else {
                                break;
                            }
                        }


                        //return ok("Feed title:" + response.asJson().findPath("title"));
                        return ok(search.render(searchResults, loggedInUser,searchCorpusForm));

                    }
                }
        ));
    }

    @Security.Authenticated(Secured.class)
    public static Result createWorkset(){
        User loggedInUser = User.findByUserID(request().username());
        List<Doc> searchResults = new ArrayList<Doc>();
        return ok(search.render(searchResults,loggedInUser,Form.form(SearchCorpus.class)));
    }

    public static class SearchCorpus{
        public String context;
        public String searchString;


        public String validate(){
            if(context.isEmpty() || searchString.isEmpty()){
                return "Empty Search Parameters";

            } else{
                return null;
            }
        }


    }

}

