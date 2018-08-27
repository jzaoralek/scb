package com.jzaoralek.scb.ui.pages.payments.vm;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Window;

import com.jzaoralek.scb.dataservice.domain.CourseApplication;
import com.jzaoralek.scb.ui.common.WebConstants;
import com.jzaoralek.scb.ui.common.utils.WebUtils;
import com.jzaoralek.scb.ui.common.vm.BaseVM;
import com.jzaoralek.scb.ui.pages.payments.dto.PaymentInstruction;

public class PaymentInstructionWinVM extends BaseVM {

	private List<PaymentInstruction> paymentInstructionList;
	private int semester;
	private String bankAccountNumber;
	private String yearFromTo;

	@Override
	@SuppressWarnings("unchecked")
	@Init
	public void init() {
		List<CourseApplication> courseApplicationList = (List<CourseApplication>) WebUtils.getArg(WebConstants.COURSE_APPLICATION_LIST_PARAM);
		int yearFrom = (int)WebUtils.getArg(WebConstants.YEAR_FROM_PARAM);
		boolean firstSemester = (boolean)WebUtils.getArg(WebConstants.SEMESTER_PARAM);
		this.bankAccountNumber = (String)WebUtils.getArg(WebConstants.BANK_ACCOUNT_NO_PARAM);
		this.yearFromTo = (String)WebUtils.getArg(WebConstants.YEAR_FROM_TO_PARAM);
		this.pageHeadline = firstSemester ? Labels.getLabel("txt.ui.common.PaymentInstructionsFirstSemester") : Labels.getLabel("txt.ui.common.PaymentInstructionsSecondSemester");

		initData(courseApplicationList, yearFrom, firstSemester, bankAccountNumber);
	}
	
	@Command
	public void submitCmd(@BindingParam("window") Window window) {
		if (CollectionUtils.isEmpty(this.paymentInstructionList)) {
			return;
		}
		
		StringBuilder mailToUser = null;
		for (PaymentInstruction paymentInstruction : this.paymentInstructionList) {
			mailToUser = new StringBuilder();
			mailToUser.append(Labels.getLabel("msg.ui.mail.paymentInstruction.text0"));
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			mailToUser.append(Labels.getLabel("msg.ui.mail.paymentInstruction.text1", new Object[] {paymentInstruction.getCourseName(), paymentInstruction.getSemester(), yearFromTo}));
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			mailToUser.append(WebConstants.LINE_SEPARATOR);

			// cislo uctu
			mailToUser.append(Labels.getLabel("txt.ui.common.AccountNo"));
			mailToUser.append(": ");
			mailToUser.append(paymentInstruction.getBankAccountNumber());
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			
			// castka
			mailToUser.append(Labels.getLabel("txt.ui.common.Amount"));
			mailToUser.append(": ");
			mailToUser.append(paymentInstruction.getPriceSemester());
			mailToUser.append(" ");
			mailToUser.append(Labels.getLabel("txt.ui.common.CZK"));
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			
			// variabilni symbol
			mailToUser.append(Labels.getLabel("txt.ui.common.VarSymbol"));
			mailToUser.append(": ");
			mailToUser.append(paymentInstruction.getVarsymbol());
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			mailToUser.append(WebConstants.LINE_SEPARATOR);
			
			// podpis
			mailToUser.append(buildMailSignature());
			
			mailService.sendMail(paymentInstruction.getCourseParticReprEmail(), Labels.getLabel("msg.ui.mail.paymentInstruction.subject", new Object[] {paymentInstruction.getCourseName(), semester, yearFromTo}), mailToUser.toString(), null);
		}
		
		WebUtils.showNotificationInfo(Labels.getLabel("msg.ui.info.messageSent"));
		
		window.detach();
	}
	
	private void initData(List<CourseApplication> courseApplicationList, int yearFrom, boolean firstSemester, String bankAccountNumber) {
		if (CollectionUtils.isEmpty(courseApplicationList)) {
			return;
		}
		
		this.paymentInstructionList = new ArrayList<>();
		this.semester = firstSemester ? 1 :2;
		for (CourseApplication courseApplication : courseApplicationList) {
			this.paymentInstructionList.add(new PaymentInstruction(courseApplication.getCourseParticipant().getContact().getCompleteName()
					, courseApplication.getCourseParticRepresentative().getContact().getEmail1()
					, courseApplication.getCourseParticipant().getCourseName()
					, firstSemester ? courseApplication.getCourseParticipant().getCoursePaymentVO().getPriceFirstSemester() : courseApplication.getCourseParticipant().getCoursePaymentVO().getPriceSecondSemester()
					, this.semester
					, buildCoursePaymentVarsymbol(yearFrom, this.semester, courseApplication.getCourseParticipant().getVarsymbolCore())
					, bankAccountNumber));
		}
	}
	
	public List<PaymentInstruction> getPaymentInstructionList() {
		return paymentInstructionList;
	}
	public String getBankAccountNumber() {
		return bankAccountNumber;
	}
}