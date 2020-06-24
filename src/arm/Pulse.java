package arm;

public class Pulse {
	float dataIPrevFFT[];
	float dataQPrevFFT[];
	
	public Pulse(int rangeCount) {
		this.dataIPrevFFT = new float[rangeCount];
		this.dataQPrevFFT = new float[rangeCount];
	}
	
	public float[] getDataIPrevFFT() {
		return this.dataIPrevFFT;
	}
	
	public float[] getDataQPrevFFT() {
		return this.dataQPrevFFT;
	}
}
