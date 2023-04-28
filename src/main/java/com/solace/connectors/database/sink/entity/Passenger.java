package com.solace.connectors.database.sink.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Passenger {
	
	@Id
	private Integer passengerId;
	private String name;
	private String contactNo;
	private String address;
	private String nationality;
	private Date day;
	@Lob
	private String clobc;

	@Lob
	private byte[] blobc ;
}
