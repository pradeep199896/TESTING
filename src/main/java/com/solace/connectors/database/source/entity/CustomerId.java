package com.solace.connector.db.pull.entity;
// Generated 19-Sep-2023, 11:35:03 am by Hibernate Tools 5.6.2.Final


import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * CustomerId generated by hbm2java
 */
@Embeddable
public class CustomerId  implements java.io.Serializable {


     private BigDecimal customerId;
     private String adbTrackingid;
     private String DStatus;
     private String sequenceId;
     private String city;
     private String firstName;
     private String lastName;
     private String state;
     private String zip;
     private Date dbtime;
     private Blob blobc;
     private Clob clobc;
     private Date systemtime;
     private BigDecimal test;

    public CustomerId() {
    }

	
    public CustomerId(BigDecimal customerId) {
        this.customerId = customerId;
    }
    public CustomerId(BigDecimal customerId, String adbTrackingid, String DStatus, String sequenceId, String city, String firstName, String lastName, String state, String zip, Date dbtime, Blob blobc, Clob clobc, Date systemtime, BigDecimal test) {
       this.customerId = customerId;
       this.adbTrackingid = adbTrackingid;
       this.DStatus = DStatus;
       this.sequenceId = sequenceId;
       this.city = city;
       this.firstName = firstName;
       this.lastName = lastName;
       this.state = state;
       this.zip = zip;
       this.dbtime = dbtime;
       this.blobc = blobc;
       this.clobc = clobc;
       this.systemtime = systemtime;
       this.test = test;
    }
   


    @Column(name="CUSTOMER_ID", nullable=false, precision=10, scale=0)
    public BigDecimal getCustomerId() {
        return this.customerId;
    }
    
    public void setCustomerId(BigDecimal customerId) {
        this.customerId = customerId;
    }


    @Column(name="ADB_TRACKINGID")
    public String getAdbTrackingid() {
        return this.adbTrackingid;
    }
    
    public void setAdbTrackingid(String adbTrackingid) {
        this.adbTrackingid = adbTrackingid;
    }


    @Column(name="D_STATUS", length=5)
    public String getDStatus() {
        return this.DStatus;
    }
    
    public void setDStatus(String DStatus) {
        this.DStatus = DStatus;
    }


