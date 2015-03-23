package com.sole.crawler.example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

public class CountTask extends RecursiveTask<Integer>{

	private static final int THRESHOLD=1000000;
	private int start;
	private int end;
	
	public CountTask(int start,int end){
		this.start = start;
		this.end = end;
	}
	
	@Override
	protected Integer compute() {
		int sum=0;
		
		boolean isCompute = (end-start) <= THRESHOLD;
		if (isCompute){
			for (int i = start; i <= end; i++) {
				sum += i;
			}
		}else{
			int middle = (start+end)/2;
			CountTask leftTask = new CountTask(start, middle);
			CountTask rightTask = new CountTask(middle+1, end);
			
			leftTask.fork();
			rightTask.fork();
			
			int leftResult = leftTask.join();
			int rightResult = rightTask.join();
			
			sum=leftResult+rightResult;
		}
		
		return sum;
	}

	public static void main(String[] args) {
		long begin = System.currentTimeMillis();
		ForkJoinPool forkJoinPool = new ForkJoinPool();
		int start = 1;
		int end = 10000;
		CountTask task = new CountTask(start, end);
		Future<Integer> result = forkJoinPool.submit(task);
		try {
			System.out.println(start + "+...+" + end + "=" + result.get()); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		System.out.println("Compute Time="+(System.currentTimeMillis()-begin));
	}
}
