package com.wh.parallel.computing.interfaces;

import java.util.List;

public interface ISimpleBatchActionComputer<T> extends  ISimpleActionComputer<T>{
	void computeBatch(List<T> t1) throws Throwable;
}