
public class Matrix {
	double [] entries;
	int rows;
	int cols;
	
	public Matrix(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		entries = new double[rows*cols];
	}
	public Matrix(int rows, int cols, double [] entries) {
		this.rows = rows;
		this.cols = cols;
		this.entries = entries;
		assert entries.length == rows*cols : "MATRIX DIMENTIONS DO NOT MATCH UP!";
	}
	
	protected double[] getEntries() { return entries; }
	protected void setEntries(double [] entries) { this.entries = entries; }
	
	protected Matrix multiply(Matrix mult) {
		//Current Matrix * mult
		assert this.cols == mult.rows: "MATRIX DIMENTIONS DO NOT MATCH UP!";
		Matrix ans = new Matrix(this.rows, mult.cols);
		
		for(int i = 0; i < this.rows; i++) {
			for(int j = 0; j < mult.cols; j++) {
				for(int k = 0; k < this.cols; k++)
					ans.entries[i*mult.cols+j] += this.entries[i*this.cols+k]*mult.entries[j+mult.cols*k];
			}
		}
		
		return ans;		
	}
}
