package com.solace.connector.db.pull.entity;
// Generated 19-Sep-2023, 11:35:03 am by Hibernate Tools 5.6.2.Final


import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * VchsIClacCustInvoice generated by hbm2java
 */
@Entity
@Table(name="VCHS_I_CLAC_CUST_INVOICE"
    
    , uniqueConstraints = {@UniqueConstraint(columnNames="ADB_SEQUENCE"), @UniqueConstraint(columnNames={"ADB_L_DELIVERY_STATUS", "ADB_SEQUENCE"})} 
)
public class VchsIClacCustInvoice  implements java.io.Serializable {


     private BigDecimal vchsICciId;
     private String invNo;
     private String docType;
     private Date transDate;
     private BigDecimal amtInBc;
     private BigDecimal amtInFc;
     private String baseCurrCode;
     private String forCurrCode;
     private String createdBy;
     private Date createdDate;
     private String modifiedBy;
     private Date modifiedDate;
     private String invCustCode;
     private String acctType;
     private String awbNo;
     private String aptCode;
     private String postedInd;
     private String remarks;
     private String processStatus;
     private Date processDate;
     private BigDecimal clacCuiId;
     private String adbSubject;
     private BigDecimal adbSequence;
     private BigDecimal adbSetSequence;
     private Date adbTimestamp;
     private BigDecimal adbOpcode;
     private BigDecimal adbUpdateAll;
     private String adbRefObject;
     private Character adbLDeliveryStatus;
     private BigDecimal adbLCmsequence;
     private String adbTrackingid;
     private Set<VchsIClacFinTransaction> vchsIClacFinTransactions = new HashSet<VchsIClacFinTransaction>(0);

    public VchsIClacCustInvoice() {
    }

	
    public VchsIClacCustInvoice(BigDecimal vchsICciId, BigDecimal adbSequence, BigDecimal adbOpcode) {
        this.vchsICciId = vchsICciId;
        this.adbSequence = adbSequence;
        this.adbOpcode = adbOpcode;
    }
    public VchsIClacCustInvoice(BigDecimal vchsICciId, String invNo, String docType, Date transDate, BigDecimal amtInBc, BigDecimal amtInFc, String baseCurrCode, String forCurrCode, String createdBy, Date createdDate, String modifiedBy, Date modifiedDate, String invCustCode, String acctType, String awbNo, String aptCode, String postedInd, String remarks, String processStatus, Date processDate, BigDecimal clacCuiId, String adbSubject, BigDecimal adbSequence, BigDecimal adbSetSequence, Date adbTimestamp, BigDecimal adbOpcode, BigDecimal adbUpdateAll, String adbRefObject, Character adbLDeliveryStatus, BigDecimal adbLCmsequence, String adbTrackingid, Set<VchsIClacFinTransaction> vchsIClacFinTransactions) {
       this.vchsICciId = vchsICciId;
       this.invNo = invNo;
       this.docType = docType;
       this.transDate = transDate;
       this.amtInBc = amtInBc;
       this.amtInFc = amtInFc;
       this.baseCurrCode = baseCurrCode;
       this.forCurrCode = forCurrCode;
       this.createdBy = createdBy;
       this.createdDate = createdDate;
       this.modifiedBy = modifiedBy;
       this.modifiedDate = modifiedDate;
       this.invCustCode = invCustCode;
       this.acctType = acctType;
       this.awbNo = awbNo;
       this.aptCode = aptCode;
       this.postedInd = postedInd;
       this.remarks = remarks;
       this.processStatus = processStatus;
       this.processDate = processDate;
       this.clacCuiId = clacCuiId;
       this.adbSubject = adbSubject;
       this.adbSequence = adbSequence;
       this.adbSetSequence = adbSetSequence;
       this.adbTimestamp = adbTimestamp;
       this.adbOpcode = adbOpcode;
       this.adbUpdateAll = adbUpdateAll;
       this.adbRefObject = adbRefObject;
       this.adbLDeliveryStatus = adbLDeliveryStatus;
       this.adbLCmsequence = adbLCmsequence;
       this.adbTrackingid = adbTrackingid;
       this.vchsIClacFinTransactions = vchsIClacFinTransactions;
    }
   
