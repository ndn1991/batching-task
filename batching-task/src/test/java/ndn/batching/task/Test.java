package ndn.batching.task;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.nhb.common.async.Callback;
import com.nhb.common.async.RPCFuture;

import ndn.batching.task.disruptor.DisruptorBatchingProcessor;
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
		batching.setBatchingProcessor(new DisruptorBatchingProcessor(workerPool) {

			@Override
			protected int process(String code, int size) {
				try {
					Thread.sleep(r.nextInt(100));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return size > 0 ? size - 1 : 0;
			}

			@Override
			protected String successMessage(String code) {
				return Thread.currentThread().getName() + " - " + code + ": success";
			}

			@Override
			protected String failureMessage(String code) {
				return Thread.currentThread().getName() + " - " + code + ": failure";
			}
		});

		ExecutorService es = Executors.newFixedThreadPool(16);
		CountDownLatch cdl = new CountDownLatch(100);

		for (int i = 0; i < 100; i++) {
			final int index = r.nextInt(10);
			es.execute(new Runnable() {
				@Override
				public void run() {
					RPCFuture<Result> future = batching.publish(String.valueOf(index));
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
