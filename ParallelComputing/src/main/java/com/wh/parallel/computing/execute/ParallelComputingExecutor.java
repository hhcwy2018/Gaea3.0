package com.wh.parallel.computing.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import com.wh.parallel.computing.action.SimpleAction;
import com.wh.parallel.computing.interfaces.IParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;

public class ParallelComputingExecutor<T> implements IParallelComputingExecutor<T> {
	final ForkJoinPool pool;
	final List<T> datas;
	final int threshold;

	public ParallelComputingExecutor(T[] datas, int threshold) {
		this(Arrays.asList(datas), threshold);
	}
	
	public ParallelComputingExecutor(Collection<T> datas, int threshold) {
		this(null, datas, threshold);
	}
	
	public ParallelComputingExecutor(Integer workerCount, Collection<T> datas, int threshold) {
		if (workerCount == null)
			pool = new ForkJoinPool();
		else {
			pool = new ForkJoinPool(workerCount);
		}
		
		this.datas = new ArrayList<>(datas);
		this.threshold = threshold;
	}

	@Override
	public void execute(ISimpleActionComputer<T> computer)
			throws InterruptedException, ExecutionException {
		SimpleAction<T> simpleAction =  new SimpleAction<T>(datas, 0, datas.size(), threshold, computer);
		pool.submit(simpleAction).get();

	}

}
