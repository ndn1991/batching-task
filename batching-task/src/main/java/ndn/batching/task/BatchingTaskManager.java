package ndn.batching.task;

import com.nhb.common.async.RPCFuture;

public interface BatchingTaskManager {
	RPCFuture<Result> publish(String code);

	BatchingProcessor getBatchingProcessor();

	void setBatchingProcessor(BatchingProcessor processor);
}
