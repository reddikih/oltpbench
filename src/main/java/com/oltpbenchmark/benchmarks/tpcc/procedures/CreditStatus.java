package com.oltpbenchmark.benchmarks.tpcc.procedures;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import com.oltpbenchmark.benchmarks.tpcc.TPCCWorker;

public class CreditStatus extends TPCCProcedure {

    @Override
    public void run(Connection conn, Random gen, int terminalWarehouseID, int numWarehouses,
        int terminalDistrictLowerID, int terminalDistrictUpperID, TPCCWorker w) throws SQLException {
        // TODO Auto-generated method stub
    }

}
