package com.jzaoralek.scb.dataservice.utils;

import java.util.GregorianCalendar;
import java.util.Objects;

import org.javatuples.Pair;
import org.springframework.util.StringUtils;

import com.jzaoralek.scb.dataservice.service.ConfigurationService;
import com.jzaoralek.scb.dataservice.service.impl.ConfigurationServiceImpl;

public final class PaymentUtils {
	
	private static final int VARSYMBOL_CORE_INDEX = 5;
	private PaymentUtils() {}
	
	/**
	 * Sestavi variabilni symbol pro platbu ucastnika za kurz.
	 * @param yearFrom
	 * @param semester
	 * @param courseParticVarsymbolCore
	 * @return
	 */
	public static String buildCoursePaymentVarsymbol(int yearFrom, int semester, int courseParticVarsymbolCore) {
		Objects.requireNonNull(yearFrom, "yearFrom");
		Objects.requireNonNull(semester, "semester");
		Objects.requireNonNull(courseParticVarsymbolCore, "courseParticVarsymbolCore");
		
		return String.valueOf(yearFrom) + String.valueOf(semester) + String.valueOf(courseParticVarsymbolCore);
	}
	
	/**
	 * Z variabilniho symbolu ziska zaklad ulozeny v COURSE_COURSE_PARTICIPANT.
	 * @param varsymbol
	 * @return
	 */
	public static String getVarsymbolCore(String varsymbol, String year) {
		if (!StringUtils.hasText(varsymbol)) {
			return null;
		}
		if (varsymbol.length() <= VARSYMBOL_CORE_INDEX) {
			return null;
		}
		// kontrola zda-li začíná variabilni symbol rokem
		if (!varsymbol.startsWith(year)) {
			return null;
		}
		
		// odebrat rok a pololeti ze zacatku variabliniho symbolu
		return varsymbol.substring(VARSYMBOL_CORE_INDEX);
	}
	
	/**
	 * Vraci default datum od do pro dany rocnik, tzn. od 1.9. do 30.6.
	 * @param configurationService
	 * @return
	 */
	public static Pair<GregorianCalendar, GregorianCalendar> getDefaultDateFromTo(ConfigurationService configurationService) {
		String courseYearSelected = configurationService.getCourseApplicationYear();
		if (!StringUtils.hasText(courseYearSelected)) {
			throw new IllegalStateException("Chyba v konfiguraci, COURSE_APPLICATION_YEAR neni nastaven");
		}
 		String[] years = courseYearSelected.split(ConfigurationServiceImpl.COURSE_YEAR_DELIMITER);
		int yearFrom = Integer.parseInt(years[0]);
		int yearTo = Integer.parseInt(years[1]);
		
		return new Pair<>(new GregorianCalendar(yearFrom,6,1), new GregorianCalendar(yearTo,5,30));
	}
}
