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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.springsource.insight.collection.OperationCollectionAspectSupport;
import com.springsource.insight.collection.OperationCollectionAspectTestSupport;
import com.springsource.insight.intercept.operation.Operation;

@ContextConfiguration("classpath:jdbc-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JdbcStatementOperationCollectionAspectTest
    extends OperationCollectionAspectTestSupport {
    @Autowired
    DataSource dataSource;

    @Test
    public void operationCollection() throws SQLException {
        Connection c = dataSource.getConnection();
        Statement ps = c.createStatement();

        String sql = "select * from appointment where owner = 'Agim' and dateTime = '2009-06-01'";
        ps.execute(sql);

        Operation   operation=getLastEntered();
        assertEquals(sql, operation.get("sql"));
        assertNull(operation.get("params"));
    }

    @Override
    public OperationCollectionAspectSupport getAspect() {
        return JdbcStatementOperationCollectionAspect.aspectOf();
    }
}
