/*
 * Copyright 2020 by OLTPBenchmark Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.oltpbenchmark.benchmarks.chbenchmark.queries;

import com.oltpbenchmark.api.Procedure;
import com.oltpbenchmark.api.SQLStmt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class GenericQuery extends Procedure {
    private static final Logger LOG = LoggerFactory.getLogger(GenericQuery.class);

    protected abstract SQLStmt get_query();

    public void run(Connection conn) throws SQLException {
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn.setReadOnly(true);
            pstmt = this.getPreparedStatement(conn, get_query());

            stmt = conn.createStatement();

            // hikida add start //
            if (conn.isReadOnly())
                LOG.debug("[rss] {} read only flag set succeeded.", getProcedureName());
            else
                LOG.debug("[rss] {} read only flag set failed.", getProcedureName());
            // hikida add end //

            rs = pstmt.executeQuery();
            // hikida add start
            if (LOG.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                LOG.debug("[rss] {} Statement: {}", getProcedureName(), pstmt);
                builder.append(String.format("[rss] %s Result: ", getProcedureName()));
                while (rs.next()) {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int cols = rsmd.getColumnCount();
                    for (int i=1; i<=cols; i++) {
                        builder.append(String.format("%s=%s ", rsmd.getColumnLabel(i), rs.getString(i)));
                    }
                    builder.append(", ");
                }
                LOG.debug(builder.toString());
            }
            // hikida add end
    } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (pstmt != null) pstmt.close();
        }
    }
}
