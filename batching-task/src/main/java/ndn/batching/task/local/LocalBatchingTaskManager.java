package ndn.batching.task.local;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.nhb.common.BaseLoggable;
import com.nhb.common.async.BaseRPCFuture;
import com.nhb.common.async.RPCFuture;

import lombok.Getter;
import lombok.Setter;
import ndn.batching.task.BatchingProcessor;
import ndn.batching.task.BatchingTaskManager;
import ndn.batching.task.Result;
import ndn.batching.task.base.CodeAndResultFuture;
import ndn.batching.task.hash.Hash;

public class LocalBatchingTaskManager<P> extends BaseLoggable implements BatchingTaskManager<P> {
	private final Hash hash;
	private final int numWorker;
	private final int modular;

	public LocalBatchingTaskManager(Hash hash, int numWorker) {
		super();
		this.hash = hash;
		if (is2Power(numWorker)) {
			this.numWorker = numWorker;
			this.modular = numWorker - 1;
		} else {
			throw new RuntimeException("numWorker must be power of 2");
		}
		start();
	}

	private static boolean is2Power(int n) {
		if (n > 1) {
			return ((n & (n - 1)) == 0);
		}
		return false;
	}

	@Getter
	@Setter
	private BatchingProcessor<P> batchingProcessor;

	private BlockingQueue<CodeAndResultFuture<P>>[] queues;
	private Thread[] threads;
	private AtomicBoolean start = new AtomicBoolean(false);

	@SuppressWarnings("unchecked")
	public void start() {
		if (start.compareAndSet(false, true)) {
			queues = new ArrayBlockingQueue[numWorker];
			threads = new Thread[numWorker];
			for (int i = 0; i < numWorker; i++) {
				final int index = i;
				BlockingQueue<CodeAndResultFuture<P>> queue = new ArrayBlockingQueue<>(1024);
				queues[index] = queue;
				threads[index] = new Thread(new Runnable() {
					@Override
					public void run() {
						while (true) {
							List<CodeAndResultFuture<P>> tmp = null;
							synchronized (queue) {
								try {
									if (queue.isEmpty()) {
										queue.wait();
									}
									tmp = new ArrayList<>();
									queue.drainTo(tmp);
								} catch (InterruptedException e) {
									getLogger().error("error", e);
								}
							}
							if (tmp != null) {
								try {
									process(tmp);
								} catch (Exception e) {
									getLogger().error("error when process batching", e);
								}
							}
						}
					}
				}, "LocalBatchingTask Thread #" + index);
				threads[index].start();
			}
		}
	}

	private void process(List<CodeAndResultFuture<P>> tasks) {
		batchingProcessor.process(tasks);
	}

	public void shutdown() {
		if (threads != null) {
			for (int i = 0; i < threads.length; i++) {
				if (threads[i] != null && threads[i].isInterrupted()) {
					threads[i].interrupt();
				}
			}
		}
	}

	@Override
	public RPCFuture<Result> publish(String code, P param) {
		BaseRPCFuture<Result> future = new BaseRPCFuture<>();
		int threadId = (int) (hash.hash(code) & modular);
		BlockingQueue<CodeAndResultFuture<P>> queue = queues[threadId];
		synchronized (queue) {
			queue.add(new CodeAndResultFuture<>(code, param, future));
			queue.notify();
		}
		return future;
	}

}
