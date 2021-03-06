package com.jzaoralek.scb.dataservice.domain;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.jzaoralek.scb.dataservice.service.impl.ConfigurationServiceImpl;

public class Course implements IdentEntity {

	public enum CourseType {
		STANDARD,
		TWO_SEMESTER;
	}
	
	private UUID uuid;
	private String modifBy;
	private Date modifAt;
	private String name;
	private String description;
	private int yearFrom;
	private int yearTo;
	private Long priceSemester1;
	private Long priceSemester2;
	private List<CourseParticipant> participantList;
	private int participantCount;
	private List<Lesson> lessonList;
	private CourseLocation courseLocation;
	private Integer maxParticipantCount;
	private CourseCourseParticipantVO courseCourseParticipantVO;
	private List<ScbUser> trainerList;
	private CourseType courseType;
	private boolean active;

	public String getOccupancy() {
		return getParticipantListCount() + " / " + (this.maxParticipantCount != null ? this.maxParticipantCount : 0);
	}
	
	public boolean isFullOccupancy() {
		return getParticipantListCount() >= (this.maxParticipantCount != null ? this.maxParticipantCount : 0);
	}
	
	@Override
	public UUID getUuid() {
		return uuid;
	}
	@Override
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
	@Override
	public String getModifBy() {
		return modifBy;
	}
	@Override
	public void setModifBy(String modifBy) {
		this.modifBy = modifBy;
	}
	@Override
	public Date getModifAt() {
		return modifAt;
	}
	@Override
	public void setModifAt(Date modifAt) {
		this.modifAt = modifAt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getYearFrom() {
		return yearFrom;
	}
	public void setYearFrom(int yearFrom) {
		this.yearFrom = yearFrom;
	}
	public int getYearTo() {
		return yearTo;
	}
	public void setYearTo(int yearTo) {
		this.yearTo = yearTo;
	}
	public Long getPriceSemester1() {
		return priceSemester1;
	}
	public void setPriceSemester1(Long priceSemester1) {
		this.priceSemester1 = priceSemester1;
	}
	public Long getPriceSemester2() {
		return priceSemester2;
	}
	public void setPriceSemester2(Long priceSemester2) {
		this.priceSemester2 = priceSemester2;
	}
	public List<CourseParticipant> getParticipantList() {
		return participantList;
	}
	public void setParticipantList(List<CourseParticipant> participantList) {
		this.participantList = participantList;
	}
	public void setParticipantCount(int participantCount) {
		this.participantCount = participantCount;
	}
	public List<Lesson> getLessonList() {
		return lessonList;
	}
	public void setLessonList(List<Lesson> lessonList) {
		this.lessonList = lessonList;
	}
	public String getYear() {
		return this.yearFrom + ConfigurationServiceImpl.COURSE_YEAR_DELIMITER + this.yearTo;
	}
	public int getParticipantListCount() {
		if (CollectionUtils.isEmpty(this.participantList)) {
			return this.participantCount;
		} else {
			return this.participantList.size();
		}
	}
	public int getLessonListCount() {
		if (CollectionUtils.isEmpty(this.lessonList)) {
			return 0;
		} else {
			return this.lessonList.size();
		}
	}
	public void fillYearFromTo(String value) {
		if (!StringUtils.hasText(value)) {
			return;
		}
		String[] years = value.split(ConfigurationServiceImpl.COURSE_YEAR_DELIMITER);
		if (years.length < 2) {
			return;
		}
		this.yearFrom = Integer.valueOf(years[0]);
		this.yearTo = Integer.valueOf(years[1]);
	}
	
	public boolean isCourseStandard() {
		return this.courseType == CourseType.STANDARD;
	}
	
	public CourseLocation getCourseLocation() {
		return courseLocation;
	}
	public void setCourseLocation(CourseLocation courseLocation) {
		this.courseLocation = courseLocation;
	}
	public Integer getMaxParticipantCount() {
		return maxParticipantCount;
	}
	public void setMaxParticipantCount(Integer maxParticipantCount) {
		this.maxParticipantCount = maxParticipantCount;
	}
	public CourseCourseParticipantVO getCourseCourseParticipantVO() {
		return courseCourseParticipantVO;
	}
	public void setCourseCourseParticipantVO(CourseCourseParticipantVO courseCourseParticipantVO) {
		this.courseCourseParticipantVO = courseCourseParticipantVO;
	}
	public List<ScbUser> getTrainerList() {
		return trainerList;
	}
	public void setTrainerList(List<ScbUser> trainerList) {
		this.trainerList = trainerList;
	}
	public CourseType getCourseType() {
		return courseType;
	}
	public void setCourseType(CourseType courseType) {
		this.courseType = courseType;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "Course [uuid=" + uuid + ", modifBy=" + modifBy + ", modifAt=" + modifAt + ", name=" + name
				+ ", description=" + description + ", yearFrom=" + yearFrom + ", yearTo=" + yearTo + ", priceSemester1="
				+ priceSemester1 + ", priceSemester2=" + priceSemester2 + ", participantList=" + participantList
				+ ", participantCount=" + participantCount + ", lessonList=" + lessonList + ", courseLocation="
				+ courseLocation + ", maxParticipantCount=" + maxParticipantCount + ", courseCourseParticipantVO="
				+ courseCourseParticipantVO + ", trainerList=" + trainerList + ", courseType=" + courseType
				+ ", active=" + active + "]";
	}
}
