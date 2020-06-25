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
	private ArrayList<Pulse> pulseList = new ArrayList<Pulse>();

	private FFTWindow fftWindow;

	private double[][] amplitudeListInDB;
	private double[][] amplitudeListInLinear;

	private float dataIAfterFFT[][];
	private float dataQAfterFFT[][];

	static FourierTransform fft = new FourierTransform();
	static double MATCH_MULTIPLIER = Math.pow(2, 15);

	private int pulseCount;
	private int rangeCount;

	private String parentRecordPath = "";

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
		boolean isEOFReached = false;
		try {
			pulseList = new ArrayList<Pulse>();

			FileInputStream fileIn = new FileInputStream(fileName);

			BufferedInputStream reader = new BufferedInputStream(fileIn);

			byte rangeBuffer[] = new byte[268 * 4];
			byte header[] = new byte[12];
			int totalRangeIndex = 268;

			byte cmdIdSize[] = new byte[8];
			System.out.println("Reading Started");

			while (!isEOFReached) {
				for (int packetIndex = 0; packetIndex < 8; packetIndex++) {
					reader.read(cmdIdSize);
					for (int pulseIndex = 0; pulseIndex < 32; pulseIndex++) {

						if (reader.read(header) == -1)
							isEOFReached = true;
						short range = (short) ((ByteIntUtil.byteArrayToIntNew(header, 8) & (0x00003FFF)));
						int packetId = ByteIntUtil.byteArrayToInt(header, 4);

						if (reader.read(rangeBuffer) == -1)
							isEOFReached = true;
						
						
						if (!isEOFReached) {
							
//							System.out.println("PulseDataList.importRKADataBuf(): header: "
//									+ ByteIntUtil.byteArrayToHexNBytes(header, 4) + " range: " + range + " Packet Id:"
//									+ packetId + " pulse index: " + pulseIndex);
							
							Pulse pulse = new Pulse(268);
							for (int rangeIndex = 0; rangeIndex < totalRangeIndex; rangeIndex++) {
								
								int iq = ByteIntUtil.byteArrayToInt(rangeBuffer, (rangeIndex * 4));
								
								// Bitmask
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
			System.out.println("Reading Finished");
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
		double[] window = fftWindow.generateBlackManWindow(pulseCount);

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

	public void setFFTWindow(FFTWindow fftWindow) {
		this.fftWindow = fftWindow;
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
