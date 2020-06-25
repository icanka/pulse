package arm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import flanagan.complex.Complex;
import flanagan.math.FourierTransform;
import utils.ByteIntUtil;

public class PulseDataStreamTest {

	private String parentRecordPath = ".";
	private ArrayList<Pulse> pulseList = new ArrayList<Pulse>();

	public static void main(String... args) {
		PulseDataStreamTest instance = new PulseDataStreamTest();
		instance.loadData(false);
	}

	public boolean loadData(boolean isRecursive) {
		File files[] = FileUtils.listFiles(new File(parentRecordPath), null, isRecursive).toArray(new File[0]);
		// printFilesInArray(files);
		// writeSomeBinaryData("./index.jpg");
		importRKADDataBuf("./index.jpg");

		return false;
	}

	public int importRKADDataBuf(String fileName) {
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
			label: while (!isEOFReached) {

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
						}

						System.out.println("Creating a Pulse with id: " + ((packetIndex * 32) + pulseIndex));

						// Total of 256 Pulse Objects will be created.
						Pulse pulse = new Pulse(268);

						// iterate 268 times, acquire 268 integers as a result.
						// first four elements in the rangeBuffer (0, 1, 2, 3) as an integer,
						// second four bytes as an integer (4, 5, 6, 7) and so on...
						for (int rangeIndex = 0; rangeIndex < totalRangeIndex; rangeIndex++) {
							//TODO fill me in
							
							int iq = ByteIntUtil.byteArrayToInt(rangeBuffer, (rangeIndex * 4));
							
							// iq is taken as two different values, most significant 16 bit 
							// as one value, least significant 16 as the other.
							short i = (short) ((iq & 0xFFFF0000) >> 16);
							short q = (short) (iq & (0x0000FFFF));
							
							pulse.getDataIPrevFFT()[rangeIndex * 4] = i;
							
						}

						// mock values
						short range = 0;
						int packetId = 0;

						System.out.println("-----------------------------------------------------------");
					}

				}

			}

		} catch (Exception e) {
		}
		
		
		
		
		

		return 0;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private static void writeSomeBinaryData(String inputFile) {
		try {
			// create a writer
			FileOutputStream fos = new FileOutputStream(new File("output.dat"));
			BufferedOutputStream writer = new BufferedOutputStream(fos);

			FileInputStream fis = new FileInputStream(inputFile);
			BufferedInputStream reader = new BufferedInputStream(fis);

			byte byteArr[] = new byte[8];

			while (reader.read(byteArr) != -1) {
				System.out.println(reader.available());
				writer.write(byteArr);
			}
			reader.close();
			writer.close();

		} catch (Exception e) {
		}
	}

	
	
	
	
	
	private static void printFilesInArray(File files[]) {
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getName());
		}

	}

}
