package ndn.batching.task;

import java.util.List;

import ndn.batching.task.base.CodeAndResultFuture;

public interface BatchingProcessor<P> {
	void process(List<CodeAndResultFuture<P>> tasks);

	int getBatchSize();
}
