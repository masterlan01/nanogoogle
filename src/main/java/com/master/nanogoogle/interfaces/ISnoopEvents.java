package com.master.nanogoogle.interfaces;

import org.jsoup.nodes.Document;

public interface ISnoopEvents {
	void addToIndex(String url, Document doc);
}
