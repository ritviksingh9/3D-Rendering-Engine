import java.util.ArrayList;

public class Mesh {
	ArrayList <Triangle> triangles;
	public Mesh() {
		triangles = new ArrayList <Triangle>();
	}
	protected void insertTriangle(Triangle insert) {
		this.triangles.add(insert);
	}
}
