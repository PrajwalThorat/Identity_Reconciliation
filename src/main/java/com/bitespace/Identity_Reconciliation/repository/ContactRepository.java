package com.bitespace.Identity_Reconciliation.repository;

import com.bitespace.Identity_Reconciliation.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findByEmail(String email);
    List<Contact> findByPhoneNumber(String phoneNumber);

    List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber);
}
