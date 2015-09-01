package com.master.nanogoogle.data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.URL;
import org.springframework.context.annotation.Scope;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UrlForm {

	@URL(message = "Опаньки ... ,   а URL  то не коректный ")
	private String url;

	@NumberFormat(style = Style.NUMBER)
	@Min(1)
	@Max(3)
	private Integer level;

	boolean onlysite;

	public boolean isMorfo() {
		return morfo;
	}

	public void setMorfo(boolean morfo) {
		this.morfo = morfo;
	}

	boolean morfo;

	public boolean isOnlysite() {
		return onlysite;
	}

	public void setOnlysite(boolean onlysite) {
		this.onlysite = onlysite;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}