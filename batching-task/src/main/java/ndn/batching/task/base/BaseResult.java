package ndn.batching.task.base;

import lombok.Data;
import ndn.batching.task.Result;

@Data
public class BaseResult implements Result {
	private final int status;
	private final String message;
}
