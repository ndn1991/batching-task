package ndn.batching.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;

import ndn.batching.task.base.BaseResult;
import ndn.batching.task.disruptor.AbstractDisruptorBatchingProcessor;
import ndn.batching.task.disruptor.HandleCompleteWorkerPool;
import ndn.batching.task.hash.FnvHash;
import ndn.batching.task.hash.Hash;
import ndn.batching.task.local.LocalBatchingTaskManager;

public class Test {
	public static void main(String[] args) throws InterruptedException {
		Random r = new Random();
		Hash hash = new FnvHash();
		BatchingTaskManager batching = new LocalBatchingTaskManager(hash, 4);
		HandleCompleteWorkerPool workerPool = new HandleCompleteWorkerPool(4, 4096).start();
		batching.setBatchingProcessor(new AbstractDisruptorBatchingProcessor(workerPool) {
			@Override
			protected List<Result> _process(String code, List<Object> params) {
				try {
					Thread.sleep(r.nextInt(100));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<Result> rs = new ArrayList<>();
				for (Object param : params) {
					int x = r.nextInt(2);
					rs.add(new BaseResult(x, Thread.currentThread().getName() + " - " + code + ": " + param + ": " + x));
				}
				return rs;
			}
		});

		ExecutorService es = Executors.newFixedThreadPool(16);
		CountDownLatch cdl = new CountDownLatch(100);

		for (int i = 0; i < 100; i++) {
			final int index = r.nextInt(10);
			final Object param = i;
			es.execute(new Runnable() {
				@Override
				public void run() {
					RPCFuture<Result> future = batching.publish(String.valueOf(index), param);
					future.setCallback(new Callback<Result>() {
						@Override
						public void apply(Result result) {
							System.out.println("result: " + result);
						}
					});
					cdl.countDown();
				}
			});
		}
		cdl.await();
	}
}
