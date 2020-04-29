package com.wh.parallel.computing.action;

import java.util.List;
import java.util.concurrent.RecursiveTask;

import com.wh.parallel.computing.interfaces.ISimpleCallComputer;

public class SimpleTask<T, V, R> extends RecursiveTask<R> {
	private static final long serialVersionUID = 1L;

	ISimpleCallComputer<T, V, R> computer;
	List<T> datas;
	int start, end, threshold;

	public SimpleTask(List<T> datas, int start, int end, int threshold, ISimpleCallComputer<T, V, R> computer) {
		this.datas = datas;
		this.start = start;
		this.end = end;
		this.threshold = threshold;
		this.computer = computer;
	}

	protected R compute() {
		if (end - start <= threshold) {
			int i = 0;
			while (i < end) {
				try {
					computer.compute(datas.get(i++));
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
			
		}else {
			int mid = (start + end) / 2;
			SimpleTask<T, V, R> leftTask = new SimpleTask<>(datas, start, mid - 1, threshold, computer);
			SimpleTask<T, V, R> rightTask = new SimpleTask<>(datas, mid, end, threshold, computer);
			
			invokeAll(leftTask, rightTask);
			
		}

		return computer.get();
	}
}