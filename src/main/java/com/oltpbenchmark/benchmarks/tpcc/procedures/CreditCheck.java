package com.oltpbenchmark.benchmarks.tpcc.procedures;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.oltpbenchmark.DBWorkload;
import com.oltpbenchmark.api.SQLStmt;
import com.oltpbenchmark.benchmarks.tpcc.TPCCConstants;
import com.oltpbenchmark.benchmarks.tpcc.TPCCUtil;
import com.oltpbenchmark.benchmarks.tpcc.TPCCWorker;

public class CreditCheck extends TPCCProcedure {

    private static final Logger LOG = LoggerFactory.getLogger(CreditCheck.class);

    public SQLStmt csGetCustBalSQL = new SQLStmt(
        "SELECT c_balance, c_credit_lim" +
        " FROM " + TPCCConstants.TABLENAME_CUSTOMER +
        " WHERE c_w_id = ?" +
        " AND c_d_id = ?" +
        " AND c_id = ?"
    );

    public SQLStmt csGetNewOrderBalSQL = new SQLStmt(
        "SELECT SUM(ol_amount)" +
        " FROM " + TPCCConstants.TABLENAME_ORDERLINE + ", " + TPCCConstants.TABLENAME_OPENORDER + ", " + TPCCConstants.TABLENAME_NEWORDER +
        " WHERE ol_w_id = ? AND ol_d_id = ? AND ol_o_id = o_id" +
        " AND o_w_id = ? AND o_d_id = ? AND o_c_id = ?" +
        " AND no_w_id = ? AND no_d_id = ? AND no_o_id = o_id"
    );

    public SQLStmt csUpdateCustCreditSQL = new SQLStmt(
        "UPDATE " + TPCCConstants.TABLENAME_CREDITSTATUS +
        " SET cs_credit = ?" +
        " WHERE cs_w_id = ? AND cs_d_id = ? AND cs_c_id = ?"
    );


    @Override
    public void run(Connection conn, Random gen, int terminalWarehouseID, int numWarehouses,
        int terminalDistrictLowerID, int terminalDistrictUpperID, TPCCWorker w) throws SQLException {

        try (PreparedStatement csGetCustBal = this.getPreparedStatement(conn, csGetCustBalSQL);
            PreparedStatement csGetNewOrderBal = this.getPreparedStatement(conn, csGetNewOrderBalSQL);
            PreparedStatement csUpdateCustCredit = this.getPreparedStatement(conn, csUpdateCustCreditSQL)) {
            
            ResultSet xidRs = conn.createStatement().executeQuery("SELECT txid_current()");xidRs.next();
            int xid = xidRs.getInt(1);
            String xactType = this.getProcedureName();

            long startTs, endTs;
            startTs = System.nanoTime() - DBWorkload.benchmarkStart;

            int districtID = TPCCUtil.randomNumber(terminalDistrictLowerID, terminalDistrictUpperID, gen);
            int customerID = TPCCUtil.getCustomerID(gen);

            float c_balance;
            float c_credit_lim;

            csGetCustBal.setInt(1, terminalWarehouseID);
            csGetCustBal.setInt(2, districtID);
            csGetCustBal.setInt(3, customerID);
            try (ResultSet rs = csGetCustBal.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                        String.format("csGetCustBal: c_w_id=%d, c_d_id=%d, c_id=%d not found",
                            terminalWarehouseID, districtID, customerID));
                }

                LOG.debug("[hiki] xid:{} xtype:{} stmt:{}", xid, xactType, csGetCustBal.toString());

                // Customer cust = TPCCUtil.newCustomerFromResults(rs);
                // c_balance = cust.c_balance;
                // c_credit_lim = cust.c_credit_lim;
                c_balance = rs.getFloat("c_balance");
                c_credit_lim = rs.getFloat("c_credit_lim");
            }

            // csGetNewOrderBalSQL実行
            float neworder_balance;

            csGetNewOrderBal.setInt(1, terminalWarehouseID);
            csGetNewOrderBal.setInt(2, districtID);
            csGetNewOrderBal.setInt(3, terminalWarehouseID);
            csGetNewOrderBal.setInt(4, districtID);
            csGetNewOrderBal.setInt(5, customerID);
            csGetNewOrderBal.setInt(6, terminalWarehouseID);
            csGetNewOrderBal.setInt(7, districtID);
            try (ResultSet rs = csGetNewOrderBal.executeQuery()) {
                if (!rs.next()) {
                    throw new RuntimeException(
                        String.format("csGetNewOrderBal: not get any result. stmt:%s",
                                        csGetNewOrderBal.toString())
                    );
                }

                LOG.debug("[hiki] xid:{} xtype:{} stmt:{}", xid, xactType, csGetNewOrderBal.toString());

                neworder_balance = rs.getFloat(1);
            }

            LOG.debug("[hiki] xid:{} xtype:{} c_balance:{} neworder_balance:{}", xid, xactType, c_balance, neworder_balance);

            // cs_creditの変更
            String cs_credit;
            if (c_balance + neworder_balance > c_credit_lim) {
                cs_credit = "BC";
            } else {
                cs_credit = "GC";
            }

            // cs_creditの更新
            csUpdateCustCredit.setString(1, cs_credit);
            csUpdateCustCredit.setInt(2, terminalWarehouseID);
            csUpdateCustCredit.setInt(3, districtID);
            csUpdateCustCredit.setInt(4, customerID);
            int result = csUpdateCustCredit.executeUpdate();
            if (result == 0) {
                throw new RuntimeException(
                    String.format("Error in csUpdateCustCredit: cs_credit:%s c_w_id:%d c_d_id:%d c_id:%d",
                                cs_credit, terminalWarehouseID, districtID, customerID));
            }

            LOG.debug("[hiki] xid:{} xtype:{} stmt:{}", xid, xactType, csUpdateCustCredit.toString());

            endTs = System.nanoTime() - DBWorkload.benchmarkStart;
            LOG.info(String.format(
                "[roa] tx infos(tx_type,xid,tx_start,tx_end,custId) %s %d %.3f %.3f %d",
                xactType, xid, startTs / 1000000000.0, endTs / 1000000000.0, customerID
            ));
        }
    }

}
