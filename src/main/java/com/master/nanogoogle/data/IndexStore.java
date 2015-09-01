package com.master.nanogoogle.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.master.nanogoogle.interfaces.IIndexStore;

@Component
public class IndexStore implements IIndexStore {
	@Value("./LuceneIndex")
	private String storName;

	public String getStorName() {
		return storName;
	}

	public void setStorName(String storName) {
		this.storName = storName;
	}

}
