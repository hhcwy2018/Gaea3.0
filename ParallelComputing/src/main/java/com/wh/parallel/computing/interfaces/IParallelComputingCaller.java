package com.wh.parallel.computing.interfaces;

import java.util.concurrent.ExecutionException;

public interface IParallelComputingCaller<T> {

	<V, R> R submit(ISimpleCallComputer<T, V, R> computer) throws InterruptedException, ExecutionException;

}
