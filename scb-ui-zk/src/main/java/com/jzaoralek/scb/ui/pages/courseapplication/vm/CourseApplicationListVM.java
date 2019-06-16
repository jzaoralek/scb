package com.jzaoralek.scb.ui.pages.courseapplication.vm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

import com.jzaoralek.scb.dataservice.domain.Contact;
import com.jzaoralek.scb.dataservice.domain.CourseApplication;
import com.jzaoralek.scb.dataservice.domain.CourseParticipant;
import com.jzaoralek.scb.dataservice.domain.CourseParticipant.PaymentNotifSendState;
import com.jzaoralek.scb.dataservice.domain.CoursePaymentVO.CoursePaymentState;
import com.jzaoralek.scb.dataservice.exception.ScbValidationException;
import com.jzaoralek.scb.dataservice.service.CourseApplicationService;
import com.jzaoralek.scb.ui.common.WebConstants;
import com.jzaoralek.scb.ui.common.WebPages;
import com.jzaoralek.scb.ui.common.events.SzpEventListener;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper.ScbEvent;
import com.jzaoralek.scb.ui.common.utils.EventQueueHelper.ScbEventQueues;
import com.jzaoralek.scb.ui.common.utils.ExcelUtil;
import com.jzaoralek.scb.ui.common.utils.MessageBoxUtils;
import com.jzaoralek.scb.ui.common.utils.WebUtils;
import com.jzaoralek.scb.ui.common.vm.BaseContextVM;
import com.jzaoralek.scb.ui.pages.courseapplication.filter.CourseApplicationFilter;

public class CourseApplicationListVM extends BaseContextVM {

	private static final Logger LOG = LoggerFactory.getLogger(CourseApplicationListVM.class);

	public enum PageMode {
		COURSE_APPLICATION_LIST,
		CORSE_PARTICIPANT_LIST;
	}

	@WireVariable
	private CourseApplicationService courseApplicationService;

