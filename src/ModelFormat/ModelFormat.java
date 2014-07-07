/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat;

import Geometry.Vec3;
import GuiComponents.MainGui;
import Images.PCX;
import OpenGL.OpenGL3DView;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author cjones
 */
public abstract class ModelFormat
{
	public static final Color VERTEX_COLOUR = new Color(0, 0, 255);
	public static final Color VERTEX_ONSEAM_COLOUR = new Color(0, 190, 255);
	public static final Color SELECTED_VERTEX_COLOUR = new Color(255, 0, 0);
	public static final Color SELECTED_VERTEX_ONSEAM_COLOUR = new Color(255, 100, 100);
	public Collection<Integer> selectedVertices = Collections.synchronizedList(new ArrayList<Integer>());
	public Collection<Integer> selectedFaces = Collections.synchronizedList(new ArrayList<Integer>());

	/**
	 * Return an array of strings for frame names.  The indices of the strings
	 * need to match up somehow to call up frames for drawing in OpenGL.
	 */
	public abstract String[] getFrameNames();

	/**
	 * Draw the model to OpenGL.
	 */
	public final void drawToOpenGL(int currentFrame, int mode)
	{
		float[][] uvData = this.getScalarUVCoordsArray();
		int[][][] triData = this.getUV_TriRelationArray();
		Vec3[] vertices = this.getVertices(currentFrame);

		Vec3 org = new Vec3(0f, 0f, 0f);

		GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing
		for (int triIndex = 0; triIndex < triData.length; triIndex++)
		{
			if (mode != MainGui.MODE_FACE || (mode == MainGui.MODE_FACE && !this.selectedFaces.contains(new Integer(triIndex))))
			{
				for (int vIndex = 0; vIndex < 3; vIndex++)
				{
					org = vertices[triData[triIndex][vIndex][0]]; //index 0 is the vertex index

					// Compute texture coordinates
					float s = uvData[triData[triIndex][vIndex][1]][0];
					float t = uvData[triData[triIndex][vIndex][1]][1];

					if (triData[triIndex][vIndex][2] == 0 && uvData[triData[triIndex][vIndex][1]][2] != 0)
					{
						s = s + 0.5f; // Backface
					}

					// Pass texture coordinates to OpenGL
					GL11.glTexCoord2f(s, t);

					// Normal vector
					//GL11.glNormal3f(-0.525731f, 0.000000f, 0.850651f); //glNormal3fv(anorms_table[pvert - > normalIndex]);

					GL11.glVertex3f(org.x, org.y, org.z);
				}
			}
		}
		GL11.glEnd(); //stop drawing

		if (mode == MainGui.MODE_FACE && !this.selectedFaces.isEmpty())
		{
			GL11.glColor3f(1.0f, 0.2f, 0.2f);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing

			Integer[] drawFaces = this.selectedFaces.toArray(new Integer[this.selectedFaces.size()]);
			for (int i = 0; i < drawFaces.length; i++)
			{
				int triIndex = drawFaces[i].intValue();
				for (int vIndex = 0; vIndex < 3; vIndex++)
				{
					org = vertices[triData[triIndex][vIndex][0]]; //index 0 is the vertex index

					GL11.glVertex3f(org.x, org.y, org.z);
				}

			}

			GL11.glEnd(); //stop drawing
		}
	}

