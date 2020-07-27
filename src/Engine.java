import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.Math;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Engine extends JFrame {
	public final int WIDTH = 1000;
	public final int HEIGHT = 1000;
	public final double NEARDIST = 0.1d;
	public final double FARDIST = 1000.0d;
	public final double FOV = 90.0d;
	public final double ASPECTRATIO = (double) HEIGHT / (double) WIDTH;
	public final double FOVRAD = 1.0d / Math.tan(FOV / 360.0d * Math.PI);
	public final double [] PROJMATENTRIES = {ASPECTRATIO*FOVRAD, 0.0d, 0.0d, 0.0d,
											0.0d, FOVRAD, 0.0d, 0.0d, 
											0.0d, 0.0d, FARDIST / (FARDIST-NEARDIST), 1.0d,
											0.0d, 0.0d, (-FARDIST*NEARDIST) / (FARDIST-NEARDIST), 0.0d};
	public final Matrix PROJMAT = new Matrix(4, 4, PROJMATENTRIES);
	
	JPanel display;
	KeyListener pressed;
	public double theta;
	public double theta2;
	public double [] cameraLoc;
	public double [] lightDir;
	public Color st = Color.black;
	public Mesh testObj; 
	public double translateForward;
	public ArrayList<Triangle> triArray;
	
	public Engine() {
		theta = 0.0d;
		theta2 = 0.0d;
		cameraLoc = new double[3];
		lightDir = new double[3];
		lightDir[2] = -1.0d;
		testObj = new Mesh();
		try {
			loadMesh("Non-Contact-Door-Opener-Key.obj");
		} catch (Exception e) {
			e.printStackTrace();
		}
		translateForward = 10.0d;
		pressed = new KeyPressed();

		display = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;
				g2.setColor(st);
				g2.fillRect(0, 0, getWidth(), getHeight());
				
				Matrix rotateX = new Matrix(4, 4);
				rotateX.entries[0] = 1.0d;
				rotateX.entries[5] = Math.cos(theta / 2.0d);
				rotateX.entries[6] = Math.sin(theta / 2.0d);
				rotateX.entries[9] = -Math.sin(theta / 2.0d);
				rotateX.entries[10] = Math.cos(theta / 2.0d);
				rotateX.entries[15] = 1.0d;
				
				Matrix rotateY = new Matrix(4, 4);
				rotateY.entries[0] = Math.cos(theta2);
				rotateY.entries[2] = Math.sin(theta2);
				rotateY.entries[5] = 1.0d;
				rotateY.entries[8] = -Math.sin(theta2);
				rotateY.entries[10] = Math.cos(theta2);
				rotateY.entries[15] = 1.0d;
				
				Matrix rotateZ = new Matrix(4, 4);
				rotateZ.entries[0] = Math.cos(theta);
				rotateZ.entries[1] = Math.sin(theta);
				rotateZ.entries[4] = -Math.sin(theta);
				rotateZ.entries[5] = Math.cos(theta);
				rotateZ.entries[10] = 1.0d;
				rotateZ.entries[15] = 1.0d;
				
				g2.setColor(Color.GREEN);
				
				triArray = new ArrayList<Triangle>();

				for(Triangle tri: testObj.triangles) {
					Triangle proj = new Triangle();
					
					for(int i = 0; i < 3; i++) {
						double [] coords = {tri.vertices[i].x, tri.vertices[i].y, tri.vertices[i].z, 1.0d};
						Matrix mat = new Matrix(1, 4, coords);
						double [] res = mat.multiply(rotateY).entries;
						Vertex v = new Vertex(res[0], res[1], res[2]);
						proj.insertVertex(i, v);
					}
					
					for(int i = 0; i < 3; i++) {
						double [] coords = {proj.vertices[i].x, proj.vertices[i].y, proj.vertices[i].z, 1.0d};
						Matrix mat = new Matrix(1, 4, coords);
						double [] res = mat.multiply(rotateX).entries;
						Vertex v = new Vertex(res[0], res[1], res[2]+translateForward);
						proj.insertVertex(i, v);
					}
					
					double [] lineA = {proj.vertices[1].x-proj.vertices[0].x, proj.vertices[1].y-proj.vertices[0].y, proj.vertices[1].z-proj.vertices[0].z};
					double [] lineB = {proj.vertices[2].x-proj.vertices[0].x, proj.vertices[2].y-proj.vertices[0].y, proj.vertices[2].z-proj.vertices[0].z};
					double [] normal = {lineA[1]*lineB[2] - lineA[2]*lineB[1], lineA[2]*lineB[0] - lineA[0]*lineB[2], lineA[0]*lineB[1] - lineA[1]*lineB[0]};
					normal = norm(normal);
					
					double shading = (normal[0]*lightDir[0]+normal[1]*lightDir[1]+normal[2]*lightDir[2])*128.0d+128.0d;
					if(shading < 0.0d)
						shading = 0.0d;
					else if(shading > 255.0d)
						shading = 255.0d;
					
					if(normal[0]*(proj.vertices[0].x-cameraLoc[0])+normal[1]*(proj.vertices[0].y-cameraLoc[1])+normal[2]*(proj.vertices[0].z-cameraLoc[2]) < 0) {
						proj.color = new Color((int) shading, (int) shading, (int) shading);
						for(int i = 0; i < 3; i++) {
							double [] coords = {proj.vertices[i].x, proj.vertices[i].y, proj.vertices[i].z, 1.0d};
							Matrix mat = new Matrix(1, 4, coords);
							double [] res = mat.multiply(PROJMAT).getEntries();
							if(res[3] == 0) 
								res[3] = 1;
							Vertex v = new Vertex(res[0]/res[3], res[1]/res[3], res[2] / res[3]);
							proj.insertVertex(i, v);
						}
						
						proj.vertices[0].x = (proj.vertices[0].x + 1.0d) * 0.5d * (double) getWidth(); proj.vertices[0].y = (proj.vertices[0].y + 1.0d) * 0.5d * (double) getHeight(); 
						proj.vertices[1].x = (proj.vertices[1].x + 1.0d) * 0.5d * (double) getWidth(); proj.vertices[1].y = (proj.vertices[1].y + 1.0d) * 0.5d * (double) getHeight();
						proj.vertices[2].x = (proj.vertices[2].x + 1.0d) * 0.5d * (double) getWidth(); proj.vertices[2].y = (proj.vertices[2].y + 1.0d) * 0.5d * (double) getHeight();					

						triArray.add(proj);
					}				
				}
				double [] zDists = new double[triArray.size()];	
				for(int i = 0; i < triArray.size(); i++) 
					zDists[i] = (triArray.get(i).vertices[0].z+triArray.get(i).vertices[1].z+triArray.get(i).vertices[2].z)/3.0d;
				sort(zDists);
				for(Triangle proj: triArray) {
					//display wireframe:
					
					
					Path2D path = new Path2D.Double();
					path.moveTo(proj.vertices[0].x, proj.vertices[0].y);
					path.lineTo(proj.vertices[1].x, proj.vertices[1].y);
					path.lineTo(proj.vertices[2].x, proj.vertices[2].y);
					path.closePath();
					g2.draw(path);
					
					/*
					g.setColor(proj.color);
					int [] x = {(int)proj.vertices[0].x, (int)proj.vertices[1].x, (int)proj.vertices[2].x};
					int[] y = {(int)proj.vertices[0].y, (int)proj.vertices[1].y, (int)proj.vertices[2].y};
					g.fillPolygon(x, y, 3);*/
					
				}
			}
		};
		display.setFocusable(true);
		display.addKeyListener(pressed);

		add(display);
    	setSize(WIDTH, HEIGHT);
    	setVisible(true);
	}
	
	public void loadMesh(String filename) throws Exception{
		BufferedReader fpIn = new BufferedReader(new FileReader("src\\"+filename));
		Scanner sc = new Scanner(fpIn.readLine());
		String str = fpIn.readLine();
		
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		
		while(str != null) {
			if(str.length() > 0 && str.charAt(0) == 'v') {
				String [] vals = str.split(" ");
				vertices.add(new Vertex(Double.parseDouble(vals[1]), Double.parseDouble(vals[2]), Double.parseDouble(vals[3])));
			}
			else if(str.length() > 0 && str.charAt(0) == 'f') {
				String [] vals = str.split(" ");
				
				
				int v1 = Integer.parseInt(vals[1].substring(0, vals[1].indexOf('/')));
				int v2 = Integer.parseInt(vals[2].substring(0, vals[2].indexOf('/')));
				int v3 = Integer.parseInt(vals[3].substring(0, vals[3].indexOf('/')));
				
				/*
				int v1 = Integer.parseInt(vals[1]);
				int v2 = Integer.parseInt(vals[2]);
				int v3 = Integer.parseInt(vals[3]);
				*/
				testObj.insertTriangle(new Triangle(vertices.get(v1-1), vertices.get(v2-1), vertices.get(v3-1)));
			}
			str = fpIn.readLine();
		}
	}
	
	public double[] norm(double[] arr) {
		double length = 0;
		for(int i = 0; i < arr.length; i++)
			length += arr[i]*arr[i];
		
		length = Math.sqrt(length);
		
		for(int i = 0; i < arr.length; i++)
			arr[i] = arr[i] / length;
		
		return arr;
	}
	
	public double [] sort(double[] zDists) {
		return sort(zDists, 0, zDists.length-1);
	}
	public double [] sort(double [] array, int start, int end) {
		int left = start;
		int right = end;
		int pivot = left;

		while(left != right) {
			if(array[left] < array[right]) {
				double place = array[left];
				array[left] = array[right];
				array[right] = place;
				pivot = (pivot == left) ? right : left;
				Triangle p = triArray.get(left);
				triArray.set(left,  triArray.get(right));
				triArray.set(right, p);
			}
			
			if(pivot == left)
				right--;
			else
				left++;
		}
		if(pivot != start)
			array = sort(array, start, pivot-1);	
		if(pivot != end)
			array = sort(array, pivot+1, end);	
		
		return array;		
	}

	private class KeyPressed implements KeyListener {
		public void keyTyped(KeyEvent e) {}

		public void keyPressed(KeyEvent e) {
			char c = (char) e.getKeyChar();
			if(c == 'a' || c == 'A') {
				theta2 += 0.1d;
				repaint();
			}
			else if(c == 'D' || c == 'd') {
				theta2 -= 0.1d;
				repaint();
			}
			else if(c == 'W' || c == 'w') {
				theta += 0.1d;
				repaint();
			}
			else if(c == 'S' || c == 's') {
				theta -= 0.1d;
				repaint();
			}
			else if(e.getKeyCode() == KeyEvent.VK_UP) {
				translateForward -= 3;
				repaint();
			}
			else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
				translateForward += 3;
				repaint();
			}
		}

		public void keyReleased(KeyEvent e) {}
	}
	
	
	public static void main(String[] args) throws Exception{
		new Engine();
	}
}