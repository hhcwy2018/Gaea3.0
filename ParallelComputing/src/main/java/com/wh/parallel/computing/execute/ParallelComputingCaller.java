package com.wh.parallel.computing.execute;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.wh.parallel.computing.action.SimpleTask;
import com.wh.parallel.computing.interfaces.IParallelComputingCaller;
import com.wh.parallel.computing.interfaces.ISimpleCallComputer;

public class ParallelComputingCaller<T> extends ParallelComputingExecutor<T>
		implements IParallelComputingCaller<T> {

	public ParallelComputingCaller(T[] datas, int threshold) {
		super(datas, threshold);
	}

	public ParallelComputingCaller(Collection<T> datas, int threshold) {
		super(datas, threshold);
	}

	public ParallelComputingCaller(Integer workerCount, Collection<T> datas, int threshold) {
		super(workerCount, datas, threshold);
	}

	@Override
	public <V, R> R submit(ISimpleCallComputer<T, V, R> computer) throws InterruptedException, ExecutionException {
		return pool.submit(new SimpleTask<T, V, R>(datas, 0, datas.size(), threshold, computer)).get();
	}

}
