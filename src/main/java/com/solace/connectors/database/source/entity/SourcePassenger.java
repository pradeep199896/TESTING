package com.solace.connector.db.pull.entity;
// Generated 19-Sep-2023, 11:35:03 am by Hibernate Tools 5.6.2.Final


import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * SourcePassenger generated by hbm2java
 */
@Entity
@Table(name="SOURCE_PASSENGER"
    
)
public class SourcePassenger  implements java.io.Serializable {


     private SourcePassengerId id;

    public SourcePassenger() {
    }

    public SourcePassenger(SourcePassengerId id) {
       this.id = id;
    }
   
     @EmbeddedId

    
    @AttributeOverrides( {
        @AttributeOverride(name="passengerId", column=@Column(name="PASSENGER_ID", nullable=false, precision=10, scale=0) ), 
        @AttributeOverride(name="dbtrackid", column=@Column(name="DBTRACKID") ), 
        @AttributeOverride(name="readtime", column=@Column(name="READTIME", length=11) ), 
        @AttributeOverride(name="address", column=@Column(name="ADDRESS") ), 
        @AttributeOverride(name="birthday", column=@Column(name="BIRTHDAY", length=11) ), 
        @AttributeOverride(name="blobc", column=@Column(name="BLOBC") ), 
        @AttributeOverride(name="clobc", column=@Column(name="CLOBC") ), 
        @AttributeOverride(name="contactNo", column=@Column(name="CONTACT_NO") ), 
        @AttributeOverride(name="createdAt", column=@Column(name="CREATED_AT", length=11) ), 
        @AttributeOverride(name="flag", column=@Column(name="FLAG") ), 
        @AttributeOverride(name="name", column=@Column(name="NAME") ), 
        @AttributeOverride(name="nationality", column=@Column(name="NATIONALITY") ) } )
    public SourcePassengerId getId() {
        return this.id;
    }
    
    public void setId(SourcePassengerId id) {
        this.id = id;
    }




}


