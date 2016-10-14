/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package saintybalboa.nutch.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

// Commons Logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Nutch imports
import org.apache.nutch.net.URLNormalizer;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

/**
 * Converts URLs to a normal form:
 * <ul>
 * <li>remove dot segments in path: <code>/./</code> or <code>/../</code></li>
 * <li>remove default ports, e.g. 80 for protocol <code>http://</code></li>
 * </ul>
 */
public class AdvancedURLNormalizer extends Configured implements URLNormalizer {
  public static final Logger LOG = LoggerFactory
      .getLogger(AdvancedURLNormalizer.class);

  /**
   * Pattern to detect whether a URL path could be normalized. Contains one of
   * /. or ./ /.. or ../ //
   */
  private final static Pattern hasNormalizablePathPattern = Pattern
      .compile("/[./]|[.]/");

  public String normalize(String urlString, String scope)
      throws MalformedURLException {
    if ("".equals(urlString)) // permit empty
      return urlString;

    urlString = urlString.trim(); // remove extra spaces
    String ourlString = urlString;
    URL url = new URL(urlString);

    String protocol = url.getProtocol();
    String host = url.getHost().toLowerCase();
    int port = url.getPort();
    String path = url.getPath().toLowerCase();
    String queryStr = url.getQuery();    

    boolean changed = false;

    if (!urlString.startsWith(protocol)) // protocol was lowercased
      changed = true;

    if ("http".equals(protocol) || "https".equals(protocol)
        || "ftp".equals(protocol)) {

      if (host != null) {
        String newHost = host.toLowerCase(); // lowercase host
        if (!host.equals(newHost)) {
          host = newHost;
          changed = true;
        }
      }

      if (port == url.getDefaultPort()) { // uses default port
        port = -1; // so don't specify it
        changed = true;
      }

      if (url.getRef() != null) { // remove the ref
        changed = true;
      }

  
      if(queryStr != null){
      	if(!queryStr.isEmpty() && queryStr != "?"){
      
  	    	changed = true;
  		
  		    	//convert query param names values to lowercase. Dependent on arguments
  		    	queryStr = formatQueryString(queryStr);
      	}
      }

    }

    urlString = (queryStr != null && queryStr != "") ? new URL(protocol, host, port, path).toString() + "?" + queryStr : new URL(protocol, host, port, path).toString();
    
    //the url should be the same as the url passed in
    if(ourlString.length() > urlString.length()) urlString = urlString + ourlString.substring(urlString.length(), ourlString.length()); 	
    
    return urlString;
  }

  
  /* public static String formatQueryString(String querystr){  throws UnsupportedEncodingException { */
	  
  public static String formatQueryString(String querystr){
	  String nquerystr = "";
	  
	 /* try{ */
		  final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		  final String[] pairs = querystr.split("&");
		  //URLDecoder.decode(querystr, "UTF-8");
		  
		  for (String pair : pairs) {
		    final int idx = pair.indexOf("=");
		    // final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
		    final String key = idx > 0 ? pair.substring(0, idx) : pair;
		   
		    if(key != null && !key.isEmpty()){ 
			    boolean keyexists = true;
			    if (!query_pairs.containsKey(key)) {
			      keyexists = false;
			      query_pairs.put(key, new LinkedList<String>());
			    }
			    	    
			    // String value = (idx > 0 && pair.length() > idx + 1) ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			    String value = (idx > 0 && pair.length() > idx + 1) ? pair.substring(idx + 1) : null;
			    if(value == null) value = "";
			    
			    query_pairs.get(key).add(value);
			    
			    //prevent duplication of param=value
			    if(!keyexists){    		    	
				    nquerystr = (nquerystr != null && nquerystr != "") ? nquerystr + "&" : nquerystr;
				    nquerystr = nquerystr + key.toLowerCase();
				    nquerystr = nquerystr + "=" + value;
			    }		   
		    }
		  }
		  
		  String queryString = (nquerystr.isEmpty()) ? querystr : nquerystr;
		    
	      List<String> queryStringParts = Arrays.asList(queryString.split("&"));
	      // sorts the query string
	      Collections.sort(queryStringParts);
	    
	      return StringUtils.join(queryStringParts, "&");
	      /*
	  }
	  catch(UnsupportedEncodingException e){
		  return querystr;
	  } */
	  
  }

  public static void main(String args[]) throws IOException {
    AdvancedURLNormalizer normalizer = new AdvancedURLNormalizer();
    normalizer.setConf(NutchConfiguration.create());
    String scope = URLNormalizers.SCOPE_DEFAULT;
    if (args.length >= 1) {
      scope = args[0];
      System.out.println("Scope: " + scope);
    }
    String line, normUrl;
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    while ((line = in.readLine()) != null) {
      try {
        normUrl = normalizer.normalize(line, scope);
        System.out.println(normUrl);
      } catch (MalformedURLException e) {
        System.out.println("failed: " + line);
      }
    }
    System.exit(0);
  }

}