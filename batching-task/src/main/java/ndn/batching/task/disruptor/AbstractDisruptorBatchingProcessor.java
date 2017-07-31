package ndn.batching.task.disruptor;

import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import ndn.batching.task.BatchingProcessor;
import ndn.batching.task.Result;
import ndn.batching.task.base.CodeAndResultFuture;
import ndn.batching.task.base.FutureAndResult;

@RequiredArgsConstructor
public abstract class AbstractDisruptorBatchingProcessor implements BatchingProcessor {
	private final HandleCompleteWorkerPool workerPool;

	@Override
	public void process(String code, List<CodeAndResultFuture> tasks) {
		List<Object> params = new ArrayList<>(tasks.size());
		for (CodeAndResultFuture task : tasks) {
			params.add(task.getParam());
		}
		List<Result> results = _process(code, params);
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
	 * @param params a list params by time order
	 * @return list result by order of params
	 */
	protected abstract List<Result> _process(String code, List<Object> params);

}
