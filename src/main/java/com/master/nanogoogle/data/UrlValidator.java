package com.master.nanogoogle.data;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UrlValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return UrlForm.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		UrlForm urlForm = (UrlForm) target;
		if (urlForm.getLevel() == null) {
			errors.rejectValue("level", "url.level", "Ошибка в поле Level.");
		}
	}
}
