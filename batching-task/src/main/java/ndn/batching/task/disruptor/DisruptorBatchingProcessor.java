package ndn.batching.task.disruptor;

import java.util.List;

import lombok.RequiredArgsConstructor;
import ndn.batching.task.BatchingProcessor;
import ndn.batching.task.base.BaseResult;
import ndn.batching.task.base.CodeAndResultFuture;
import ndn.batching.task.base.FutureAndResult;

@RequiredArgsConstructor
public abstract class DisruptorBatchingProcessor implements BatchingProcessor {
	private final HandleCompleteWorkerPool workerPool;

	@Override
	public void process(String code, List<CodeAndResultFuture> tasks) {
		int success = process(code, tasks.size());
		if (success < 0) {
			throw new NegativeArraySizeException();
		}
		if (success > tasks.size()) {
			throw new ArrayIndexOutOfBoundsException();
		}

		int successCode = successCode();
		int failureCode = failureCode();
		String successMessage = successMessage(code);
		String failureMessage = failureMessage(code);

		BaseResult successResult = new BaseResult(successCode, successMessage);
		BaseResult failureResult = new BaseResult(failureCode, failureMessage);
		for (int i = 0; i < success; i++) {
			workerPool.publish(new FutureAndResult(tasks.get(i).getFuture(), successResult));
		}
		for (int i = success; i < tasks.size(); i++) {
			workerPool.publish(new FutureAndResult(tasks.get(i).getFuture(), failureResult));
		}
	}

	/**
	 * 
	 * @param code
	 * @param size
	 * @return number of task success
	 */
	protected abstract int process(String code, int size);

	protected int successCode() {
		return 0;
	}

	protected int failureCode() {
		return 1;
	}

	protected String successMessage(String code) {
		return null;
	}

	protected String failureMessage(String code) {
		return null;
	}
}
