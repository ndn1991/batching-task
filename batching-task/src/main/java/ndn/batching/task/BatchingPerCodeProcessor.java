package ndn.batching.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ndn.batching.task.base.CodeAndResultFuture;

public interface BatchingPerCodeProcessor<P> extends BatchingProcessor<P> {
	@Override
	default void process(List<CodeAndResultFuture<P>> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			return;
		}
		Map<String, List<CodeAndResultFuture<P>>> codeMap = new HashMap<>(tasks.size());
		for (CodeAndResultFuture<P> task : tasks) {
			if (codeMap.containsKey(task.getCode())) {
				codeMap.get(task.getCode()).add(task);
			} else {
				List<CodeAndResultFuture<P>> list = new ArrayList<>();
				list.add(task);
				codeMap.put(task.getCode(), list);
			}
		}
		for (Entry<String, List<CodeAndResultFuture<P>>> e : codeMap.entrySet()) {
			process(e.getKey(), e.getValue());
		}
	}

	void process(String key, List<CodeAndResultFuture<P>> value);
}
