package ndn.batching.task.base;

import com.nhb.common.async.BaseRPCFuture;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ndn.batching.task.Result;

@Data
@EqualsAndHashCode(of = { "code" })
public class CodeAndResultFuture {
	private final String code;
	private final BaseRPCFuture<Result> future;
}
