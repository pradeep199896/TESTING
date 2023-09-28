package com.solace.connector.db.pull.entity;
// Generated 19-Sep-2023, 11:35:03 am by Hibernate Tools 5.6.2.Final


import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * VwCustomer generated by hbm2java
 */
@Entity
@Table(name="VW_CUSTOMER"
    
)
public class VwCustomer  implements java.io.Serializable {


     private VwCustomerId id;

    public VwCustomer() {
    }

    public VwCustomer(VwCustomerId id) {
       this.id = id;
    }
   
     @EmbeddedId

    
    @AttributeOverrides( {
        @AttributeOverride(name="customerId", column=@Column(name="CUSTOMER_ID", nullable=false, precision=10, scale=0) ), 
        @AttributeOverride(name="adbTrackingid", column=@Column(name="ADB_TRACKINGID") ), 
        @AttributeOverride(name="DStatus", column=@Column(name="D_STATUS", length=5) ), 
        @AttributeOverride(name="sequenceId", column=@Column(name="SEQUENCE_ID", length=50) ), 
        @AttributeOverride(name="city", column=@Column(name="CITY") ), 
        @AttributeOverride(name="firstName", column=@Column(name="FIRST_NAME") ), 
        @AttributeOverride(name="lastName", column=@Column(name="LAST_NAME") ), 
        @AttributeOverride(name="state", column=@Column(name="STATE") ), 
        @AttributeOverride(name="zip", column=@Column(name="ZIP") ), 
        @AttributeOverride(name="dbtime", column=@Column(name="DBTIME", length=11) ), 
        @AttributeOverride(name="blobc", column=@Column(name="BLOBC") ), 
        @AttributeOverride(name="clobc", column=@Column(name="CLOBC") ), 
        @AttributeOverride(name="systemtime", column=@Column(name="SYSTEMTIME", length=11) ) } )
    public VwCustomerId getId() {
        return this.id;
    }
    
    public void setId(VwCustomerId id) {
        this.id = id;
    }




}


