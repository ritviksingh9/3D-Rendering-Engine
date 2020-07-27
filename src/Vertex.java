public class Vertex {
	double x;
	double y;
	double z;
	
	public Vertex() {}
	public Vertex(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	protected double getX() { return x;	}
	protected double getY() { return y;	}
	protected double getZ() { return z;	}
	
	protected void setX(double x) { this.x = x; }
	protected void setY(double y) { this.y = y; }
	protected void setZ(double z) { this.z = z;}
}
