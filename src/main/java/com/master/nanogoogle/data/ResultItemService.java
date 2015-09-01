package com.master.nanogoogle.data;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import com.master.nanogoogle.interfaces.IIndexStore;
import com.master.nanogoogle.interfaces.IResultItemService;

@Service
public class ResultItemService implements IResultItemService {

	private static final Logger logger = LoggerFactory.getLogger(ResultItemService.class);

	@Value("content")
	private String textFieldName;
	@Value("url")
	private String urlFieldName;
	@Value("title")
	private String titleFieldName;
	@Value("score")
	private String ordered;

	@Autowired
	private IIndexStore store;

	@Autowired
	private ResultItem resultItem;

	public String getOrdered() {
		return ordered;
	}

	ArrayList<ResultItem> listResult = new ArrayList<ResultItem>();

	@Override
	public List<ResultItem> search(String searchText, Analyzer analyzer) throws IOException, ParseException, InvalidTokenOffsetsException {
		listResult.clear();
		IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(store.getStorName()))));
		Highlighter textHighlighter = getHighlighter(textFieldName, searchText, analyzer);
		Highlighter titleHighlighter = getHighlighter(titleFieldName, searchText, analyzer);

		QueryParser parser = new QueryParser(textFieldName, analyzer);
		Query query = parser.parse(QueryParser.escape(searchText));
		TopDocs results = searcher.search(query, 10000);
		ScoreDoc[] hits = results.scoreDocs;

		ApplicationContext ctx = new AnnotationConfigApplicationContext(ResultItem.class);

		int numTotalHits = results.totalHits;
		logger.info("Найдено документов - {}", numTotalHits);

		for (int i = 0; i < numTotalHits; i++) {
			Document doc = searcher.doc(hits[i].doc);
			resultItem = (ResultItem) ctx.getBean(ResultItem.class);

			String title = titleHighlighter.getBestFragment(analyzer, titleFieldName, doc.get(titleFieldName));
			if (title != null) {
				if (title.trim().length() == 0) {
					resultItem.setTitle(doc.get(textFieldName).substring(0, 128));
				} else
					resultItem.setTitle(title);
			} else {
				if (doc.get(titleFieldName).trim().length() > 0)
					resultItem.setTitle(doc.get(titleFieldName));
				else
					resultItem.setTitle(doc.get(textFieldName).substring(0, 128));
			}

			if (textHighlighter.getBestFragment(analyzer, textFieldName, doc.get(textFieldName)) == null) {
				String fragment = doc.get(textFieldName);
				if (fragment.length() > 256) {
					fragment = fragment.substring(fragment.indexOf(" " + searchText) - 1, fragment.indexOf(" " + searchText) + 256);
				}
				fragment = fragment.substring(0, fragment.indexOf(searchText)) + "<span class=\"select\">" + searchText + "</span>"
						+ fragment.substring(fragment.indexOf(searchText) + searchText.length());
				resultItem.setContent(fragment);
			} else
				resultItem.setContent(textHighlighter.getBestFragment(analyzer, textFieldName, doc.get(textFieldName)));

			if (doc.get(urlFieldName).length() > 72)
				resultItem.setUrl(doc.get(urlFieldName).substring(0, 72) + " ...");
			else
				resultItem.setUrl(doc.get(urlFieldName));

			resultItem.setScore(hits[i].score);
			listResult.add(resultItem);

		}
		((AnnotationConfigApplicationContext) ctx).close();
		ordered = "score";
		return listResult;
	}

	@Override
	public void append(ResultItem item) {
		listResult.add(item);
	}

	@Override
	public int size() {
		return listResult.size();
	}

	@Override
	public List<ResultItem> portion(int fromIndex, int toIndex) {
		return listResult.subList(fromIndex, toIndex);
	}

	@Override
	public void ordering(String order) {
		if (order.compareTo(ordered) != 0) {
			if (order.compareTo("text") == 0) {
				Collections.sort(listResult, new Comparator<ResultItem>() {
					public int compare(ResultItem o1, ResultItem o2) {
						if (o1.getTitle() == null)
							return -1;
						if (o2.getTitle() == null)
							return 1;
						return o1.getTitle().toUpperCase().compareTo(o2.getTitle().toUpperCase());
					}
				});
				logger.info("searchGet - сортировка по  алфавиту  ");
			} else {
				Collections.sort(listResult, new Comparator<ResultItem>() {
					public int compare(ResultItem o1, ResultItem o2) {
						if (o1.getScore() == o2.getScore())
							return 0;
						else {
							if (o1.getScore() > o2.getScore())
								return -1;
							else
								return 1;
						}
					}
				});
				logger.info("searchGet - сортировка по релевантности  ");
			}
			ordered = order;
		}
	}

	private Highlighter getHighlighter(String fieldName, String searchText, Analyzer analyzer) throws ParseException {
		QueryParser parser = new QueryParser(fieldName, analyzer);
		parser.setDefaultOperator(QueryParser.Operator.AND);
		Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"select\">", "</span>"),
				new QueryScorer(parser.parse(QueryParser.escape(searchText)), fieldName));
		highlighter.setTextFragmenter(new SimpleFragmenter(256));
		return highlighter;
	}
}
