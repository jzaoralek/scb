package com.jzaoralek.scb.ui.common.vm;

import java.util.List;

import org.springframework.util.StringUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;

import com.jzaoralek.scb.dataservice.service.impl.ConfigurationServiceImpl;
import com.jzaoralek.scb.ui.common.WebConstants;
import com.jzaoralek.scb.ui.common.utils.WebUtils;

public abstract class BaseContextVM extends BaseVM {

	private List<String> courseYearList;
	private String courseYearSelected;
	
	protected abstract void courseYearChangeCmdCore();

	@NotifyChange("*")
	@Command
	public void courseYearChangeCmd() {
		WebUtils.setSessAtribute(WebConstants.YEAR_SELECTED_PARAM, this.courseYearSelected);
		courseYearChangeCmdCore();
	}
	
	protected void initYearContext() {
		this.courseYearList = configurationService.getCourseYearList();
		
		String courseYearSelectedSession = (String) WebUtils.getSessAtribute(WebConstants.YEAR_SELECTED_PARAM);
		if (StringUtils.hasText(courseYearSelectedSession)) {
			// get from session
			this.courseYearSelected = courseYearSelectedSession;
		} else {
			// get default value from configuration and store to session
			this.courseYearSelected = configurationService.getCourseApplicationYear();
			WebUtils.setSessAtribute(WebConstants.YEAR_SELECTED_PARAM, this.courseYearSelected);
		}
	}
	
	protected String[] getYearsFromContext() {
		if (!StringUtils.hasText(this.courseYearSelected)) {
			return null;
		}
		return this.courseYearSelected.split(ConfigurationServiceImpl.COURSE_YEAR_DELIMITER);
	}
	
	public List<String> getCourseYearList() {
		return courseYearList;
	}

	public String getCourseYearSelected() {
		return courseYearSelected;
	}
	
	public void setCourseYearSelected(String courseYearSelected) {
		this.courseYearSelected = courseYearSelected;
	}
}