package com.master.nanogoogle.nano;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.master.nanogoogle.interfaces.IHtmlParser;
import com.master.nanogoogle.interfaces.ISnoopEvents;
import com.master.nanogoogle.interfaces.ISnooper;

@Component
class NanoSnooper implements ISnooper {

	private static final Logger logger = LoggerFactory.getLogger(NanoSnooper.class);
	private ISnoopEvents events;
	private NavigableSet<String> linksCurrent; // очередь ссылок на обработку
	private Set<String> linksReviewed; // просмотренныt ссылки
	private Set<String> linksFromPage; // ссылки c текущей страницы
	private HashMap<Integer, Set<String>> linksLevel; // коллекция ссылок (по уровням)
	private String seed;
	@Value("1")
	private int maxLevel;
	@Value("false")
	private boolean finishRequest;
	@Value("-1")
	private long lastRequest;
	@Value("false")
	private boolean clientOnLine;
	@Value("-1")
	private long state;
	@Value("false")
	boolean onlySite;

	public long getState() {
		return state;
	}

	@Autowired
	IHtmlParser nanoHtmlParser;

	public NanoSnooper() {
	};

	public void init(Integer level, String seed, boolean onlysite, ISnoopEvents events) {
		this.events = events;
		this.seed = seed;
		this.maxLevel = level;
		this.onlySite = onlysite;
	}

	boolean skipDoc(Document doc) {
		// <meta name=”robots” content=”noindex, follow” />
		Elements meta = doc.getElementsByTag("META");
		Object metatags[] = meta.toArray();
		for (int i = 0; i < metatags.length; i++) {
			String metatag = metatags[i].toString().toLowerCase().trim();
			if (metatag.contains("robots") & metatag.contains("noindex") & metatag.contains("follow")) {
				return true;
			}
		}
		return false;
	}

	public void run() {
		state = 0;
		linksReviewed = new HashSet<String>();
		linksReviewed.add(seed);

		linksFromPage = new HashSet<String>();
		linksFromPage.add(seed);

		linksLevel = new HashMap<Integer, Set<String>>();
		linksLevel.put(0, linksFromPage);

		linksCurrent = new TreeSet<String>();
		finishRequest = false;
		clientOnLine = true;
		lastRequest = System.currentTimeMillis();
		for (int currentLavel = 0; currentLavel < maxLevel; currentLavel++) {

			if (linksLevel == null)
				break;
			linksCurrent.addAll((Set<String>) linksLevel.get(currentLavel));
			if (linksCurrent.size() == 0)
				break;
			linksFromPage.clear();
			logger.info("Вход на  уровень {}  (в нём ссылок - {} )", (currentLavel), linksCurrent.size());

			while (!linksCurrent.isEmpty() & !finishRequest) {

				logger.info("Текущий размер очереди = {}. Всего просмотрено ссылок - {} ", linksCurrent.size(), linksReviewed.size());

				/* берём в работу первую в очереди ссылку */
				String strURL = linksCurrent.pollFirst();
				/* добавляем в список просмотренных */
				linksReviewed.add(strURL);

				try {
					logger.info("Начинаем работу над  документом [{}] ", strURL);
					Document doc = nanoHtmlParser.downloadDoc(strURL);
					if (skipDoc(doc)) {
						logger.info("Документ не рекомендован к сканированию   (meta name=robots content=noindex, follow)");
						continue;
					}
					if (doc != null) {
						if (currentLavel + 1 < maxLevel) {
							int cntLinks = 0;
							for (String l : nanoHtmlParser.extractLinks(doc, seed)) {
								l = URLDecoder.decode(l, "UTF-8");
								// logger.info("Из документа извлекли ссылку - {}", l);
								if (!linksReviewed.contains(l) && !linksCurrent.contains(l) && !linksFromPage.contains(l)) {
									if (l.startsWith("mailto:"))
										continue;
									if (!l.startsWith(seed) & onlySite)
										continue;
									/* добавляем очередную ссылку в очередь */
									if (linksFromPage.add(l))
										cntLinks++;
									// logger.info("Добавили в очередь ссылку - {} ", l);

								}
								if (clientIsLost()) {
									linksReviewed.clear();
									linksCurrent.clear();
									linksFromPage.clear();
									linksLevel.clear();
									state = -2;
									break;
								}

							}
							logger.info("Извлекли из документа [{}]   новых ссылок - {}", strURL, cntLinks);
						}
						events.addToIndex(strURL, doc);
					} else {

						if (currentLavel == 0) {
							logger.info("Ошибка  обработки стартового документа {}", strURL);
						} else
							logger.info("Ошибка  обработки документа {}", strURL);
					}

					linksLevel.put(currentLavel + 1, new TreeSet<String>());
					((Set<String>) linksLevel.get(currentLavel + 1)).addAll(linksFromPage);

				} catch (Exception ex) {
					logger.error("Ошибка обработки документа [{}].", strURL);
					logger.error(ex.toString());
				}
				if (finishRequest)
					break;
				if (clientIsLost()) {
					linksReviewed.clear();
					linksCurrent.clear();
					linksFromPage.clear();
					linksLevel.clear();
					state = -2;
					break;
				}
			}
			if (finishRequest)
				break;
			if (clientIsLost()) {
				linksReviewed.clear();
				linksCurrent.clear();
				linksFromPage.clear();
				linksLevel.clear();
				state = -2;
				break;
			}
		}
	}

	@Override
	public Integer getCntDocReviewed() {
		if (linksReviewed != null) {
			lastRequest = System.currentTimeMillis();
			return linksReviewed.size();
		} else {
			return 0;
		}
	}

	@Override
	public void finish() {
		finishRequest = true;
	}

	private boolean clientIsLost() {
		if (lastRequest > 0) {
			clientOnLine = (System.currentTimeMillis() - lastRequest < 10000);
			// logger.info("После последнего перезапроса счётчика прошло милисекунд: {}.", System.currentTimeMillis() - lastRequest);
		}

		if (!clientOnLine)
			logger.info("Связь с клиентом утрачена (нет перезапросв счётчика).");
		// else
		// logger.info("Связь с клиентом существует (есть перезапрос счётчика).");
		return !clientOnLine;
	}

}