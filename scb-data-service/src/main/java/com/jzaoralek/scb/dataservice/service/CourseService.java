package com.jzaoralek.scb.dataservice.service;

import java.util.List;
import java.util.UUID;

import com.jzaoralek.scb.dataservice.domain.Course;
import com.jzaoralek.scb.dataservice.domain.CourseParticipant;
import com.jzaoralek.scb.dataservice.exception.ScbValidationException;

public interface CourseService {

	List<Course> getAll();
	Course getByUuid(UUID uuid);
	Course store(Course course) throws ScbValidationException;
	void delete(UUID uuid) throws ScbValidationException;
	void storeCourseParticipants(List<CourseParticipant> newCourseParticipantList, Course course) throws ScbValidationException;
	void deleteParticipantFromCourse(UUID participantUuid, UUID courseUuid);
}
