package com.master.nanogoogle.interfaces;

import org.apache.lucene.analysis.Analyzer;

public interface IIndexer {

	public void optimizeAndClose();

	public void clearAndClose();

	public void init();

	public void setAnalyzer(Analyzer analyzer);

	public void add(String url, String title, String content);
}
