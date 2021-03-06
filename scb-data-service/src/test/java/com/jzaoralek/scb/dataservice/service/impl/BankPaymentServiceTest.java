package com.jzaoralek.scb.dataservice.service.impl;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jzaoralek.scb.dataservice.BaseTestCase;
import com.jzaoralek.scb.dataservice.service.BankPaymentService;

import bank.fioclient.dto.AccountStatement;

public class BankPaymentServiceTest extends BaseTestCase {

	@Autowired
	private BankPaymentService bankPaymentService;
	
	@Ignore
	@Test
	public void testGetItemListByType() {
		Calendar datumOd = new GregorianCalendar(2018,9,1);
		Calendar datumDo = new GregorianCalendar(2018,9,9);
		AccountStatement transactions = bankPaymentService.transactions(datumOd, datumDo);
		Assert.assertNotNull(transactions);
	}
}