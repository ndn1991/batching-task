package ndn.batching.task.disruptor;

import com.lmax.disruptor.WorkHandler;

import ndn.batching.task.base.FutureAndResult;

public class HandleCompleteWorker implements WorkHandler<FutureAndResult> {

	@Override
	public void onEvent(FutureAndResult event) throws Exception {
		event.getFuture().setAndDone(event.getResult());
	}

}
