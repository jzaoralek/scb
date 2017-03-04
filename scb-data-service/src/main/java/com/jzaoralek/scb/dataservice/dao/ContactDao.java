package com.jzaoralek.scb.dataservice.dao;

import java.util.UUID;

import com.jzaoralek.scb.dataservice.domain.Contact;

public interface ContactDao {

	Contact getByUuid(UUID uuid);
	boolean existsByEmail(String email);
	void insert(Contact contact);
	void update(Contact contact);
	void delete(Contact contact);
}