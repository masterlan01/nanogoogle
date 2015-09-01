package com.master.nanogoogle.nano;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.master.nanogoogle.interfaces.IHtmlParser;

@Component
final class NanoHtmlParser implements IHtmlParser {

	@Value("300")
	private static int pause = 300;
	@Value("3")
	private static int retryCnt = 3;
	@Value("1000")
	private static int timeout = 3000;

	private static final Logger logger = LoggerFactory.getLogger(NanoHtmlParser.class);

	public NanoHtmlParser() {
	}

	public Collection<String> extractLinks(Document doc, String seed) {

		Set<String> linksSet = new HashSet<String>();
		for (Element link : doc.select("a[href]")) {
			String strLink = link.attr("abs:href").trim().toLowerCase();
			if (strLink.contains("#"))
				continue;
			if (strLink.endsWith("/"))
				strLink = strLink.substring(0, strLink.length() - 1);
			if (!strLink.isEmpty())
				linksSet.add(strLink);
		}
		return Collections.unmodifiableCollection(linksSet);
	}

	public Document downloadDoc(String link) {

		int retry = retryCnt;

		do {
			try {

				Thread.sleep(pause);
				logger.info("Попытка ({})  загрузить документ {} .", retryCnt - retry + 1, link);
				Document doc = Jsoup.connect(link).userAgent("Chrome").timeout(timeout).get();
				logger.info("Документ загружен.");
				return doc;

			} catch (HttpStatusException hex) {
				logger.error(hex.toString());
				logger.error("hex.getStatusCode()={}", hex.getStatusCode());
				if (hex.getStatusCode() == 404)
					retry = 0;
			} catch (Exception ex) {
				logger.error(ex.toString());
			}
		} while (--retry > 0);
		logger.info("Документ {} не удалось загрузить.", link);
		return null;
	}
}