		/**
	 * Draw the model to OpenGL.
	 */
	public final void drawEdgesToOpenGL(int currentFrame, int mode)
	{
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_LINE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glPolygonOffset(-1.0f, -0.1f);

		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

		GL11.glColor3f(0.5f, 0.5f, 0.5f);

		
		
		float[][] uvData = this.getScalarUVCoordsArray();
		int[][][] triData = this.getUV_TriRelationArray();
		Vec3[] vertices = this.getVertices(currentFrame);

		Vec3 org = new Vec3(0f, 0f, 0f);

		GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing
		for (int triIndex = 0; triIndex < triData.length; triIndex++)
		{
			if (mode != MainGui.MODE_FACE || (mode == MainGui.MODE_FACE && !this.selectedFaces.contains(new Integer(triIndex))))
			{
				for (int vIndex = 0; vIndex < 3; vIndex++)
				{
					org = vertices[triData[triIndex][vIndex][0]]; //index 0 is the vertex index

					// Compute texture coordinates
					float s = uvData[triData[triIndex][vIndex][1]][0];
					float t = uvData[triData[triIndex][vIndex][1]][1];

					if (triData[triIndex][vIndex][2] == 0 && uvData[triData[triIndex][vIndex][1]][2] != 0)
					{
						s = s + 0.5f; // Backface
					}

					// Pass texture coordinates to OpenGL
					GL11.glTexCoord2f(s, t);

					// Normal vector
					//GL11.glNormal3f(-0.525731f, 0.000000f, 0.850651f); //glNormal3fv(anorms_table[pvert - > normalIndex]);

					GL11.glVertex3f(org.x, org.y, org.z);
				}
			}
		}
		GL11.glEnd(); //stop drawing

		if (mode == MainGui.MODE_FACE && !this.selectedFaces.isEmpty())
		{
			GL11.glColor3f(1.0f, 0.2f, 0.2f);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing

			Integer[] drawFaces = this.selectedFaces.toArray(new Integer[this.selectedFaces.size()]);
			for (int i = 0; i < drawFaces.length; i++)
			{
				int triIndex = drawFaces[i].intValue();
				for (int vIndex = 0; vIndex < 3; vIndex++)
				{
					org = vertices[triData[triIndex][vIndex][0]]; //index 0 is the vertex index

					GL11.glVertex3f(org.x, org.y, org.z);
				}

			}

			GL11.glEnd(); //stop drawing
		}
		
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_LINE);

	}

	
	/**
	 * Draw the vertices to OpenGL.
	 */
	public final void drawVerticesToOpenGL(int currentFrame, float vSize)
	{
		//Setup proper mode for vertex drawing
		GL11.glPointSize(vSize);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glEnable(GL11.GL_POLYGON_OFFSET_POINT);
		GL11.glPolygonOffset(1.0f, -0.1f);
		
		boolean isVertexSelected = false;

		float[][] uvData = this.getScalarUVCoordsArray();
		int[][][] triData = this.getUV_TriRelationArray();
		Vec3[] vertices = this.getVertices(currentFrame);

		Vec3 org = new Vec3(0f, 0f, 0f), n = new Vec3(0f, 0f, 0f);
		GL11.glBegin(GL11.GL_POINTS); // Start Drawing
		OpenGL3DView.setDefaultVertexDisplayColour();
		for (int triIndex = 0; triIndex < triData.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				org = vertices[triData[triIndex][vIndex][0]]; //index 0 is the vertex index

				if (!selectedVertices.isEmpty())
				{
					Iterator<Integer> it = selectedVertices.iterator();
					while (it.hasNext())
					{
						Integer selIndex = it.next();
						if (selIndex.intValue() == triData[triIndex][vIndex][0])
						{
							isVertexSelected = true;
							break;
						}
					}
				}

				if (isVertexSelected)
					OpenGL3DView.setSelectedVertexDisplayColour();
				else if (uvData[triData[triIndex][vIndex][0]][2] != 0) //onseam
					OpenGL3DView.setDefaultVertexOnseamDisplayColour();
				else
					OpenGL3DView.setDefaultVertexDisplayColour();

				if (isVertexSelected)
					OpenGL3DView.setSelectedVertexDisplayColour();

				GL11.glVertex3f(org.x, org.y, org.z);

				if (isVertexSelected)
				{
					isVertexSelected = false;
					OpenGL3DView.setDefaultVertexDisplayColour();
				}
			}
		}
		GL11.glEnd(); //stop drawing
		
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
		GL11.glDisable(GL11.GL_POLYGON_OFFSET_POINT);
	}

	//Integer[] selectedVertices = model.selectedVertices.toArray(new Integer[model.selectedVertices.size()]);
	/**
	 * Upload texture data to OpenGL and get the texID for OpenGL texture bind call
	 */
	public abstract int UploadTextureToOpenGL(int skinIndex);

	//TODO: should eventually load any kind of image format.
	public abstract void setTexture(PCX pcxImage);

	/*
	Generic access to texture data
	 */
	public abstract boolean getHasTexture();

	public abstract int[] getTextureData();

	public abstract int getTextureWidth();

	public abstract int getTextureHeight();

	public abstract void exportMDL(String _filename);

	public abstract int getFrameIndexFromNameIndex(int selectedIndex);

	/**
	 * A general accessor method for uv coords.
	 */
	public abstract float[][] getScalarUVCoordsArray();

	public abstract int[][][] getUV_TriRelationArray();

	public abstract Vec3[] getVertices(int frame);

	/*public abstract float[] getUVCoordData(int index);
	public abstract int[][] getTriangleData(int triIndex);*/
	/**
	 * Provides a universal way to update texture coordinates
	 */
	public abstract void moveUVCoordBy(int index, float deltaX, float deltaY, int zoom);

	private float scaleRDistByDist(float dist)
	{
		float val = dist / OpenGL3DView.MAX_ZOOM;
		if (val > 2f)
			val = 2f;
		else if (val < 0.1f)
			val = 0.1f;
		return val;
	}

	public final boolean isVertexCloseToLine(Vec3 org, Vec3 dir, float maxRadiusDistance, int currentFrame)
	{
		dir = dir.normalize(); //just do this incase it's not normalized.
		maxRadiusDistance = (float) Math.pow(maxRadiusDistance, 2); //so we don't need to sqrt the dist value each time

		Vec3[] vertices = this.getVertices(currentFrame);
		for (int vIndex = 0; vIndex < vertices.length; vIndex++)
		{
			Vec3 vOrigin = vertices[vIndex];
			Vec3 vec = vOrigin.subtract(org);
			float dist = (dir.scale(dir.dot(vec))).length();
			float rDist = (vec.dot(vec)) - (float) Math.pow((vec.dot(dir)), 2);

			if (rDist < maxRadiusDistance * scaleRDistByDist(dist))
				return true;
		}
		return false;
	}

	public final int getVertexClosestToLine(Vec3 org, Vec3 dir, float maxRadiusDistance, int currentFrame)
	{
		int nearestVIndex = -1;
		dir = dir.normalize(); //just do this incase it's not normalized.
		maxRadiusDistance = (float) Math.pow(maxRadiusDistance, 2); //so we don't need to sqrt the dist value each time

		Vec3[] vertices = this.getVertices(currentFrame);
		float oldRDist = 9999999999f;
		float oldDist = 9999999999f;
		for (int vIndex = 0; vIndex < vertices.length; vIndex++)
		{
			Vec3 vOrigin = vertices[vIndex];
			Vec3 vec = vOrigin.subtract(org);
			float dist = (dir.scale(dir.dot(vec))).length();
			float rDist = (vec.dot(vec)) - (float) Math.pow((vec.dot(dir)), 2);

			if (rDist < maxRadiusDistance * scaleRDistByDist(dist))
			{
				if (dist < oldDist || rDist < oldRDist)
				{
					nearestVIndex = vIndex;
					oldRDist = rDist;
					oldDist = dist;
				}
			}
		}
		return nearestVIndex;
	}
}
