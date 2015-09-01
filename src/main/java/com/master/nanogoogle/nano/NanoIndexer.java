package com.master.nanogoogle.nano;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.master.nanogoogle.interfaces.IIndexStore;
import com.master.nanogoogle.interfaces.IIndexer;

@Component
class NanoIndexer implements IIndexer {

	private static final Logger logger = LoggerFactory.getLogger(NanoIndexer.class);

	private static IndexWriter indexWriter;

	private Analyzer analyzer;

	@Autowired
	private IIndexStore store;

	public void clearAndClose() {
		try {
			synchronized (NanoIndexer.class) {
				if (null != indexWriter) {
					if (indexWriter.isOpen()) {
						indexWriter.deleteAll();
						indexWriter.close();
					}
					indexWriter = null;
					logger.info("Индекс  закрыт.");
				} else {
					throw new IOException("Индекс уже закрыт.");
				}
			}
		} catch (IOException ex) {
			logger.error(ex.toString());
		}
	}

	public void optimizeAndClose() {
		try {
			synchronized (NanoIndexer.class) {
				if (null != indexWriter) {
					if (indexWriter.isOpen())
						indexWriter.close();
					indexWriter = null;
					logger.info("Индекс  закрыт.");
				} else {
					throw new IOException("Индекс уже закрыт.");
				}
			}
		} catch (IOException ex) {
			logger.error(ex.toString());
		}
	}

	public NanoIndexer() {
	}

	public void add(String url, String title, String content) {
		// logger.info("Подготовка документа {} .", url);
		Document doc = new Document();
		doc.add(new Field("title", title, TextField.TYPE_STORED));
		doc.add(new Field("url", url, TextField.TYPE_STORED));
		doc.add(new Field("content", content, TextField.TYPE_STORED));
		try {
			synchronized (NanoIndexer.class) {
				if (null == indexWriter) {
					init();
				}
				// logger.info("Добавление документа {} в индекс .", url);
				indexWriter.addDocument(doc);
				logger.info("Документ добавлен в индекс .", url);
			}
		} catch (IOException ex) {
			logger.error("Возникла ошибка при добавление документа  в индекс .");
			logger.error(ex.toString());
			ex.printStackTrace();
		}
	}

	public void init() {
		logger.info("Инициализации индекса .");
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		config.setOpenMode(OpenMode.CREATE); // Перезаписывать старый индекс
		try {
			indexWriter = new IndexWriter(FSDirectory.open(new File(store.getStorName()).toPath()), config);
		} catch (IOException e) {
			logger.error("Ошибка при инициализации индекса:   {} .", e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

}