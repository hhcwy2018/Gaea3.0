package com.wh.parallel.computing.interfaces;

public interface ISimpleCallComputer<T, V, R> {
	V compute(T t1) throws Exception;
	R get();
}