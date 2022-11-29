package com.oltpbenchmark.benchmarks.chbenchmark.queries;

import com.oltpbenchmark.api.SQLStmt;

public class Q23 extends GenericQuery {

    public final SQLStmt query_stmt = new SQLStmt(
        "select o_w_id, o_d_id, o_c_id, no_balance, c_balance, cs_credit " +
        "from credit_status, customer, " +
        "     (select o_w_id, o_d_id, o_c_id, sum(ol_amount) as no_balance " +
        "      from order_line, oorder, new_order " +
        "      where ol_o_id = o_id and no_o_id = o_id " +
        "      group by o_w_id, o_d_id, o_c_id) as no_bal " +
        "where no_bal.o_w_id = cs_w_id AND no_bal.o_d_id = cs_d_id AND no_bal.o_c_id = cs_c_id " +
        "AND   cs_w_id = c_w_id AND cs_d_id = c_d_id AND cs_c_id = c_id "
    );

    @Override
    protected SQLStmt get_query() {
        return query_stmt;
    }
    
}