     @Id 

    
    @Column(name="VCHS_I_CCI_ID", unique=true, nullable=false, precision=15, scale=0)
    public BigDecimal getVchsICciId() {
        return this.vchsICciId;
    }
    
    public void setVchsICciId(BigDecimal vchsICciId) {
        this.vchsICciId = vchsICciId;
    }

    
    @Column(name="INV_NO", length=35)
    public String getInvNo() {
        return this.invNo;
    }
    
    public void setInvNo(String invNo) {
        this.invNo = invNo;
    }

    
    @Column(name="DOC_TYPE", length=10)
    public String getDocType() {
        return this.docType;
    }
    
    public void setDocType(String docType) {
        this.docType = docType;
    }

    
    @Column(name="TRANS_DATE", length=11)
    public Date getTransDate() {
        return this.transDate;
    }
    
    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    
    @Column(name="AMT_IN_BC", precision=15, scale=3)
    public BigDecimal getAmtInBc() {
        return this.amtInBc;
    }
    
    public void setAmtInBc(BigDecimal amtInBc) {
        this.amtInBc = amtInBc;
    }

    
    @Column(name="AMT_IN_FC", precision=15, scale=3)
    public BigDecimal getAmtInFc() {
        return this.amtInFc;
    }
    
    public void setAmtInFc(BigDecimal amtInFc) {
        this.amtInFc = amtInFc;
    }

    
    @Column(name="BASE_CURR_CODE", length=5)
    public String getBaseCurrCode() {
        return this.baseCurrCode;
    }
    
    public void setBaseCurrCode(String baseCurrCode) {
        this.baseCurrCode = baseCurrCode;
    }

    
    @Column(name="FOR_CURR_CODE", length=5)
    public String getForCurrCode() {
        return this.forCurrCode;
    }
    
