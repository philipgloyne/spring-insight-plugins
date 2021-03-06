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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class StoredProcedureExample {
    public static int testSp(Connection conn, String owner) throws SQLException {
        Statement stmt = conn.createStatement(); 
        ResultSet rs = stmt.executeQuery("select count(*) from appointment where owner = '" + owner + "'");
        try {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException ex) {
        } finally {
            rs.close();
            stmt.close();
        }
        
        return -1;
    }
}
