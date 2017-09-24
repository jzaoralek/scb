package com.jzaoralek.scb.dataservice.dao.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.jzaoralek.scb.dataservice.dao.BaseTestCase;
import com.jzaoralek.scb.dataservice.dao.PaymentDao;
import com.jzaoralek.scb.dataservice.domain.Payment;
import com.jzaoralek.scb.dataservice.domain.Payment.PaymentType;

public class PaymentDaoImplTest extends BaseTestCase {

	private static final PaymentType TYPE = PaymentType.CASH;
	private static final String DESCRIPTION = "description";
	private static final Long AMOUNT = 2200L;
	private static final UUID COURSE_COURSE_PARTICIPANT_UUID = UUID.randomUUID();
	private static final Date PAYMENT_DATE = Calendar.getInstance().getTime();
	
	@Autowired
	private PaymentDao paymentDao;

	private Payment item;
	
	@Before
	public void setUp() {
		item = new Payment();
		fillIdentEntity(item);
		item.setDescription(DESCRIPTION);
		item.setPaymentDate(PAYMENT_DATE);
		item.setAmount(AMOUNT);
		item.setType(TYPE);
		item.setCourseCourseParticipantUuid(COURSE_COURSE_PARTICIPANT_UUID);

		paymentDao.insert(item);
	}

	@Test
	public void testGetByUuid() {
		Payment item = paymentDao.getByUuid(ITEM_UUID);
		Assert.assertNotNull(item);
		Assert.assertTrue(ITEM_UUID.toString().equals(item.getUuid().toString()));
		Assert.assertTrue(AMOUNT.equals(item.getAmount()));
		Assert.assertTrue(DESCRIPTION.equals(item.getDescription()));
		Assert.assertTrue(TYPE == item.getType());
		Assert.assertTrue(COURSE_COURSE_PARTICIPANT_UUID.toString().equals(item.getCourseCourseParticipantUuid().toString()));
	}

	@Test
	public void testGetByCourseCourseParticipantUuid() {
		assertList(paymentDao.getByCourseCourseParticipantUuid(COURSE_COURSE_PARTICIPANT_UUID, getYesterday(), getTomorrow()), 1 , ITEM_UUID);
	}

	@Test
	public void testUpdate() {
		String DESCRIPTION_UPDATED = "descriptionUpdated";
		item.setDescription(DESCRIPTION_UPDATED);
		Long AMOUNT_UPDATED = 2600L;
		item.setAmount(AMOUNT_UPDATED);
		item.setType(TYPE);
		item.setCourseCourseParticipantUuid(COURSE_COURSE_PARTICIPANT_UUID);

		paymentDao.update(item);

		Payment itemUpdated = paymentDao.getByUuid(ITEM_UUID);
		Assert.assertNotNull(itemUpdated);
		Assert.assertTrue(ITEM_UUID.toString().equals(itemUpdated.getUuid().toString()));
		Assert.assertTrue(DESCRIPTION_UPDATED.equals(itemUpdated.getDescription()));
		Assert.assertTrue(AMOUNT_UPDATED.equals(item.getAmount()));
		Assert.assertTrue(TYPE == item.getType());
		Assert.assertTrue(COURSE_COURSE_PARTICIPANT_UUID.toString().equals(item.getCourseCourseParticipantUuid().toString()));
	}

	@Test
	public void testDelete() {
		paymentDao.delete(item);
		Assert.assertNull(paymentDao.getByUuid(ITEM_UUID));
	}
	
	private Date getYesterday() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);
	    return cal.getTime();
	}
	
	private Date getTomorrow() {
	    final Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, 1);
	    return cal.getTime();
	}
}
