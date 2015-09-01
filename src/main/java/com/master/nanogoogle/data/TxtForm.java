package com.master.nanogoogle.data;

import org.hibernate.validator.constraints.NotBlank;

public class TxtForm {
	@NotBlank(message = "Контекст не должен быть пустым")
	private String searchtext;

	public String getSearchtext() {
		return searchtext;
	}

	public void setSearchtext(String searchtext) {
		this.searchtext = searchtext;
	}

}
