package com.master.nanogoogle.interfaces;

import java.util.Collection;

import org.jsoup.nodes.Document;

public interface IHtmlParser {
	public Collection<String> extractLinks(Document doc, String seed);

	public Document downloadDoc(String link);
}
