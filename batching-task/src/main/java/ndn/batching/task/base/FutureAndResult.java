package ndn.batching.task.base;

import com.nhb.common.async.BaseRPCFuture;

import lombok.Data;
import ndn.batching.task.Result;

@Data
public class FutureAndResult {
	public FutureAndResult() {

	}

	public FutureAndResult(BaseRPCFuture<Result> future, Result result) {
		this.future = future;
		this.result = result;
	}

	private BaseRPCFuture<Result> future;
	private Result result;
}