	private List<CourseApplication> courseApplicationList;
	private List<CourseApplication> courseApplicationListBase;
	private CourseApplicationFilter filter = new CourseApplicationFilter();
	private PageMode pageMode;
	private boolean unregToCurrYear;
	private String unregToCurrYearLabel;
	private final List<Listitem> coursePaymentStateListWithEmptyItem = WebUtils.getMessageItemsFromEnumWithEmptyItem(EnumSet.allOf(CoursePaymentState.class));
	private final List<Listitem> paymentNotifStateListWithEmptyItem = WebUtils.getMessageItemsFromEnumWithEmptyItem(EnumSet.of(PaymentNotifSendState.NOT_SENT_FIRST_SEMESTER, PaymentNotifSendState.NOT_SENT_SECOND_SEMESTER, PaymentNotifSendState.SENT_FIRST_SEMESTER, PaymentNotifSendState.SENT_SECOND_SEMESTER));
	private String bankAccountNumber;
	private int yearFrom;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Init
	public void init() {
		initYearContext();

		setPageMode();
		loadData();		
		this.bankAccountNumber = configurationService.getBankAccountNumber();


		final EventQueue eq = EventQueues.lookup(ScbEventQueues.COURSE_APPLICATION_QUEUE.name() , EventQueues.DESKTOP, true);
		eq.subscribe(new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				if (event.getName().equals(ScbEvent.RELOAD_COURSE_APPLICATION_DATA_EVENT.name())) {
					loadData();
				}
			}
		});
	}

	private void setPageMode() {
		if (WebUtils.getCurrentUrl().contains(WebPages.APPLICATION_LIST.getUrl())) {
			this.pageMode = PageMode.COURSE_APPLICATION_LIST;
		} else if (WebUtils.getCurrentUrl().contains(WebPages.PARTICIPANT_LIST.getUrl())) {
			this.pageMode = PageMode.CORSE_PARTICIPANT_LIST;
		} else {
			throw new IllegalStateException("Unexpected page url: " + WebUtils.getCurrentUrl());
		}
	}

	@NotifyChange("*")
	@Command
    public void deleteCmd(@BindingParam(WebConstants.ITEM_PARAM) final CourseApplication item) {
		if (item ==  null) {
			throw new IllegalArgumentException("CourseApplication is null");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Deleting application with uuid: " + item.getUuid());
		}
		final Object[] msgParams = new Object[] {item.getCourseParticipant().getContact().getCompleteName()};
		final UUID uuid = item.getUuid();
		MessageBoxUtils.showDefaultConfirmDialog(
			"msg.ui.quest.deleteApplication",
			"msg.ui.title.deleteRecord",
			new SzpEventListener() {
				@Override
				public void onOkEvent() {
					try {
						courseApplicationService.delete(uuid);
						EventQueueHelper.publish(ScbEventQueues.COURSE_APPLICATION_QUEUE, ScbEvent.RELOAD_COURSE_APPLICATION_DATA_EVENT, null, null);
						WebUtils.showNotificationInfo(Labels.getLabel("msg.ui.info.applicationDeleted", msgParams));
					} catch (ScbValidationException e) {
						LOG.warn("ScbValidationException caught for application with uuid: " + uuid);
						WebUtils.showNotificationError(e.getMessage());
					}
				}
			},
			msgParams
		);
	}

	@Command
    public void detailCmd(@BindingParam(WebConstants.UUID_PARAM) final UUID uuid, @BindingParam(WebConstants.NEW_TAB_PARAM) final Boolean newTab) {
		if (uuid ==  null) {
			throw new IllegalArgumentException("uuid is null");
		}
		String targetPage = (this.pageMode == PageMode.COURSE_APPLICATION_LIST) ? WebPages.APPLICATION_DETAIL.getUrl() : WebPages.PARTICIPANT_DETAIL.getUrl();
		WebPages fromPage = (this.pageMode == PageMode.COURSE_APPLICATION_LIST) ? WebPages.APPLICATION_LIST : WebPages.PARTICIPANT_LIST;
		if (newTab != null && newTab) {
			Executions.getCurrent().sendRedirect(targetPage + "?"+WebConstants.UUID_PARAM+"="+uuid.toString() + "&" + WebConstants.FROM_PAGE_PARAM + "=" + fromPage, "_blank");
		} else {
			Executions.getCurrent().sendRedirect(targetPage + "?"+WebConstants.UUID_PARAM+"="+uuid.toString() + "&" + WebConstants.FROM_PAGE_PARAM + "=" + fromPage);
		}
	}
	
	@Command
    public void paymentsCmd(@BindingParam(WebConstants.ITEM_PARAM) final CourseApplication item) {
		if (item ==  null) {
			throw new IllegalArgumentException("CourseApplication is null");
		}
		Executions.sendRedirect(WebPages.PAYMENT_LIST.getUrl() + "?"+WebConstants.COURSE_PARTIC_UUID_PARAM+"="+item.getCourseParticipant().getUuid().toString() + "&" +WebConstants.COURSE_UUID_PARAM+"="+item.getCourseParticipant().getCourseUuid().toString() + "&" + WebConstants.FROM_PAGE_PARAM + "=" + WebPages.PARTICIPANT_LIST);
	}

	@Command
	@NotifyChange("courseApplicationList")
	public void filterDomCmd() {
		this.courseApplicationList = filter.getApplicationListFiltered(this.courseApplicationListBase);
	}

	@NotifyChange("courseApplicationList")
	@Command
	public void exportToExcel(@BindingParam("listbox") Listbox listbox) {
		String filename = this.pageMode == PageMode.COURSE_APPLICATION_LIST ? "seznam_prihlasek.xls" : "seznam_ucastniku.xls";
		ExcelUtil.exportToExcel(filename, buildExcelRowData(listbox));
	}

	@NotifyChange("*")
	@Command
	public void refreshDataCmd() {
		loadData();
		filter.setEmptyValues();
	}

	protected void courseYearChangeCmdCore() {
		loadData();
	}
	
	@NotifyChange("*")
	@Command
	public void loadUnregisteredCurrYearCmd() {
		loadData();
	}

