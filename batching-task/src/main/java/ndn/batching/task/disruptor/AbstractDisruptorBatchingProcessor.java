package ndn.batching.task.disruptor;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ndn.batching.task.BatchingProcessor;
import ndn.batching.task.Result;
import ndn.batching.task.base.CodeAndResultFuture;
import ndn.batching.task.base.FutureAndResult;

@RequiredArgsConstructor
@Setter
@Getter
public abstract class AbstractDisruptorBatchingProcessor<P> implements BatchingProcessor<P> {
	private final HandleCompleteWorkerPool workerPool;

	private int batchSize = 0;

	@Override
	public void process(List<CodeAndResultFuture<P>> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			return;
		}
		int size = tasks.size();
		if (batchSize <= 0 || size <= batchSize) {
			__process(tasks);
		} else {
			for (int i = 0; i < size; i += batchSize) {
				int to = Math.min(i + batchSize, size);
				List<CodeAndResultFuture<P>> subTasks = tasks.subList(i, to);
				__process(subTasks);
			}
		}
	}

	private void __process(List<CodeAndResultFuture<P>> tasks) {
		List<P> params = new ArrayList<>(tasks.size());
		for (CodeAndResultFuture<P> task : tasks) {
			params.add(task.getParam());
		}
		List<Result> results = _process(params);
		if (results.size() < tasks.size()) {
			throw new RuntimeException("results size must equal tasks size");
		}
		if (results.size() > tasks.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		for (int i = 0; i < results.size(); i++) {
			workerPool.publish(new FutureAndResult(tasks.get(i).getFuture(), results.get(i)));
		}
	}

	/**
	 * 
	 * @param code
	 * @param params
	 *            a list params by time order
	 * @return list result by order of params
	 */
	protected abstract List<Result> _process(List<P> params);

}
