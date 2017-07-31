package ndn.batching.task.disruptor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkerPool;
import com.nhb.common.BaseLoggable;

import lombok.RequiredArgsConstructor;
import ndn.batching.task.base.FutureAndResult;

@RequiredArgsConstructor
public class HandleCompleteWorkerPool extends BaseLoggable implements ExceptionHandler<FutureAndResult> {
	private final int poolSize;
	private final int ringBufferSize;

	private WorkerPool<FutureAndResult> workerPool;
	private RingBuffer<FutureAndResult> ringBuffer;

	private HandleCompleteWorker[] workers;

	private AtomicBoolean started = new AtomicBoolean(false);

	private void initWorkers() {
		workers = new HandleCompleteWorker[poolSize];
		for (int i = 0; i < poolSize; i++) {
			workers[i] = new HandleCompleteWorker();
		}
	}

	public final HandleCompleteWorkerPool start() {
		if (started.compareAndSet(false, true)) {
			initWorkers();
			this.ringBuffer = RingBuffer.createMultiProducer(new EventFactory<FutureAndResult>() {
				@Override
				public FutureAndResult newInstance() {
					return new FutureAndResult();
				}
			}, ringBufferSize);
			workerPool = new WorkerPool<>(ringBuffer, ringBuffer.newBarrier(), this, workers);
			this.ringBuffer.addGatingSequences(this.workerPool.getWorkerSequences());
			this.workerPool.start(new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
						final AtomicInteger threadNumber = new AtomicInteger(1);

						@Override
						public Thread newThread(Runnable r) {
							int index = threadNumber.getAndIncrement();
							return new Thread(r, String.format("handle complete #%d", index));
						}
					}));
		}
		return this;
	}

	public FutureAndResult publish(FutureAndResult fr) {
		final FutureAndResult futureAndResult;
		long sequence = ringBuffer.next();
		try {
			futureAndResult = ringBuffer.get(sequence);
			futureAndResult.setFuture(fr.getFuture());
			futureAndResult.setResult(fr.getResult());
		} finally {
			ringBuffer.publish(sequence);
		}
		return futureAndResult;
	}

	@Override
	public void handleEventException(Throwable ex, long sequence, FutureAndResult event) {
		getLogger().error("An error occurs when handling result at sequence: {}, data: {}", sequence, event.getResult(),
				ex);
	}

	@Override
	public void handleOnStartException(Throwable ex) {
		getLogger().error("An error occurs when starting handle result", ex);
	}

	@Override
	public void handleOnShutdownException(Throwable ex) {
		getLogger().error("An error occurs when shutting down handle result", ex);
	}
}
