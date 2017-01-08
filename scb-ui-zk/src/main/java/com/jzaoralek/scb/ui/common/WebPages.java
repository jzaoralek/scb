package com.jzaoralek.scb.ui.common;

public enum WebPages {
	APPLICATION_LIST("/pages/secured/ADMIN/seznam-prihlasek.zul"),
	APPLICATION_DETAIL("/pages/secured/ADMIN/prihlaska-do-klubu.zul"),
	COURSE_LIST("/pages/secured/TRAINER/seznam-kurzu.zul"),
	COURSE_DETAIL("/pages/secured/ADMIN/kurz.zul"),
	PARTICIPANT_LIST("/pages/secured/TRAINER/seznam-ucastniku.zul"),
	PARTICIPANT_DETAIL("/pages/secured/TRAINER/ucastnik.zul"),
	USER_LIST("/pages/secured/ADMIN/seznam-uzivatelu.zul"),
	USER_DETAIL("/pages/secured/ADMIN/uzivatel.zul");

	private String url;

	WebPages(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
