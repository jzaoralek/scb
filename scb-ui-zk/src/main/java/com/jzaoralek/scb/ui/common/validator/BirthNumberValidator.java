package com.jzaoralek.scb.ui.common.validator;

import org.springframework.util.StringUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.util.resource.Labels;

import com.jzaoralek.scb.dataservice.service.ConfigurationService;
import com.jzaoralek.scb.ui.common.utils.WebUtils;

public class BirthNumberValidator extends ScbAbstractValidator {

	private ConfigurationService configurationService;
	
	public BirthNumberValidator(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
	
	@Override
	public void validate(ValidationContext ctx) {
		String value = (String) ctx.getProperty().getValue();
		Boolean notNull = (Boolean)ctx.getBindContext().getValidatorArg("notNull");
		
		String valueWithoutDelim = WebUtils.getBirthDateWithoutDelims(value);
		if (notNull != null && notNull && StringUtils.isEmpty(valueWithoutDelim)) {
			// NOT NULL
			super.addInvalidMessage(ctx, Labels.getLabel("msg.ui.validation.err.valueRequired"));
			return;
		}

		Boolean validateFormat = (Boolean)ctx.getBindContext().getValidatorArg("validateFormat");
		
		if (validateFormat != null && validateFormat) {
			try {
				// NUMBER
				Long.valueOf(valueWithoutDelim);
			} catch (NumberFormatException e) {
				super.addInvalidMessage(ctx, Labels.getLabel("msg.ui.validation.err.invalidaBirthNumber"));
				return;
			}
		
			try {
				// DATE PART
				String valuePreDelim = value.substring(0, value.indexOf("/"));
				WebUtils.parseRcDatePart(valuePreDelim).getValue1();			
			} catch (IllegalArgumentException e) {
				super.addInvalidMessage(ctx, Labels.getLabel("msg.ui.validation.err.invalidaBirthNumber"));
				return;
			}
			
			// CHECKSUM
			if (StringUtils.hasText(valueWithoutDelim) 
					&& !WebUtils.validateBirthNoCheckSum(valueWithoutDelim, configurationService.isCheckSumBirthNumAllowed())) {
				super.addInvalidMessage(ctx, Labels.getLabel("msg.ui.validation.err.invalidaBirthNumber"));
				return;
			}
		}

        removeValidationStyle(ctx);
	}
}
