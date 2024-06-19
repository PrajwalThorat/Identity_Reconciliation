package com.bitespace.Identity_Reconciliation.service;

import com.bitespace.Identity_Reconciliation.model.Contact;
import com.bitespace.Identity_Reconciliation.repository.ContactRepository;
import com.bitespace.Identity_Reconciliation.utils.LinkPrecedence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public Contact save(Contact contact) {
        if (contact.getLinkPrecedence() == null) {
            contact.setLinkPrecedence(LinkPrecedence.PRIMARY); // default to PRIMARY
        }
        return contactRepository.save(contact);
    }

    public List<Contact> findByEmail(String email) {
        return contactRepository.findByEmail(email);
    }

    public List<Contact> findByPhoneNumber(String phoneNumber) {
        return contactRepository.findByPhoneNumber(phoneNumber);
    }

    public List<Contact> findByEmailOrPhoneNumber(String email, String phoneNumber) {
        return contactRepository.findByEmailOrPhoneNumber(email, phoneNumber);
    }

    public void updateLinkPrecedence(Contact primaryContact, List<Contact> secondaryContacts) {
        for (Contact contact : secondaryContacts) {
            contact.setLinkedId(primaryContact.getId());
            contact.setLinkPrecedence(LinkPrecedence.SECONDARY);
            contactRepository.save(contact);
        }
    }
}
