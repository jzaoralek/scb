package com.jzaoralek.scb.dataservice.domain;

import java.util.Date;
import java.util.UUID;

/**
 * Dynamic attributest on course application.
 *
 */
public class CourseApplDynAttr implements IdentEntity {

	private UUID uuid;
	private String modifBy;
	private Date modifAt;
	private UUID courseParticUuid;
	private CourseApplDynAttrConfig courseApplDynConfig;
	private String textValue;
	private Date dateValue;
	private Integer intValue;
	private Double doubleValue;
	private boolean booleanValue;
		
	public static CourseApplDynAttr ofConfig(CourseApplDynAttrConfig courseApplDynConfig) {
		CourseApplDynAttr ret = new CourseApplDynAttr();
		ret.setCourseApplDynConfig(courseApplDynConfig);
		
		return ret;
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
	public UUID getCourseParticUuid() {
		return courseParticUuid;
	}
	public void setCourseParticUuid(UUID courseParticUuid) {
		this.courseParticUuid = courseParticUuid;
	}
	public CourseApplDynAttrConfig getCourseApplDynConfig() {
		return courseApplDynConfig;
	}
	public void setCourseApplDynConfig(CourseApplDynAttrConfig courseApplDynConfig) {
		this.courseApplDynConfig = courseApplDynConfig;
	}
	public String getTextValue() {
		return textValue;
	}
	public void setTextValue(String textValue) {
		this.textValue = textValue;
	}
	public Date getDateValue() {
		return dateValue;
	}
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}
	public Integer getIntValue() {
		return intValue;
	}
	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
	}
	public Double getDoubleValue() {
		return doubleValue;
	}
	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}
	public boolean isBooleanValue() {
		return booleanValue;
	}
	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}
	
	@Override
	public String toString() {
		return "CourseApplDynAttr [uuid=" + uuid + ", modifBy=" + modifBy + ", modifAt=" + modifAt + ", courseParticUuid="
				+ courseParticUuid + ", courseApplDynConfig=" + courseApplDynConfig + ", textValue=" + textValue
				+ ", dateValue=" + dateValue + ", intValue=" + intValue + ", doubleValue=" + doubleValue
				+ ", booleanValue=" + booleanValue + "]";
	}
}
