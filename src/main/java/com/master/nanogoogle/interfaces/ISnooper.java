package com.master.nanogoogle.interfaces;

public interface ISnooper {
	public void init(Integer level, String seed, boolean onlysite, ISnoopEvents events);

	public void run();

	public void finish();

	public long getState();

	public Integer getCntDocReviewed();
}
