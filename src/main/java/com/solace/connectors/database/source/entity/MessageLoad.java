package com.solace.connector.db.pull.entity;
// Generated 19-Sep-2023, 11:35:03 am by Hibernate Tools 5.6.2.Final


import java.math.BigDecimal;
import java.sql.Blob;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * MessageLoad generated by hbm2java
 */
@Entity
@Table(name="MESSAGE_LOAD"
    
)
public class MessageLoad  implements java.io.Serializable {


     private BigDecimal id;
     private Blob load;
     private String trackingId;
     private Date readTime;
     private String flag;

    public MessageLoad() {
    }

	
    public MessageLoad(BigDecimal id) {
        this.id = id;
    }
    public MessageLoad(BigDecimal id, Blob load, String trackingId, Date readTime, String flag) {
       this.id = id;
       this.load = load;
       this.trackingId = trackingId;
       this.readTime = readTime;
       this.flag = flag;
    }
   
     @Id 

    
    @Column(name="ID", unique=true, nullable=false, precision=22, scale=0)
    public BigDecimal getId() {
        return this.id;
    }
    
    public void setId(BigDecimal id) {
        this.id = id;
    }

    
    @Column(name="LOAD")
    public Blob getLoad() {
        return this.load;
    }
    
    public void setLoad(Blob load) {
        this.load = load;
    }

    
    @Column(name="TRACKING_ID", length=50)
    public String getTrackingId() {
        return this.trackingId;
    }
    
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    
    @Column(name="READ_TIME", length=7)
    public Date getReadTime() {
        return this.readTime;
    }
    
    public void setReadTime(Date readTime) {
        this.readTime = readTime;
    }

    
    @Column(name="FLAG", length=20)
    public String getFlag() {
        return this.flag;
    }
    
    public void setFlag(String flag) {
        this.flag = flag;
    }




}


