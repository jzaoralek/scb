package com.jzaoralek.scb.dataservice.dao;

import java.util.List;
import java.util.UUID;

import com.jzaoralek.scb.dataservice.domain.CourseParticipant;

public interface CourseParticipantDao {

	CourseParticipant getByUuid(UUID uuid, boolean deep);
	List<CourseParticipant> getByCourseUuid(UUID courseUuid);
	void deleteParticipantFromCourse(UUID participantUuid, UUID courseUuid);
	void deleteAllFromCourse(UUID courseUuid);
	void insetToCourse(List<CourseParticipant> courseParticipantList, UUID courseUuid);
	void insert(CourseParticipant courseParticipant);
	void update(CourseParticipant courseParticipant);
	void delete(CourseParticipant courseParticipant);
}
