package utils;

public class ByteIntUtil {

	/**
	 * This method returns an int value from the given byte array. It does this by
	 * concatenating 4 bytes from the array starting from the given index value.
	 * <p>
	 * For example:
	 * <p>
	 * { (byte)0xF0, (byte)0xFF, (byte)0x00, (byte)0xF0}
	 * <p>
	 * array will be concatenated into 0xF0FF00F0, given an index 0.
	 *
	 * @param byteArray an byte array in which we will acquire an int
	 * @param index     the index value from which to start acquire 4 bytes.
	 * @return the int concatenated from the bytes in the given array
	 * @see utils.ByteIntUtil#bytesToInt(byte[] bytes)
	 */

	public static int byteArrayToInt(byte[] byteArray, int index) {
		byte buffer[] = new byte[4];

		// Load 4 bytes to buffer starting from the index.
		for (int i = 0; i < 4; i++) {
			buffer[i] = byteArray[index + i];
		}

		return bytesToInt(buffer);
	}

	private static int bytesToInt(byte[] bytes) {
		int concatenated = 0;
		for (int i = 1; i <= bytes.length; i++) {
			concatenated = (((bytes[i - 1] & 0xFF) << ((bytes.length - i) * 8))) | concatenated;
		}
		return concatenated;
	}

	
	
	public static void test() {
		byte x[] = {
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,    // index = 0   ---> 0xFFFFFFFF
				(byte)0xF0, (byte)0x00, (byte)0x00, (byte)0x00,    // index = 4   ---> 0xF0000000
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,    // index = 8   ---> 0xFFFFFFFF
				(byte)0xF0, (byte)0x00, (byte)0x00, (byte)0x00,    // index = 12  ---> 0xF0000000
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,    // index = 16  ---> 0xFFFFFFFF
				(byte)0xF0, (byte)0x00, (byte)0x00, (byte)0x00,    // index = 20  ---> 0xF0000000
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,    // index = 24  ---> 0xFFFFFFFF
				(byte)0xF0, (byte)0x00, (byte)0x00, (byte)0x00,    // index = 28  ---> 0xF0000000
				(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,    // index = 32  ---> 0xFFFFFFFF
				(byte)0xF0, (byte)0x00, (byte)0x00, (byte)0x00,    // index = 36  ---> 0xF0000000
				} ;
		
		int concat = 0;
		
		concat = byteArrayToInt(x, 20);
		if( concat != 0xF0000000) {throw new Error();}
		
		concat = byteArrayToInt(x, 8);
		if( concat != 0xFFFFFFFF) {throw new Error();}
		
		System.out.println("binary: "+Integer.toBinaryString(concat));

	}

}
