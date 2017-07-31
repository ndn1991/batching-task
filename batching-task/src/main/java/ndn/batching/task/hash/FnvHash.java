package ndn.batching.task.hash;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FnvHash implements Hash {
	private static final long INIT = -3750763034362895579L;

	@Override
	public long hash(String code) {
		byte[] arr = code.getBytes();
		return fnv(arr, 0, arr.length, INIT);
	}

	private long fnv(final byte[] array, final int n, final int n2, long n3) {
		for (int i = n; i < n + n2; ++i) {
			n3 += (n3 << 1) + (n3 << 4) + (n3 << 5) + (n3 << 7) + (n3 << 8) + (n3 << 40);
			n3 ^= array[i];
		}
		return n3;
	}

}
