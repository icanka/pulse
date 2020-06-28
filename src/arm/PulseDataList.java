package arm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import flanagan.complex.Complex;
import flanagan.math.FourierTransform;
import utils.ByteIntUtil;

public class PulseDataList {

	static FourierTransform fft = new FourierTransform();
	static double MATCH_MULTIPLIER = Math.pow(2, 15);

	private String parentRecordPath = "";
	private ArrayList<Pulse> pulseList = new ArrayList<Pulse>();

	private double[][] amplitudeListInDB;
	private double[][] amplitudeListInLinear;

	private float dataIAfterFFT[][];
	private float dataQAfterFFT[][];

	private int pulseCount;
	private int rangeCount;

	public boolean loadData(String recordName, int timeInSec) {

		Object[] a = FileUtils.listFiles(new File(parentRecordPath), null, true).toArray();

		if (a.length > ((timeInSec * 3))) {
			importRKADataBuf(parentRecordPath + "/" + (timeInSec * 3));
			return true;
		} else {
			return false;
		}
	}

	public int importRKADataBuf(String fileName) {
		pulseList = new ArrayList<Pulse>();

		boolean isEOFReached = false;

		int totalRangeIndex = 268;
		byte header[] = new byte[12];

		// 1072 sized byte array
		byte rangeBuffer[] = new byte[268 * 4];
		byte cmdIdSize[] = new byte[8];

		try {

			// create a reader
			FileInputStream fis = new FileInputStream(fileName);
			BufferedInputStream reader = new BufferedInputStream(fis);

			System.out.println("Reading Started");

			int count = 0;
			while (!isEOFReached) {

				for (int packetIndex = 0; packetIndex < 8; packetIndex++) {

					// reader 8 byte into cmdIdSize array
					reader.read(cmdIdSize);

					for (int pulseIndex = 0; pulseIndex < 32; pulseIndex++) {
						count++;

						// read 12 bytes into header array
						if (reader.read(header) == -1)
							isEOFReached = true;

						// read 1072 byte into rangeBuffer array
						if (reader.read(rangeBuffer) == -1) {
							System.out.println("EOF reached in " + count + ". iteration");
							isEOFReached = true;
						}

						if (!isEOFReached) {
							System.out.print("EOF not reached: " + reader.available() + " bytes still available in ");
							System.out.println(count + ". iteration");

							System.out.println("Creating a Pulse with id: " + ((packetIndex * 32) + pulseIndex));

							// Total of 256 Pulse Objects will be created.
							Pulse pulse = new Pulse(268);

							// iterate 268 times, acquire 268 integers as a result.
							// first four elements in the rangeBuffer (0, 1, 2, 3) as an integer,
							// second four bytes as an integer (4, 5, 6, 7) and so on...
							for (int rangeIndex = 0; rangeIndex < totalRangeIndex; rangeIndex++) {
								int iq = ByteIntUtil.byteArrayToInt(rangeBuffer, (rangeIndex * 4));

								// iq is taken as two different values, most significant 16 bit
								// as one value, least significant 16 as the other.
								short i = (short) ((iq & 0xFFFF0000) >> 16);
								short q = (short) (iq & (0x0000FFFF));

								pulse.getDataIPrevFFT()[rangeIndex] = i;
								pulse.getDataQPrevFFT()[rangeIndex] = q;

							}
							pulseList.add(pulse);

						}
					}

				}

			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			isEOFReached = true;

		} catch (IOException e) {
			e.printStackTrace();
			isEOFReached = true;
		}

		return pulseList.size();
	}

	/**
	 * 
	 * @param timeInSec
	 * @param pulseCount 128-256 ...2048
	 * @param rangeCount 402 menzilde 268
	 */
	public void doFFTProcessing(int timeInSec, int pulseCount, int rangeCount) {

		if (!loadData(parentRecordPath, timeInSec)) {
			return;
		}

		this.pulseCount = pulseCount;
		this.rangeCount = rangeCount;

		Complex data[] = new Complex[pulseCount];
		double[] window = FFTWindow.generateBlackManWindow(pulseCount);

		dataIAfterFFT = new float[rangeCount][pulseCount];
		dataQAfterFFT = new float[rangeCount][pulseCount];

		amplitudeListInDB = new double[rangeCount][pulseCount];
		amplitudeListInLinear = new double[rangeCount][pulseCount];

		for (int dopplerIndex = 0; dopplerIndex < data.length; dopplerIndex++) {
			data[dopplerIndex] = new Complex();
		}

		for (int rangeIndex = 0; rangeIndex < rangeCount; rangeIndex++) {
			for (int dopplerIndex = 0; dopplerIndex < pulseCount; dopplerIndex++) {
				if (dopplerIndex >= pulseList.size()) {
					break;
				}
				float i = pulseList.get(dopplerIndex).getDataIPrevFFT()[rangeIndex];

				double win = window[dopplerIndex];
				float q = pulseList.get(dopplerIndex).getDataQPrevFFT()[rangeIndex];
				data[dopplerIndex].setReal(i * win);
				data[dopplerIndex].setImag(q * win);
			}
			fft.setData(data);
			fft.transform();
			Complex result[] = fft.getTransformedDataAsComplex();
			for (int i = 0; i < result.length; i++) {

				dataIAfterFFT[rangeIndex][i] = (float) result[i].getReal();
				dataQAfterFFT[rangeIndex][i] = (float) result[i].getImag();
				double amplitudeInLinear = (float) (Math
						.sqrt((dataIAfterFFT[rangeIndex][i] * dataIAfterFFT[rangeIndex][i])
								+ (dataQAfterFFT[rangeIndex][i] * dataQAfterFFT[rangeIndex][i])));
				amplitudeListInLinear[rangeIndex][i] = amplitudeInLinear;

				double amplitudeInDB = 20f * Math.log10(amplitudeInLinear * 1d / MATCH_MULTIPLIER + 0.000000001);

				if (amplitudeInDB < -150)
					amplitudeInDB = -150;
				amplitudeListInDB[rangeIndex][i] = amplitudeInDB;
			}
		}
	}

	public double[][] getAmplitudeListInDB() {
		return amplitudeListInDB;
	}

	public int getRangeCount() {
		return rangeCount;
	}

	public void setRangeCount(int rangeCount) {
		this.rangeCount = rangeCount;
	}

	public int getPulseCount() {
		return pulseCount;
	}

	public String getParentRecordPath() {
		return parentRecordPath;
	}

	public void setParentRecordPath(String parentRecordPath) {
		this.parentRecordPath = parentRecordPath;
	}
}
