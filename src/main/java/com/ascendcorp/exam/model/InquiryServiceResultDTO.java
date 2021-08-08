package com.ascendcorp.exam.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class InquiryServiceResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String tranID;

    private String namespace;

    private String reasonCode;

    private String reasonDesc;

    private String balance;

    @JsonProperty("ref_no1")
    private String refNo1;

    @JsonProperty("ref_no2")
    private String refNo2;

    private String amount;

    private String accountName = null;

    public String getTranID() {
        return tranID;
    }

    public void setTranID(String tranID) {
        this.tranID = tranID;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getRefNo1() {
        return refNo1;
    }

    public void setRefNo1(String refNo1) {
        this.refNo1 = refNo1;
    }

    public String getRefNo2() {
        return refNo2;
    }

    public void setRefNo2(String refNo2) {
        this.refNo2 = refNo2;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    @Override
    public String toString() {
        return "InquiryServiceResultDTO [tranID=" + tranID + ",namespace = " + namespace + ", reasonCode="
                + reasonCode + ", reasonDesc=" + reasonDesc + ", balance="
                + balance + ", ref_no1=" + refNo1 + ", ref_no2=" + refNo2
                + ", amount=" + amount + " ,account_name=" + accountName + "  ]";
    }


}
