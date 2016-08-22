package experimental;

import java.util.Arrays;

import org.jtransforms.fft.DoubleFFT_1D;

import datacollect.NSTransform;

public class TestParse {
	public static void main(String[] args) {
		double[] data = /*
						 * { 24, 11, 25, 49, 57, 50, 40, 37, 36, 38, 34, 38, 41,
						 * 33, 27, 28, 29, 22, 18, 23, 32, 37, 44, 42, 38, 42,
						 * 41, 37, 34, 34, 26, 20, 23, 37, 54, 54, 50, 48, 51,
						 * 56, 65, 58, 52, 56, 57, 41, 21, 10, 20, 38, 66, 73,
						 * 59, 52, 53, 53, 51, 55, 57, 44, 32, 26, 34, 41, 42,
						 * 38, 40, 43, 49, 51, 51, 54, 61, 59, 39, 18, 12, 29,
						 * 52, 64, 57, 52, 51, 49, 52, 48, 43, 52, 53, 37, 24,
						 * 18, 6, 3, 6, 16, 28, 34, 32, 29, 32, 32, 40, 42, 40,
						 * 48, 54, 57, 69, 57, 38, 53, 82, 98, 97, 82, 58, 51,
						 * 71, 96, 102, 89, 70, 69, 83, 89, 83, 72, 59, 60, 68,
						 * 68, 64, 59, 60, 60, 69, 77, 72, 53, 26, 9, 12, 39,
						 * 74, 90, 76, 49, 32, 38, 61, 68, 55, 44, 38, 37, 35,
						 * 28, 25, 32, 34, 28, 25, 28, 49, 81, 102, 104, 98,
						 * 102, 129, 151, 152, 134, 109, 104, 115, 122, 121,
						 * 118, 116, 120, 128, 120, 112, 104, 97, 82, 65, 40,
						 * 19, 19, 41, 67, 64, 40, 26, 26, 38, 43, 42, 40, 43,
						 * 38, 23, 10, 11, 22, 24, 21, 18, 11, 5, 2, 1, 6, 6, 1,
						 * 1, 7, 25, 40, 42, 38, 33, 12, 1, 17, 51, 75, 74, 53,
						 * 34, 21, 10, 3, -10, -19, -12, -6, -10, -6, 4, 2, -10,
						 * -20, -21, 0, 43, 85, 90, 72, 56, 67, 89, 104 }
						 */ { 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0, 4.0 };
		System.out.println(data.length);
		double[] newdata = new double[data.length * 2];
		System.arraycopy(data, 0, newdata, 0, data.length);
		System.out.println(Arrays.toString(newdata));
		DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
		fft.realForwardFull(newdata);
		System.out.println(Arrays.toString(newdata));
		double[] result = NSTransform.fftSingle(data, data.length);
		System.out.println(Arrays.toString(result));

	}
}
