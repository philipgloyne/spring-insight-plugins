/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.insight.plugin.jdbc;

import org.springframework.web.util.HtmlUtils;


/**
 * Simple class to convert a string of sql into HTML.
 * 
 * The SQL will first be encoded to be properly displayed in HTML.
 * 
 * Following that, specific keywords (SELECT, INSERT, etc.) will be wrapped with
 * &lt;span&gt; tags containing a user specified class.
 * 
 * e.g.:
 *      SqlToHtml.toHtml("select *", "myClass") -&gt;
 *   yields:
 *      &lt;span class='myClass myClass-select'&gt;SELECT &lt;/span&gt;*
 */
public class SqlToHtml {
    
    private static final String[][] KEYWORDS = { { "SELECT ", "-select" },
                                                { "INSERT ", "-insert" },
                                                { "UPDATE ", "-update" },
                                                { "DELETE ", "-delete" },
                                                { "CREATE TABLE ", "-createTable" },
                                                { "CREATE INDEX ", "-createIndex" },
                                                { "FROM ", "-from" },
                                                { "WHERE ", "-where" },
                                                { "ORDER BY ", "-orderBy" },
                                                { "GROUP BY ", "-groupBy" },
                                                { "HAVING ", "-having" },
                                                { "SET ", "-set" } };

    public String toHtml(String sql, String keywordClass) {
        sql = new SQLFormatter().prettyPrint(sql);
        sql = HtmlUtils.htmlEscape(sql);
        for (int i = 0; i < KEYWORDS.length; i++) {
            String keyword = KEYWORDS[i][0];
            String selectorSuffix = KEYWORDS[i][1];

            if (sql.contains(keyword)) {
                String spanTag = makeSpanTag(keywordClass, selectorSuffix);
                String replace = new StringBuilder(spanTag).append(keyword).append("</span>").toString();
                sql = sql.replace(keyword, replace);
            }
        }
        return sql;
    }

    private String makeSpanTag(String keywordClass, String selectorSuffix) {
        return new StringBuilder()
                    .append("<span class='")
                    .append(keywordClass)
                    .append(" ")
                    .append(keywordClass)
                    .append(selectorSuffix)
                    .append("'>")
                    .toString();
    }

}
