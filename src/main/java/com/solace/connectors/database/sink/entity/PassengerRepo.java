package com.solace.connectors.database.sink.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PassengerRepo extends JpaRepository<Passenger, Integer>{
	
	List<Passenger> findByPassengerId(Integer Passenger_Id);

//    List<Passengers> findByName(String Name);
//
//    List<Passengers> findByNationality(String Nationality);

//    Passengers findByFirstNameAndLastName(String firstName,
//                                       String lastName);
//
//    //JPQL
//    @Query("select s from Passengers s where s.emailId = ?1")
//    Passengers getPassengersByEmailAddress(String emailId);
//
//
//    //JPQL
//    @Query("select s.firstName from Passengers s where s.emailId = ?1")
//    String getPassengersFirstNameByEmailAddress(String emailId);
//
//    //Native
//    @Query(
//            value = "SELECT * FROM tbl_Passengers s where s.email_address = ?1",
//            nativeQuery = true
//    )
//    Passengers getPassengersByEmailAddressNative(String emailId);
//
//
//    //Native Named Param
//    @Query(
//            value = "SELECT * FROM tbl_Passengers s where s.email_address = :emailId",
//            nativeQuery = true
//    )
//    Passengers getPassengersByEmailAddressNativeNamedParam(
//            @Param("emailId") String emailId
//    );
//
//    @Modifying
//    @Transactional
//    @Query(
//            value = "update tbl_Passengers set first_name = ?1 where email_address = ?2",
//            nativeQuery = true
//    )
//    int updatePassengersNameByEmailId(String firstName, String emailId);

}
