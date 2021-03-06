package com.jzaoralek.scb.dataservice.dao.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.jzaoralek.scb.dataservice.BaseTestCase;
import com.jzaoralek.scb.dataservice.domain.Course.CourseType;
import com.jzaoralek.scb.dataservice.domain.CourseApplication;


public class CourseApplicationDaoTest extends BaseTestCase {

	private CourseApplication item;

	@Before
	public void setUp() {
		item = buildCourseApplication();
		courseApplicationDao.insert(item);
	}

	@After
    public void tearDown() {
    }

	@Test
	public void testGetAll() {
		assertList(courseApplicationDao.getAll(YEAR_FROM, YEAR_TO), 1, ITEM_UUID);
	}
	
	@Test
	public void testGetUnregisteredToCurrYear() {
		courseApplicationDao.delete(item);
		item.setYearFrom(YEAR_FROM + 1);
		item.setYearTo(YEAR_TO + 1);
		courseApplicationDao.insert(item);
		assertList(courseApplicationDao.getUnregisteredToCurrYear(YEAR_FROM, YEAR_TO), 0, ITEM_UUID);
	}


	@Test
	public void testGetNotInCourse() {
		assertList(courseApplicationDao.getNotInCourse(UUID.randomUUID(), YEAR_FROM, YEAR_TO), 1, ITEM_UUID);
	}

	@Test
	public void testGetInCourse() {
		assertList(courseApplicationDao.getInCourse(UUID.randomUUID(), YEAR_FROM, YEAR_TO), 0, ITEM_UUID);
	}

	@Test
	public void testGetAssignedToCourse() {
		assertList(courseApplicationDao.getAssignedToCourse(YEAR_FROM, YEAR_TO, CourseType.STANDARD), 0, ITEM_UUID);
	}
	
	@Test
	public void testUpdateNotifiedPayment() {
		courseApplicationDao.updateNotifiedPayment(Arrays.asList(COURSE_PARTICIPANT_UUID), Calendar.getInstance().getTime(), true);
	}
	
	@Test
	public void testUpdateCourseParticInterruption() {
		courseApplicationDao.updateCourseParticInterruption(Arrays.asList(COURSE_PARTICIPANT_UUID), Calendar.getInstance().getTime());
	}
	
	@Test
	public void testUpdateCourseParticCourseUuid() {
		courseApplicationDao.updateCourseParticCourseUuid(Arrays.asList(COURSE_PARTICIPANT_UUID), COURSE_UUID);
	}
	
	@Test
	public void testInsertCourseParticInterruption() {
		courseApplicationDao.insertCourseParticInterruption(UUID.randomUUID(), COURSE_PARTICIPANT_UUID, COURSE_UUID, Calendar.getInstance().getTime());
	}
	
	@Test
	public void testGetByCourseParticipantUuid() {
		assertList(courseApplicationDao.getByCourseParticipantUuid(COURSE_PARTICIPANT_UUID), 1, item.getUuid());
	}

	@Test
	public void testGetByUuid() {
		CourseApplication item = courseApplicationDao.getByUuid(ITEM_UUID, false);
		Assert.assertNotNull(item);
		Assert.assertTrue(ITEM_UUID.toString().equals(item.getUuid().toString()));
		Assert.assertTrue(YEAR_FROM == item.getYearFrom());
		Assert.assertTrue(YEAR_TO == item.getYearTo());
	}

	@Test
	public void testUpdate() {
		item.setYearFrom(2017);
		item.setYearTo(2018);

		courseApplicationDao.update(item);
		CourseApplication item = courseApplicationDao.getByUuid(ITEM_UUID, false);
		Assert.assertNotNull(item);
		Assert.assertTrue(ITEM_UUID.toString().equals(item.getUuid().toString()));
		Assert.assertTrue(2017 == item.getYearFrom());
		Assert.assertTrue(2018 == item.getYearTo());
	}

	@Test
	public void testUpdatePayed() {
		item.setPayed(true);

		courseApplicationDao.update(item);
		CourseApplication item = courseApplicationDao.getByUuid(ITEM_UUID, false);
		Assert.assertNotNull(item);
		Assert.assertTrue(ITEM_UUID.toString().equals(item.getUuid().toString()));
		Assert.assertTrue(item.isPayed());
	}

	@Test
	public void testDelete() {
		courseApplicationDao.delete(item);
		CourseApplication item = courseApplicationDao.getByUuid(ITEM_UUID, false);
		Assert.assertNull(item);
	}
}
