package ndn.batching.task;

import com.nhb.common.async.RPCFuture;

public interface BatchingTaskManager<P> {
	RPCFuture<Result> publish(String code, P param);

	BatchingProcessor<P> getBatchingProcessor();

	void setBatchingProcessor(BatchingProcessor<P> processor);
}
