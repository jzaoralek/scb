package com.jzaoralek.scb.ui.pages.courseapplication.vm;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.QueryParam;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import com.jzaoralek.scb.dataservice.domain.Course;
import com.jzaoralek.scb.dataservice.exception.ScbValidationException;
import com.jzaoralek.scb.dataservice.service.ConfigurationService;
import com.jzaoralek.scb.dataservice.service.CourseService;
import com.jzaoralek.scb.ui.common.WebConstants;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper.ScbEvent;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper.ScbEventQueues;
import com.jzaoralek.scb.ui.common.utils.WebUtils;
import com.jzaoralek.scb.ui.common.vm.BaseVM;

public class CourseVM extends BaseVM {

	private static final Logger LOG = LoggerFactory.getLogger(CourseVM.class);

	private Course course;
	private List<String> courseYearList;
	private String courseYearSelected;
	private String pageHeadline;
	private Boolean updateMode;

	@WireVariable
	private CourseService courseService;

	@WireVariable
	private ConfigurationService configurationService;

	@Init
	public void init(@QueryParam(WebConstants.UUID_PARAM) final String uuid, @QueryParam(WebConstants.FROM_PAGE_PARAM) String fromPage) {
		Course course = null;
		if (StringUtils.hasText(uuid)) {
			course = courseService.getByUuid(UUID.fromString(uuid));
		}

		if (course != null) {
			this.course = course;
			this.pageHeadline = Labels.getLabel("txt.ui.menu.courseDetail");
			this.updateMode = true;
		} else {
			this.course = new Course();
			this.pageHeadline = Labels.getLabel("txt.ui.menu.courseNew");
			this.updateMode = false;
		}

		setReturnPage(fromPage);
		this.courseYearList = configurationService.getCourseYearList();
		this.courseYearSelected = configurationService.getCourseYearList().get(0);

		EventQueues.lookup(ScbEventQueues.SDAT_COURSE_APPLICATION_QUEUE.name() , EventQueues.DESKTOP, true).subscribe(new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				if (event.getName().equals(ScbEvent.RELOAD_COURSE_PARTICIPANT_DATA_EVENT.name())) {
					loadData(uuid);
				}
			}
		});
	}

	public void loadData(String uuid) {
		this.course = courseService.getByUuid(UUID.fromString(uuid));
		BindUtils.postNotifyChange(null, null, this, "course");
	}

	@NotifyChange("*")
	@Command
    public void submit() {
		try {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Storing course: " + this.course);
			}
			course.fillYearFromTo(this.courseYearSelected);
			this.course = courseService.store(course);
			WebUtils.showNotificationInfo(Labels.getLabel("msg.ui.info.changesSaved"));
			this.updateMode = true;
		} catch (ScbValidationException e) {
			LOG.error("ScbValidationException caught for course: " + this.course, e);
			WebUtils.showNotificationError(e.getMessage());
		} catch (Exception e) {
			LOG.error("Unexpected exception caught for course: " + this.course, e);
			throw new RuntimeException(e);
		}
    }

	@Command
    public void courseParticipantDetailCmd(@BindingParam(WebConstants.UUID_PARAM) final UUID uuid) {
		if (uuid ==  null) {
			throw new IllegalArgumentException("uuid is null");
		}
	}

	@NotifyChange("*")
	@Command
    public void courseParticipantDeleteCmd(@BindingParam(WebConstants.UUID_PARAM) final UUID uuid) {
		if (uuid ==  null) {
			throw new IllegalArgumentException("uuid is null");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Deleting courseParticipant with uuid: " + uuid + " for course uuid:" + this.course.getUuid());
		}

		courseService.deleteParticipantFromCourse(uuid, this.course.getUuid());
		EventQueueHelper.publish(ScbEventQueues.SDAT_COURSE_APPLICATION_QUEUE, ScbEvent.RELOAD_COURSE_PARTICIPANT_DATA_EVENT, null, null);
		//WebUtils.showNotificationInfo(Labels.getLabel("msg.ui.info.courseDeleted"));
	}

	@Command
	public void newCourseParticipantCmd() {

	}

	@Command
	public void newLessonCmd() {

	}

	public Course getCourse() {
		return course;
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

	public String getPageHeadline() {
		return pageHeadline;
	}

	public Boolean getUpdateMode() {
		return updateMode;
	}
}