    @Column(name="SEQUENCE_ID", length=50)
    public String getSequenceId() {
        return this.sequenceId;
    }
    
    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }


    @Column(name="CITY")
    public String getCity() {
        return this.city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }


    @Column(name="FIRST_NAME")
    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }


    @Column(name="LAST_NAME")
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    @Column(name="STATE")
    public String getState() {
        return this.state;
    }
    
    public void setState(String state) {
        this.state = state;
    }


    @Column(name="ZIP")
    public String getZip() {
        return this.zip;
    }
    
    public void setZip(String zip) {
        this.zip = zip;
    }


    @Column(name="DBTIME", length=11)
    public Date getDbtime() {
        return this.dbtime;
    }
    
    public void setDbtime(Date dbtime) {
        this.dbtime = dbtime;
    }


    @Column(name="BLOBC")
    public Blob getBlobc() {
        return this.blobc;
    }
    
    public void setBlobc(Blob blobc) {
        this.blobc = blobc;
    }


    @Column(name="CLOBC")
    public Clob getClobc() {
        return this.clobc;
    }
    
    public void setClobc(Clob clobc) {
        this.clobc = clobc;
    }


    @Column(name="SYSTEMTIME", length=11)
    public Date getSystemtime() {
        return this.systemtime;
    }
    
    public void setSystemtime(Date systemtime) {
        this.systemtime = systemtime;
    }


    @Column(name="TEST", precision=1, scale=0)
    public BigDecimal getTest() {
        return this.test;
    }
    
    public void setTest(BigDecimal test) {
        this.test = test;
    }


   public boolean equals(Object other) {
         if ( (this == other ) ) return true;
		 if ( (other == null ) ) return false;
		 if ( !(other instanceof CustomerId) ) return false;
		 CustomerId castOther = ( CustomerId ) other; 
         
		 return ( (this.getCustomerId()==castOther.getCustomerId()) || ( this.getCustomerId()!=null && castOther.getCustomerId()!=null && this.getCustomerId().equals(castOther.getCustomerId()) ) )
 && ( (this.getAdbTrackingid()==castOther.getAdbTrackingid()) || ( this.getAdbTrackingid()!=null && castOther.getAdbTrackingid()!=null && this.getAdbTrackingid().equals(castOther.getAdbTrackingid()) ) )
 && ( (this.getDStatus()==castOther.getDStatus()) || ( this.getDStatus()!=null && castOther.getDStatus()!=null && this.getDStatus().equals(castOther.getDStatus()) ) )
 && ( (this.getSequenceId()==castOther.getSequenceId()) || ( this.getSequenceId()!=null && castOther.getSequenceId()!=null && this.getSequenceId().equals(castOther.getSequenceId()) ) )
 && ( (this.getCity()==castOther.getCity()) || ( this.getCity()!=null && castOther.getCity()!=null && this.getCity().equals(castOther.getCity()) ) )
 && ( (this.getFirstName()==castOther.getFirstName()) || ( this.getFirstName()!=null && castOther.getFirstName()!=null && this.getFirstName().equals(castOther.getFirstName()) ) )
 && ( (this.getLastName()==castOther.getLastName()) || ( this.getLastName()!=null && castOther.getLastName()!=null && this.getLastName().equals(castOther.getLastName()) ) )
 && ( (this.getState()==castOther.getState()) || ( this.getState()!=null && castOther.getState()!=null && this.getState().equals(castOther.getState()) ) )
 && ( (this.getZip()==castOther.getZip()) || ( this.getZip()!=null && castOther.getZip()!=null && this.getZip().equals(castOther.getZip()) ) )
 && ( (this.getDbtime()==castOther.getDbtime()) || ( this.getDbtime()!=null && castOther.getDbtime()!=null && this.getDbtime().equals(castOther.getDbtime()) ) )
 && ( (this.getBlobc()==castOther.getBlobc()) || ( this.getBlobc()!=null && castOther.getBlobc()!=null && this.getBlobc().equals(castOther.getBlobc()) ) )
 && ( (this.getClobc()==castOther.getClobc()) || ( this.getClobc()!=null && castOther.getClobc()!=null && this.getClobc().equals(castOther.getClobc()) ) )
 && ( (this.getSystemtime()==castOther.getSystemtime()) || ( this.getSystemtime()!=null && castOther.getSystemtime()!=null && this.getSystemtime().equals(castOther.getSystemtime()) ) )
 && ( (this.getTest()==castOther.getTest()) || ( this.getTest()!=null && castOther.getTest()!=null && this.getTest().equals(castOther.getTest()) ) );
   }
   
   public int hashCode() {
         int result = 17;
         
         result = 37 * result + ( getCustomerId() == null ? 0 : this.getCustomerId().hashCode() );
         result = 37 * result + ( getAdbTrackingid() == null ? 0 : this.getAdbTrackingid().hashCode() );
         result = 37 * result + ( getDStatus() == null ? 0 : this.getDStatus().hashCode() );
         result = 37 * result + ( getSequenceId() == null ? 0 : this.getSequenceId().hashCode() );
         result = 37 * result + ( getCity() == null ? 0 : this.getCity().hashCode() );
         result = 37 * result + ( getFirstName() == null ? 0 : this.getFirstName().hashCode() );
         result = 37 * result + ( getLastName() == null ? 0 : this.getLastName().hashCode() );
         result = 37 * result + ( getState() == null ? 0 : this.getState().hashCode() );
         result = 37 * result + ( getZip() == null ? 0 : this.getZip().hashCode() );
         result = 37 * result + ( getDbtime() == null ? 0 : this.getDbtime().hashCode() );
         result = 37 * result + ( getBlobc() == null ? 0 : this.getBlobc().hashCode() );
         result = 37 * result + ( getClobc() == null ? 0 : this.getClobc().hashCode() );
         result = 37 * result + ( getSystemtime() == null ? 0 : this.getSystemtime().hashCode() );
         result = 37 * result + ( getTest() == null ? 0 : this.getTest().hashCode() );
         return result;
   }   


}


