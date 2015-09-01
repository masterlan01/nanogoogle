package com.master.nanogoogle;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.master.nanogoogle.data.CntResponse;
import com.master.nanogoogle.data.TxtForm;
import com.master.nanogoogle.data.UrlForm;
import com.master.nanogoogle.interfaces.IIndexer;
import com.master.nanogoogle.interfaces.IResultItemService;
import com.master.nanogoogle.interfaces.ISnoopEvents;
import com.master.nanogoogle.interfaces.ISnooper;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	@Value("1")
	private Integer level = 1;
	@Value(" ")
	private String urlSnoop;
	@Value("10")
	private int hitsPerPage;
	@Value("1")
	private Integer firstPage;
	@Value("1")
	private Integer lastPage;
	@Value("0")
	private Integer currentPage;
	@Value("0")
	private Integer totalPages;
	@Value("-1")
	private int snooperState;
	@Value("false")
	boolean onlySite;

	private Analyzer analyzer;

	@Autowired
	ISnooper nanoSnooper;

	@Autowired
	IIndexer nanoIndexer;

	@Autowired
	IResultItemService resultItemService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String rootGet(Locale locale, Model model, HttpServletRequest request) {
		logger.info("rootGet - Корневая страница. snooperState={}", snooperState);
		model.addAttribute("txtForm", new TxtForm());
		if (snooperState == 1) {
			snooperState = -1;
			logger.info("rootGet - Корневая страница. snooperState={}", snooperState);
			return "rootpage";
		} else
			return "redirect:/index";
	}

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String indexGet(ModelMap model, HttpServletRequest request) {
		logger.info("indexGet - Стартовая страница. ");
		if (request.getParameter("q") == null) {
			logger.info("indexGet - Начало пути.");
			model.addAttribute("urlForm", new UrlForm());
			return "index";
		} else {
			model.addAttribute("txtForm", new TxtForm());
			// try {
			// String q = URLDecoder.decode(request.getParameter("q"), "UTF-8");
			// } catch (UnsupportedEncodingException e) {
			// e.printStackTrace();
			// }
			logger.info("indexGet - Стартовая страница. q= {}.", request.getParameter("q"));
			return "wait";
		}
	}

	@RequestMapping(value = "/index", method = RequestMethod.POST, produces = "text/html; charset=UTF-8")
	public String indexPost(@Valid final UrlForm form, BindingResult result, ModelMap model, Locale locale, HttpServletRequest request) {
		logger.info("indexPost - Получили URL:  \"{}\"   и    LEVEL:  \"{}\".", form.getUrl(), form.getLevel());
		logger.info("indexPost - Получили ONLYSITE:  \"{}\".", form.isOnlysite());
		logger.info("indexPost - Получили MORFO:  \"{}\".", form.isMorfo());
		onlySite = form.isOnlysite();

		if (form.isMorfo())
			analyzer = new RussianAnalyzer();
		else
			analyzer = new StandardAnalyzer();

		if (result.hasErrors()) {
			logger.info("indexPost - Полученно не валиденое поле {}.", result.getFieldError().getField());
			model.addAttribute("urlForm", form);
			return "index";
		}

		if (form.getLevel() != null)
			level = form.getLevel();
		urlSnoop = form.getUrl();

		logger.info("indexPost - Переходим дальше.  на redirect:/index?q={}", form.getUrl());
		String q = form.getUrl();
		try {
			q = URLEncoder.encode(form.getUrl(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "redirect:/index?q=" + q;
	}

	/* Обработка AJAX-запросов */
	@RequestMapping(value = "/getcnt", method = RequestMethod.GET)
	public @ResponseBody CntResponse getCnt(@RequestParam String cmd) {
		logger.info("getCnt - получили:  {}", cmd);

		if (cmd.compareTo("break") == 0) {
			nanoSnooper.finish();
			snooperState = 1; /* для отлова ситуации с ранним нажатием кнопки-Прервать (до запуска snooper-a) */
		}
		if (cmd.compareTo("run") == 0 | (cmd.compareTo("get") == 0 & snooperState == -1)) {
			snoop();
		}
		CntResponse result = new CntResponse();

		if (snooperState == 1) {
			result.setCount(-1);
		} else {
			result.setCount(nanoSnooper.getCntDocReviewed());
		}
		return result;
	}

	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String searchPost(@Valid final TxtForm txtForm, BindingResult result) {
		logger.info("searchPost - Получили TXT:  \"{}\".", txtForm.getSearchtext());
		String q = txtForm.getSearchtext();
		try {
			q = URLEncoder.encode(txtForm.getSearchtext(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.info("searchPost - Ошибка преобразования в UTF-8  :  \"{}\".", txtForm.getSearchtext());
		}
		if (txtForm.getSearchtext().trim().length() > 0)
			return "redirect:/search?q=" + q;
		else
			return "search";
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String searchGet(TxtForm txtForm, HttpServletRequest request, ModelMap model) {
		logger.info("searchGet - Получили TXT:  \"{}\".", request.getParameter("q"));
		logger.info("searchGet - Получили PAGE:  \"{}\".", request.getParameter("page"));
		logger.info("searchGet - Получили ORDER:  \"{}\"   текущее значение ORDERED: {}.", request.getParameter("order"), resultItemService.getOrdered());
		if (request.getParameter("q") == null) {
			logger.info("searchGet - Параметр TXT=null :  \"redirect:/search?q=\".");
			return "redirect:/search?q=";
		}
		if (request.getParameter("q").trim().length() > 0) {

			if (request.getParameter("page") != null) {
				try {
					getPage(new Integer(request.getParameter("page")));
				} catch (Exception e) {
					currentPage = 1;
					logger.info("searchGet - Ошибка обработки параметра PAGE:  \"{}\".", request.getParameter("page"));
				}

				if (currentPage * hitsPerPage - resultItemService.size() > hitsPerPage) { // Запрос несуществующей страницы 69 7c
					logger.info("searchGet - Запрос несуществующей страницы:  \"redirect:/search?q=\".");
					return "redirect:/search?q=";
				}
			} else {
				try {
					logger.info("searchGet - Ищем и показываем результат поиска контекста \"{}\"", request.getParameter("q"));

					resultItemService.search(request.getParameter("q"), analyzer);
					currentPage = 1;
					firstPage = 1;
					totalPages = (resultItemService.size() % hitsPerPage > 0) ? resultItemService.size() / hitsPerPage + 1
							: resultItemService.size() / hitsPerPage;
					lastPage = (totalPages > hitsPerPage) ? hitsPerPage : totalPages;

				} catch (Exception e) {
					logger.info("searchGet - Поиск контекст завершился с ошибкой.");
					e.printStackTrace();
					return "redirect:/index?q= ";
				}
			}

			if (request.getParameter("order") != null) {
				resultItemService.ordering(request.getParameter("order"));
			}
			logger.info("searchGet - Показываем результат поиска контекста \"{}\"  страница № {}", request.getParameter("q"), currentPage);
			logger.info("searchGet - totalPages:   {} ", totalPages);

			model.addAttribute("searchUri", "/nanogoogle/search?q=" + request.getParameter("q"));
			model.addAttribute("searchText", request.getParameter("q"));
			model.addAttribute("totalPages", totalPages);
			model.addAttribute("totalDocs", resultItemService.size());

			if (resultItemService.size() == 1)
				model.addAttribute("totalText", "документе");
			else {
				if (resultItemService.size() > 9) {
					String s = new Integer(resultItemService.size()).toString().substring(new Integer(resultItemService.size()).toString().length() - 2);
					Integer i = new Integer(s);
					Integer n = new Integer(s.substring(s.length() - 1));
					if (n == 1 & i != 11)
						model.addAttribute("totalText", "документе");
					else
						model.addAttribute("totalText", "документах");
				} else {
					model.addAttribute("totalText", "документах");
				}
			}

			ArrayList<Integer> pages = new ArrayList<Integer>();
			for (int i = firstPage; i <= lastPage; i++) {
				pages.add(i);
			}
			model.addAttribute("pages", pages);
			model.addAttribute("currentPage", currentPage);
			model.addAttribute("order", (resultItemService.getOrdered().compareTo("text") == 0) ? 1 : 0);

			logger.info("searchGet - listResult.size():   {}  ", resultItemService.size());
			model.addAttribute("listResult",
					resultItemService.portion((currentPage - 1) * hitsPerPage, Math.min(resultItemService.size(), hitsPerPage * currentPage - 1)));

			return "search";

		} else {
			logger.info("searchGet - Пустой контекст для поиска.");
			return "redirect:/index?q= ";
		}
	}

	private void getPage(int pageNumber) {
		Integer previousPage = currentPage;
		currentPage = pageNumber;
		if (Math.abs(previousPage - currentPage) > 1) {
			firstPage = currentPage - hitsPerPage / 2;
			lastPage = firstPage + hitsPerPage;
		} else {
			firstPage += (currentPage - previousPage);
			lastPage += (currentPage - previousPage);
		}

		if (totalPages > hitsPerPage) {
			firstPage = (firstPage <= 0) ? 1 : firstPage;
			lastPage = firstPage + hitsPerPage;
			lastPage = (lastPage > totalPages) ? totalPages : lastPage;
			if (lastPage == totalPages)
				firstPage = totalPages - hitsPerPage;

		} else {
			firstPage = 1;
			lastPage = totalPages;
		}

	}

	private void snoop() {
		try {
			nanoIndexer.setAnalyzer(analyzer);
			nanoSnooper.init(level, urlSnoop, onlySite, new ISnoopEvents() {
				public void addToIndex(String url, org.jsoup.nodes.Document doc) {
					nanoIndexer.add(url, Jsoup.parse(doc.title()).text(), Jsoup.parse(doc.html()).text());
				}
			});
			snooperState = 0;
			logger.info("snooper START");
			nanoSnooper.run();
			logger.info("snooper FINISH");
			if (nanoSnooper.getState() == -2) {
				snooperState = -1;
				nanoIndexer.clearAndClose();
			} else {
				snooperState = 1;
				nanoIndexer.optimizeAndClose();
			}

		} catch (Exception ex) {
			logger.error("ISnooper - Ошибка запуска !", ex);
		}
	}
}
