package com.bitespace.Identity_Reconciliation.controller;

import com.bitespace.Identity_Reconciliation.Request.IdentifyRequest;
import com.bitespace.Identity_Reconciliation.Response.ContactResponse;
import com.bitespace.Identity_Reconciliation.model.Contact;
import com.bitespace.Identity_Reconciliation.model.ContactDetail;
import com.bitespace.Identity_Reconciliation.service.ContactService;
import com.bitespace.Identity_Reconciliation.utils.LinkPrecedence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/identify")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactResponse> identifyContact(@RequestBody IdentifyRequest request) {
        String email = request.getEmail();
        String phoneNumber = request.getPhoneNumber();

        List<Contact> contacts = contactService.findByEmailOrPhoneNumber(email, phoneNumber);

        Contact primaryContact = new Contact();
        Set<String> emails = new HashSet<>();
        Set<String> phoneNumbers = new HashSet<>();
        List<Integer> secondaryContactIds = new ArrayList<>();

        if (contacts.isEmpty()) {
            primaryContact.setEmail(email);
            primaryContact.setPhoneNumber(phoneNumber);
            primaryContact.setLinkPrecedence(LinkPrecedence.PRIMARY);
            primaryContact = contactService.save(primaryContact);
        } else {
            primaryContact = contacts.stream()
                    .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY)
                    .findFirst()
                    .orElse(contacts.get(0));

            final Contact finalPrimaryContact = primaryContact;
            contacts.forEach(contact -> {
                if (contact.getEmail() != null) emails.add(contact.getEmail());
                if (contact.getPhoneNumber() != null) phoneNumbers.add(contact.getPhoneNumber());
                if (!contact.equals(finalPrimaryContact)) secondaryContactIds.add(contact.getId());
            });

            if (contacts.size() == 1 && !contacts.get(0).getEmail().equals(email) && !contacts.get(0).getPhoneNumber().equals(phoneNumber)) {
                Contact secondaryContact = new Contact();
                secondaryContact.setEmail(email);
                secondaryContact.setPhoneNumber(phoneNumber);
                secondaryContact.setLinkPrecedence(LinkPrecedence.SECONDARY);
                secondaryContact.setLinkedId(primaryContact.getId());
                secondaryContact = contactService.save(secondaryContact);
                secondaryContactIds.add(secondaryContact.getId());
            } else {
                contactService.updateLinkPrecedence(finalPrimaryContact, contacts.stream()
                        .filter(contact -> contact.getLinkPrecedence() == LinkPrecedence.PRIMARY && !contact.equals(finalPrimaryContact))
                        .collect(Collectors.toList()));
            }
        }

        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPrimaryContactId(primaryContact.getId());
        contactDetail.setEmails(new ArrayList<>(emails));
        contactDetail.setPhoneNumbers(new ArrayList<>(phoneNumbers));
        contactDetail.setSecondaryContactIds(secondaryContactIds);

        ContactResponse response = new ContactResponse();
        response.setContact(contactDetail);
        return ResponseEntity.ok(response);
    }
}
