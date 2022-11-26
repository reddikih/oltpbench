package com.oltpbenchmark.benchmarks.tpcc.pojo;

import java.io.Serializable;

import com.google.protobuf.Timestamp;

public class CreditStatus implements Serializable {
    
    public int cs_w_id;
    public int cs_d_id;
    public int cs_c_id;
    public String cs_credit;
    public Timestamp cs_updated_d;

    @Override
    public String toString() {
        return ("\n***************** Customer ********************"
            + "\n* cs_w_id = " + cs_w_id
            + "\n* cs_d_id = " + cs_d_id
            + "\n* cs_c_id = " + cs_c_id
            + "\n* cs_credit = " + cs_credit
            + "\n* cs_updated_d = " + cs_updated_d
        );
    }
}