    public void setForCurrCode(String forCurrCode) {
        this.forCurrCode = forCurrCode;
    }

    
    @Column(name="CREATED_BY", length=30)
    public String getCreatedBy() {
        return this.createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    
    @Column(name="CREATED_DATE", length=11)
    public Date getCreatedDate() {
        return this.createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    
    @Column(name="MODIFIED_BY", length=30)
    public String getModifiedBy() {
        return this.modifiedBy;
    }
    
    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    
    @Column(name="MODIFIED_DATE", length=11)
    public Date getModifiedDate() {
        return this.modifiedDate;
    }
    
    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    
    @Column(name="INV_CUST_CODE", length=14)
    public String getInvCustCode() {
        return this.invCustCode;
    }
    
    public void setInvCustCode(String invCustCode) {
        this.invCustCode = invCustCode;
    }

    
    @Column(name="ACCT_TYPE", length=10)
    public String getAcctType() {
        return this.acctType;
    }
    
    public void setAcctType(String acctType) {
        this.acctType = acctType;
    }

    
    @Column(name="AWB_NO", length=20)
    public String getAwbNo() {
        return this.awbNo;
    }
    
    public void setAwbNo(String awbNo) {
        this.awbNo = awbNo;
    }

    
    @Column(name="APT_CODE", length=5)
    public String getAptCode() {
        return this.aptCode;
    }
    
    public void setAptCode(String aptCode) {
        this.aptCode = aptCode;
    }

    
    @Column(name="POSTED_IND", length=1)
    public String getPostedInd() {
        return this.postedInd;
    }
    
    public void setPostedInd(String postedInd) {
        this.postedInd = postedInd;
    }

    
    @Column(name="REMARKS", length=500)
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    
    @Column(name="PROCESS_STATUS", length=1)
    public String getProcessStatus() {
        return this.processStatus;
    }
    
    public void setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
    }

    
    @Column(name="PROCESS_DATE", length=11)
    public Date getProcessDate() {
        return this.processDate;
    }
    
    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    
    @Column(name="CLAC_CUI_ID", precision=15, scale=0)
    public BigDecimal getClacCuiId() {
        return this.clacCuiId;
    }
    
    public void setClacCuiId(BigDecimal clacCuiId) {
        this.clacCuiId = clacCuiId;
    }

    
    @Column(name="ADB_SUBJECT")
    public String getAdbSubject() {
        return this.adbSubject;
    }
    
    public void setAdbSubject(String adbSubject) {
        this.adbSubject = adbSubject;
    }

    
    @Column(name="ADB_SEQUENCE", unique=true, nullable=false, precision=22, scale=0)
    public BigDecimal getAdbSequence() {
        return this.adbSequence;
    }
    
    public void setAdbSequence(BigDecimal adbSequence) {
        this.adbSequence = adbSequence;
    }

    
    @Column(name="ADB_SET_SEQUENCE", precision=22, scale=0)
    public BigDecimal getAdbSetSequence() {
        return this.adbSetSequence;
    }
    
    public void setAdbSetSequence(BigDecimal adbSetSequence) {
        this.adbSetSequence = adbSetSequence;
    }

    
    @Column(name="ADB_TIMESTAMP", length=13)
    public Date getAdbTimestamp() {
        return this.adbTimestamp;
    }
    
    public void setAdbTimestamp(Date adbTimestamp) {
        this.adbTimestamp = adbTimestamp;
    }

    
    @Column(name="ADB_OPCODE", nullable=false, precision=22, scale=0)
    public BigDecimal getAdbOpcode() {
        return this.adbOpcode;
    }
    
    public void setAdbOpcode(BigDecimal adbOpcode) {
        this.adbOpcode = adbOpcode;
    }

    
    @Column(name="ADB_UPDATE_ALL", precision=22, scale=0)
    public BigDecimal getAdbUpdateAll() {
        return this.adbUpdateAll;
    }
    
    public void setAdbUpdateAll(BigDecimal adbUpdateAll) {
        this.adbUpdateAll = adbUpdateAll;
    }

    
    @Column(name="ADB_REF_OBJECT", length=64)
    public String getAdbRefObject() {
        return this.adbRefObject;
    }
    
    public void setAdbRefObject(String adbRefObject) {
        this.adbRefObject = adbRefObject;
    }

    
    @Column(name="ADB_L_DELIVERY_STATUS", length=1)
    public Character getAdbLDeliveryStatus() {
        return this.adbLDeliveryStatus;
    }
    
    public void setAdbLDeliveryStatus(Character adbLDeliveryStatus) {
        this.adbLDeliveryStatus = adbLDeliveryStatus;
    }

    
    @Column(name="ADB_L_CMSEQUENCE", precision=38, scale=0)
    public BigDecimal getAdbLCmsequence() {
        return this.adbLCmsequence;
    }
    
    public void setAdbLCmsequence(BigDecimal adbLCmsequence) {
        this.adbLCmsequence = adbLCmsequence;
    }

    
    @Column(name="ADB_TRACKINGID", length=40)
    public String getAdbTrackingid() {
        return this.adbTrackingid;
    }
    
    public void setAdbTrackingid(String adbTrackingid) {
        this.adbTrackingid = adbTrackingid;
    }

@OneToMany(fetch=FetchType.LAZY, mappedBy="vchsIClacCustInvoice")
    public Set<VchsIClacFinTransaction> getVchsIClacFinTransactions() {
        return this.vchsIClacFinTransactions;
    }
    
    public void setVchsIClacFinTransactions(Set<VchsIClacFinTransaction> vchsIClacFinTransactions) {
        this.vchsIClacFinTransactions = vchsIClacFinTransactions;
    }




}


