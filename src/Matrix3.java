
public class Matrix3 {
	double [] entries;
	
	public Matrix3() { this.entries = new double[9]; }
	public Matrix3(double [] entries) {
		this.entries = entries;
	}
	
	protected double[] getEntries() { return entries; }
	protected void setEntries(double [] entries) { this.entries = entries; }
	protected Matrix3 multiply(Matrix3 mult) {
		//Current Matrix * mult
		Matrix3 ans = new Matrix3();
		
		for(int row = 0; row < 3; row++) {
			for(int col = 0; col < 3; col++) 
				ans.entries[row*3+col] = this.entries[row*3]*mult.entries[col] + this.entries[row*3+1]*mult.entries[col+3] + this.entries[row*3+2]*mult.entries[col+6];  
		}
		
		return ans;
	}
	
}
