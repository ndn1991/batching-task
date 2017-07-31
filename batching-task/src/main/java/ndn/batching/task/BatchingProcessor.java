package ndn.batching.task;

import java.util.List;

import ndn.batching.task.base.CodeAndResultFuture;

public interface BatchingProcessor {
	void process(String code, List<CodeAndResultFuture> tasks);
}
