package com.master.nanogoogle.interfaces;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import com.master.nanogoogle.data.ResultItem;

public interface IResultItemService {

	void append(ResultItem item);

	int size();

	void ordering(String order);

	List<ResultItem> portion(int fromIndex, int toIndex);

	public String getOrdered();

	List<ResultItem> search(String searchText, Analyzer analyzer) throws IOException, ParseException, InvalidTokenOffsetsException;
}
