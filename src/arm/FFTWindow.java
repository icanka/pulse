package arm;

public class FFTWindow {

	/**
	 * Return a new array that is filled with samples of a Blackman window of a
	 * specified length. Throw an IllegalArgumentException if the length is less
	 * than 1 or the window type is unknown.
	 * 
	 * @param length The length of the window to be generated.
	 * @return A new array of doubles.
	 */
	public static final double[] generateBlackManWindow(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("ptolemy.math.SignalProcessing" + ".generateBlackmanWindow(): "
					+ " length of window should be greater than 0.");
		}

		int M = length - 1;
		int n;
		double[] window = new double[length];

		double twoPiOverM = 2.0 * Math.PI / M;
		double fourPiOverM = 2.0 * twoPiOverM;

		for (n = 0; n < length; n++) {
			window[n] = 0.42 - 0.5 * Math.cos(twoPiOverM * n) + 0.08 * Math.cos(fourPiOverM * n);
		}

		return window;
	}

}
