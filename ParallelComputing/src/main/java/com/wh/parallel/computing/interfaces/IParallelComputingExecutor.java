package com.wh.parallel.computing.interfaces;

import java.util.concurrent.ExecutionException;

public interface IParallelComputingExecutor<T> {

	void execute(ISimpleActionComputer<T> computer) throws InterruptedException, ExecutionException;

}