//	@NotifyChange("*")
//	@Command
//	public void updatePayedCmd(@BindingParam(WebConstants.CHECKED_PARAM) final Boolean checked
//			, @BindingParam(WebConstants.UUID_PARAM) final UUID uuid) {
//		if (checked == null) {
//			throw new IllegalArgumentException("checked is null");
//		}
//		if (uuid == null) {
//			throw new IllegalArgumentException("uuid is null");
//		}
//
//		try {
//			courseApplicationService.updatePayed(uuid, checked);
//			EventQueueHelper.publish(ScbEventQueues.COURSE_APPLICATION_QUEUE, ScbEvent.RELOAD_COURSE_APPLICATION_DATA_EVENT, null, null);
//		} catch (ScbValidationException e) {
//			LOG.warn("ScbValidationException caught for application with uuid: " + uuid);
//			WebUtils.showNotificationError(e.getMessage());
//		}
//
//	}
	
	@Command
	public void sendnMailToUnregisteredFromPrevSeasonCmd() {
		if (CollectionUtils.isEmpty(this.courseApplicationList)) {
			return;
		}
		final List<CourseApplication> courseApplicationList = this.courseApplicationList;
		final String courseYearSelected = getCourseYearSelected();
		final Object[] msgParams = new Object[] {this.courseApplicationList.size()};
		MessageBoxUtils.showDefaultConfirmDialog(
			"msg.ui.quest.sendMailToUnregisteredParticipant",
			"msg.ui.title.sendMail",
			new SzpEventListener() {
				@Override
				public void onOkEvent() {
					StringBuilder mailToUser = null;
					for (CourseApplication courseApplication : courseApplicationList) {
						mailToUser = new StringBuilder();
						mailToUser.append(Labels.getLabel("msg.ui.mail.unregisteredToCurrSeason.text0"));
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(Labels.getLabel("msg.ui.mail.unregisteredToCurrSeason.text1", new Object[] {courseApplication.getCourseParticipant().getContact().getCompleteName(), courseYearSelected}));
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(Labels.getLabel("msg.ui.mail.unregisteredToCurrSeason.text2", new Object[] {configurationService.getBaseURL(), courseApplication.getCourseParticipant().getUuid(), courseApplication.getCourseParticRepresentative().getUuid()}));
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(Labels.getLabel("msg.ui.mail.unregisteredToCurrSeason.text3"));
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(WebConstants.LINE_SEPARATOR);
						mailToUser.append(buildMailSignature());
						
						mailService.sendMail(courseApplication.getCourseParticRepresentative().getContact().getEmail1(), null, Labels.getLabel("msg.ui.mail.unregisteredToCurrSeason.subject", new Object[] {courseYearSelected}), mailToUser.toString(), null, false);
						WebUtils.showNotificationInfo("Obeslání uživatelů úspěšně dokončeno.");
					}
				}
			},
			msgParams
		);
	}
	
	/**
	 * Otevre stranku pro odeslani emailu na emailove adresy vybranych ucastniku.
	 */
	@Command
	public void goToSendEmailCmd() {
		goToSendEmailCore(buildCourseParticipantContactSet(this.courseApplicationList));
	}
	
	/**
	 * Sestavi seznam emailovych adres ucastniku.
	 * @param courseApplicationList
	 * @return
	 */
	private Set<Contact> buildCourseParticipantContactSet(List<CourseApplication> courseApplicationList) {
		if (CollectionUtils.isEmpty(courseApplicationList)) {
			return Collections.emptySet();
		}
		
		Set<Contact> ret = new HashSet<>();
		for (CourseApplication item : courseApplicationList) {
			if (item.getCourseParticRepresentative() != null 
					&& item.getCourseParticRepresentative().getContact() != null
					&& StringUtils.hasText(item.getCourseParticRepresentative().getContact().getEmail1())) {
				ret.add(item.getCourseParticRepresentative().getContact());				
			}
		}
		return ret;
	}

	/**
	 * Na emailove adresy zastupcu ybranych ucastniku odesle instrukce k platbe za dany kurz.
	 */
	@Command
	public void sendPaymentInstructionCmd(@BindingParam("firstSemester") final boolean firstSemester) {
		if (CollectionUtils.isEmpty(this.courseApplicationList)) {
			return;
		}
		
		// validace, ze v konfiguraci je vyplneno cislo bankovniho uctu
		final String bankAccountNumber = configurationService.getBankAccountNumber();
		if (!StringUtils.hasText(bankAccountNumber)) {
			WebUtils.showNotificationWarning(Labels.getLabel("msg.ui.warn.noBankAccountInConfig"));
			return;
		}
		
		String[] years = getYearsFromContext();
		final String yearFromTo = years[0] + "/" + years[1];
		
		Map<String, Object> args = new HashMap<>();
		args.put(WebConstants.COURSE_APPLICATION_LIST_PARAM, this.courseApplicationList);
		args.put(WebConstants.YEAR_FROM_PARAM, Integer.parseInt(years[0]));
		args.put(WebConstants.SEMESTER_PARAM, firstSemester);
		args.put(WebConstants.BANK_ACCOUNT_NO_PARAM, bankAccountNumber);
		args.put(WebConstants.YEAR_FROM_TO_PARAM, yearFromTo);
		
		WebUtils.openModal(WebPages.PAYMENT_INSTRUCTION_WINDOW.getUrl(), null, args);
		
//		final int semester = firstSemester ? 1 : 2;		
//		final List<CourseApplication> courseApplicationList = this.courseApplicationList;
//		final int yearFrom = Integer.parseInt(years[0]);
//		final Object[] msgParams = new Object[] {semester};
//		MessageBoxUtils.showDefaultConfirmDialog(
//			"msg.ui.quest.sendMailWithPaymentInstructions",
//			"msg.ui.title.sendMail",
//			new SzpEventListener() {
//				@Override
//				public void onOkEvent() {
//					StringBuilder mailToUser = null;
//					for (CourseApplication courseApplication : courseApplicationList) {
//						mailToUser = new StringBuilder();
//						mailToUser.append(Labels.getLabel("msg.ui.mail.paymentInstruction.text0"));
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						mailToUser.append(Labels.getLabel("msg.ui.mail.paymentInstruction.text1", new Object[] {courseApplication.getCourseParticipant().getCourseName(), semester, yearFromTo}));
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//
//						// cislo uctu
//						mailToUser.append(Labels.getLabel("txt.ui.common.AccountNo"));
//						mailToUser.append(": ");
//						mailToUser.append(bankAccountNumber);
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						
//						// castka
//						long priceForSemester = firstSemester ? courseApplication.getCourseParticipant().getCoursePaymentVO().getPriceFirstSemester() : courseApplication.getCourseParticipant().getCoursePaymentVO().getPriceSecondSemester();
//						mailToUser.append(Labels.getLabel("txt.ui.common.Amount"));
//						mailToUser.append(": ");
//						mailToUser.append(priceForSemester);
//						mailToUser.append(" ");
//						mailToUser.append(Labels.getLabel("txt.ui.common.CZK"));
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						
//						// variabilni symbol
//						mailToUser.append(Labels.getLabel("txt.ui.common.VarSymbol"));
//						mailToUser.append(": ");
//						mailToUser.append(buildCoursePaymentVarsymbol(yearFrom, semester, courseApplication.getCourseParticipant().getVarsymbolCore()));
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						mailToUser.append(WebConstants.LINE_SEPARATOR);
//						
//						// podpis
//						mailToUser.append(buildMailSignature());
//						
//						mailService.sendMail(courseApplication.getCourseParticRepresentative().getContact().getEmail1(), Labels.getLabel("msg.ui.mail.paymentInstruction.subject", new Object[] {courseApplication.getCourseParticipant().getCourseName(), semester, yearFromTo}), mailToUser.toString(), null);
//						WebUtils.showNotificationInfo(Labels.getLabel("msg.ui.info.messageSent"));
//					}
//				}
//			},
//			msgParams
//		);
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object[]> buildExcelRowData(@BindingParam("listbox") Listbox listbox) {
		Map<String, Object[]> data = new LinkedHashMap<String, Object[]>();

		DateFormat dateFormat = new SimpleDateFormat(WebConstants.WEB_DATETIME_PATTERN);

		// header
		Listhead lh = listbox.getListhead();
		Object[] headerArray = new Object[lh.getChildren().size() + 10];
		for (int i = 0; i < lh.getChildren().size(); i++) {
			headerArray[i] = ((Listheader) lh.getChildren().get(i)).getLabel();
		}
		
		String currency = " " + Labels.getLabel("txt.ui.common.CZK");
		
		if (this.pageMode == PageMode.COURSE_APPLICATION_LIST)  {
			headerArray[lh.getChildren().size()-1] = Labels.getLabel("txt.ui.common.birthNumber");
			headerArray[lh.getChildren().size()] = Labels.getLabel("txt.ui.common.phone");
			headerArray[lh.getChildren().size()+1] = Labels.getLabel("txt.ui.common.email");
			headerArray[lh.getChildren().size()+2] = Labels.getLabel("txt.ui.common.street");
			headerArray[lh.getChildren().size()+3] = Labels.getLabel("txt.ui.common.landRegNo");
			headerArray[lh.getChildren().size()+4] = Labels.getLabel("txt.ui.common.houseNo");
			headerArray[lh.getChildren().size()+5] = Labels.getLabel("txt.ui.common.city");
			headerArray[lh.getChildren().size()+6] = Labels.getLabel("txt.ui.common.zipCode");			
		} else {
			headerArray[lh.getChildren().size()-1] = Labels.getLabel("txt.ui.common.payed") + currency;	
			headerArray[lh.getChildren().size()] = Labels.getLabel("txt.ui.common.PriceTotal") + currency;
			headerArray[lh.getChildren().size()+1] = Labels.getLabel("txt.ui.common.phone");
			headerArray[lh.getChildren().size()+2] = Labels.getLabel("txt.ui.common.email");
			headerArray[lh.getChildren().size()+3] = Labels.getLabel("txt.ui.common.street");
			headerArray[lh.getChildren().size()+4] = Labels.getLabel("txt.ui.common.landRegNo");
			headerArray[lh.getChildren().size()+5] = Labels.getLabel("txt.ui.common.houseNo");
			headerArray[lh.getChildren().size()+6] = Labels.getLabel("txt.ui.common.city");
			headerArray[lh.getChildren().size()+7] = Labels.getLabel("txt.ui.common.zipCode");	
		}
		data.put("0", headerArray);

		
		// rows
		ListModel<Object> model = listbox.getListModel();
		CourseApplication item = null;
		for (int i = 0; i < model.getSize(); i++) {
			if (model.getElementAt(i) instanceof CourseApplication) {
				item = (CourseApplication)model.getElementAt(i);
				if (this.pageMode == PageMode.COURSE_APPLICATION_LIST) {
					data.put(String.valueOf(i+1),
						new Object[] { item.getCourseParticipant().getContact().getCompleteName(),
								getDateConverter().coerceToUi(item.getCourseParticipant().getBirthdate(), null, null),
								item.getCourseParticRepresentative().getContact().getCompleteName(),
								dateFormat.format(item.getModifAt()),
								item.getCourseParticipant().getInCourseInfo(),
								!item.isCurrentParticipant() ? Labels.getLabel("txt.ui.common.yes") : Labels.getLabel("txt.ui.common.no"),
								item.getCourseParticipant().getPersonalNo().replace("/", ""),
								item.getCourseParticRepresentative().getContact().getPhone1(),
								item.getCourseParticRepresentative().getContact().getEmail1(),								
								getNotNullString(item.getCourseParticipant().getContact().getStreet()),
								getNotNullLong(item.getCourseParticipant().getContact().getLandRegistryNumber()),
								getNotNullShort(item.getCourseParticipant().getContact().getHouseNumber()),
								getNotNullString(item.getCourseParticipant().getContact().getCity()),
								getNotNullString(item.getCourseParticipant().getContact().getZipCode())
					});
				} else {
					data.put(String.valueOf(i+1),
						new Object[] { item.getCourseParticipant().getContact().getCompleteName(),
								item.getCourseParticipant().getCourseName(),
								buildPaymentNotifiedInfo(item.getCourseParticipant()),
								item.getCourseParticipant().getCoursePaymentVO() != null ? (item.getCourseParticipant().getCoursePaymentVO().isOverpayed() ? Labels.getLabel("enum.CoursePaymentState.OVERPAYED") : getEnumLabelConverter().coerceToUi(item.getCourseParticipant().getCoursePaymentVO().getStateTotal(), null, null)) : null,
								item.getCourseParticipant().getCoursePaymentVO() != null ? item.getCourseParticipant().getCoursePaymentVO().getPaymentSum() : 0,
								item.getCourseParticipant().getCoursePaymentVO() != null ? item.getCourseParticipant().getCoursePaymentVO().getTotalPrice() : 0,
								item.getCourseParticRepresentative().getContact().getPhone1(),
								item.getCourseParticRepresentative().getContact().getEmail1(),	
								getNotNullStringEmptyChar(item.getCourseParticipant().getContact().getStreet()),
								getNotNullLongEmptyChar(item.getCourseParticipant().getContact().getLandRegistryNumber()),
								getNotNullShortEmptyChar(item.getCourseParticipant().getContact().getHouseNumber()),
								getNotNullStringEmptyChar(item.getCourseParticipant().getContact().getCity()),
								getNotNullStringEmptyChar(item.getCourseParticipant().getContact().getZipCode())
								});
				}
			}
		}

		return data;
	}
	
	/**
	 * Sestavi text s informaci o odeslani instrukci k platbe za prvni a druhe pololeti.
	 * @param cp
	 * @return
	 */
	private String buildPaymentNotifiedInfo(CourseParticipant cp) {
		if (cp == null) {
			return null;
		}
		StringBuilder ret = new StringBuilder();
		if (cp.getNotifiedSemester1PaymentAt() != null) {
			ret.append(Labels.getLabel("txt.ui.common.yes"));
		} else {
			ret.append(Labels.getLabel("txt.ui.common.no"));
		}
		ret.append(", ");
		if (cp.getNotifiedSemester2PaymentAt() != null) {
			ret.append(Labels.getLabel("txt.ui.common.yes"));
		} else {
			ret.append(Labels.getLabel("txt.ui.common.no"));
		}

		return ret.toString();
	}

	@SuppressWarnings("unchecked")
	public void loadData() {
		String[] years = getYearsFromContext();
		
		int yearFrom = Integer.parseInt(years[0]);
		int yearTo = Integer.parseInt(years[1]);
		this.yearFrom = yearFrom;
		
		this.unregToCurrYearLabel = Labels.getLabel("txt.ui.common.unregisteredFrom")+" "+String.valueOf(yearFrom-1)+"/"+String.valueOf(yearTo-1);

		if (this.unregToCurrYear) {
			this.courseApplicationList = (this.pageMode == PageMode.COURSE_APPLICATION_LIST) ? courseApplicationService.getUnregisteredToCurrYear(yearFrom, yearTo) : Collections.EMPTY_LIST;
		} else {
			this.courseApplicationList = (this.pageMode == PageMode.COURSE_APPLICATION_LIST) ? courseApplicationService.getAll(yearFrom, yearTo) : courseApplicationService.getAssignedToCourse(yearFrom, yearTo);
		}
		this.courseApplicationListBase = this.courseApplicationList;
		
		BindUtils.postNotifyChange(null, null, this, "courseApplicationList");
	}

	public List<CourseApplication> getCourseApplicationList() {
		return courseApplicationList;
	}

	public CourseApplicationFilter getFilter() {
		return filter;
	}

	public void setFilter(CourseApplicationFilter filter) {
		this.filter = filter;
	}

	public boolean isUnregToCurrYear() {
		return unregToCurrYear;
	}

	public void setUnregToCurrYear(boolean unregToCurrYear) {
		this.unregToCurrYear = unregToCurrYear;
	}
	
	public String getUnregToCurrYearLabel() {
		return unregToCurrYearLabel;
	}
	
	public List<Listitem> getCoursePaymentStateListWithEmptyItem() {
		return coursePaymentStateListWithEmptyItem;
	}
	
	public List<Listitem> getPaymentNotifStateListWithEmptyItem() {
		return paymentNotifStateListWithEmptyItem;
	}
	
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
	
	public int getYearFrom() {
		return yearFrom;
	}
}
