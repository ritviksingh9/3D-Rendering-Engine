import java.awt.Color;

public class Triangle {
	Vertex [] vertices;
	Color color;
	
	public Triangle() {
		this.vertices = new Vertex[]{null, null, null};
		color = Color.white;
	}
	public Triangle(Vertex v1, Vertex v2, Vertex v3) {
		this.vertices = new Vertex[]{v1, v2, v3};
		color = Color.white;
	}
	public Triangle(Vertex[] vertices) {
		this.vertices = vertices;
		color = Color.white;
	}
	
	protected void insertVertex(int index, Vertex v) {
		if(index >= 0 && index < 3)
			this.vertices[index] = new Vertex(v.x, v.y, v.z);
	}
}
