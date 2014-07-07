/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.WavefrontOBJ;

import Geometry.Vec3;
import Images.PCX;
import ModelFormat.QuakeMDL.MDL;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import ModelFormat.ModelFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author cjones
 */
public class OBJ extends ModelFormat
{
	private Vec3[] textureCoordinates;
	private int[][][] triangles;
	private ArrayList<OBJFrame> frames = new ArrayList();
	private PCX texture = null;

	public OBJ(File file)
	{
		try
		{
			frames.add(this.parseOBJ(file));
		}
		catch (FileNotFoundException ex)
		{
			System.out.println("Couldn't parse file.");
			throw new RuntimeException(ex.getMessage());
		}
	}

	private OBJFrame parseOBJ(File file) throws FileNotFoundException
	{
		ArrayList<Vec3> verts = new ArrayList();
		ArrayList<Vec3> texCoords = new ArrayList();
		ArrayList<Vec3> vertNorms = new ArrayList();
		ArrayList<int[][]> tris = new ArrayList();


		Scanner scn = new Scanner(file), scnInner, scnInnerFace;
		String buffer, bufferInner;

		while (scn.hasNextLine())
		{
			buffer = scn.nextLine();
			if (buffer.startsWith("#")) //comment, ignore.
				continue;

			if (buffer.startsWith("v")) //some type of vertex def here
			{
				scnInner = new Scanner(buffer);
				while (scnInner.hasNext())
				{
					bufferInner = scnInner.next();
					if (bufferInner.equals("v")) //vertex def
					{
						verts.add(new Vec3(scnInner.nextFloat(),
										   scnInner.nextFloat(),
										   scnInner.nextFloat()));
						//those 3 floats had better be there!
					}
					else if (bufferInner.equals("vt")) //vertex tex coords
					{
						float u, v, w = 0.0f;
						u = scnInner.nextFloat(); // u/s
						v = 1 - scnInner.nextFloat(); // v/t
						if (scnInner.hasNext()) //w component is sometimes omitted
							w = scnInner.nextFloat(); // w (will be ignored by mdl)
						texCoords.add(new Vec3(u, v, w));
						//again, those 3 floats had bette be there!
					}
					else if (bufferInner.equals("vn"))
					{
						vertNorms.add(new Vec3(scnInner.nextFloat(),
											   scnInner.nextFloat(),
											   scnInner.nextFloat()));
						//once more, those 3 floats had bette be there!
					}
				}
			}
			else if (buffer.startsWith("f"))
			{
				scnInner = new Scanner(buffer);
				scnInner.next(); //throw away f
				int[][] triData = new int[3][3];
				//tris have 3 blocks of 3 indices (each block is vertIndex/texCoordIndex/normalIndex)
				int prevVT = 0;
				for (int blockIndex = 2; blockIndex >= 0; blockIndex--) //always 3 blocks for a tri!
				{
					scnInnerFace = new Scanner(scnInner.next()); //get the first block of face defs
					scnInnerFace.useDelimiter("/"); //blocks are seperated with slashes
					int innerFaceCounter = 0;
					while (scnInnerFace.hasNext() && innerFaceCounter < 3)
					{
						String temp = scnInnerFace.next();
						int tempIndex;
						if (temp.isEmpty())
							triData[blockIndex][innerFaceCounter] = prevVT; //NO UV info??? I was so tempted to use Math.random()...
						else
						{
							triData[blockIndex][innerFaceCounter] = Integer.parseInt(temp) - 1; //-1 because friggin obj is not zero based.
							prevVT = triData[blockIndex][innerFaceCounter];
						}
						//first element of the block is the vertex index (the other two are tex coords and normals)
						innerFaceCounter++;
					}
				}
				tris.add(triData);
			}

		}
		//System.out.println("Loaded " + verts.size() + " vertices");
		//System.out.println("Loaded " + texCoords.size() + " vertex texture coords");
		//System.out.println("Loaded " + vertNorms.size() + " vertex normals");
		//System.out.println("Loaded " + tris.size() + " tris");

		if (this.frames.isEmpty()) //haven't imported any frames.
		{
			System.out.println("Loading first frame triangle data (" + tris.size() + " tris).");
			triangles = new int[tris.size()][3][3];
			for (int i = 0; i < triangles.length; i++)
			{
				for (int j = 0; j < 3; j++)
					triangles[i][j] = tris.get(i)[j];
			}
			textureCoordinates = texCoords.toArray(new Vec3[texCoords.size()]);
		}

		Vec3 mins = new Vec3(verts.get(0)), maxs = new Vec3(verts.get(0));
		Iterator<Vec3> it = verts.iterator();
		while (it.hasNext())
		{
			Vec3 vec = it.next();

			if (vec.x < mins.x)
				mins.x = vec.x;
			if (vec.y < mins.y)
				mins.y = vec.y;
			if (vec.z < mins.z)
				mins.z = vec.z;

			if (vec.x > maxs.x)
				maxs.x = vec.x;
			if (vec.y > maxs.y)
				maxs.y = vec.y;
			if (vec.z > maxs.z)
				maxs.z = vec.z;
		}

		return new OBJFrame("frame", verts.toArray(new Vec3[verts.size()]), vertNorms.toArray(new Vec3[vertNorms.size()]), mins, maxs);
	}

/*	@Override
	public void drawToOpenGL(int currentFrame)
	{
		OBJFrame frame = this.frames.get(currentFrame);

		Vec3 org = new Vec3(0f, 0f, 0f), n = new Vec3(0f, 0f, 0f);
		GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing
		for (int triIndex = 0; triIndex < triangles.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				org = this.frames.get(currentFrame).getVertices()[this.triangles[triIndex][vIndex][0]]; //index 0 is the vertex index

				// Compute texture coordinates
				float s = this.textureCoordinates[this.triangles[triIndex][vIndex][1]].x; //s is stored in x component
				float t = this.textureCoordinates[this.triangles[triIndex][vIndex][1]].y; //t is stored in y component
				//we ignore w component in z

				// Pass texture coordinates to OpenGL
				GL11.glTexCoord2f(s, t);

				// Normal vector
				n = this.frames.get(currentFrame).getVertexNormals()[this.triangles[triIndex][vIndex][2]]; //index 2 is the vertex normal
				n = n.normalize(); //is this necessary?
				GL11.glNormal3f(n.x, n.y, n.z);

				GL11.glVertex3f(org.x, org.y, org.z);
			}
		}
		GL11.glEnd(); //stop drawing
	}
*/

	public Vec3[] getSimpleTexCoords()
	{
		return this.textureCoordinates;
	}

	@Override
	public void setTexture(PCX pcxImage)
	{
		//int[] pixels = pcxImage.getPixels();
		texture = pcxImage;
	}

	@Override
	public boolean getHasTexture()
	{
		return texture != null;
	}

	@Override
	public int[] getTextureData()
	{
		return texture.getPixels();
	}

	@Override
	public int getTextureWidth()
	{
		return texture.getWidth();
	}

	@Override
	public int getTextureHeight()
	{
		return texture.getHeight();
	}

	/*@Override
	public void drawVerticesToOpenGL(int currentFrame, ArrayList<Integer> selectedVertices)
	{
		boolean isVertexSelected = false;
		OBJFrame frame = this.frames.get(currentFrame);

		Vec3 org = new Vec3(0f, 0f, 0f), n = new Vec3(0f, 0f, 0f);
		GL11.glPointSize(4.0f);
		GL11.glBegin(GL11.GL_POINTS); // Start Drawing
		OpenGL3DView.setDefaultVertexDisplayColour();
		for (int triIndex = 0; triIndex < triangles.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				org = frame.getVertices()[this.triangles[triIndex][vIndex][0]]; //index 0 is the vertex index

				if (!selectedVertices.isEmpty())
				{
					Iterator<Integer> it = selectedVertices.iterator();
					while (it.hasNext())
					{
						Integer selIndex = it.next();
						if (selIndex.intValue() == this.triangles[triIndex][vIndex][0])
						{
							isVertexSelected = true;
							break;
						}
					}
				}

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
	}*/

	@Override
	public int UploadTextureToOpenGL(int skinIndex)
	{
		int texID;

		if (texture == null)
			return 0;

		int[] skinData = this.texture.getPixels();
		byte[] pixels = new byte[this.texture.getWidth() * this.texture.getHeight() * 3];

		/* Convert indexed 8 bits texture to RGB from the colourmap */
		for (int i = 0; i < skinData.length; i++)
		{
			pixels[(i * 3) + 0] = (byte) MDL.colourMap[skinData[i]][0];
			pixels[(i * 3) + 1] = (byte) MDL.colourMap[skinData[i]][1];
			pixels[(i * 3) + 2] = (byte) MDL.colourMap[skinData[i]][2];
		}

		ByteBuffer pixelsBuffer = BufferUtils.createByteBuffer(pixels.length);
		pixelsBuffer = pixelsBuffer.put(pixels);
		pixelsBuffer.rewind();
		texID = GL11.glGenTextures(); //generate an id for this open gl texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID); //tell opengl we're working with texID texture.

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 3, this.texture.getWidth(), this.texture.getHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixelsBuffer);

		return texID;
	}

	@Override
	public void exportMDL(String _filename)
	{
		new MDL(this).exportMDL(_filename);
	}

	public int[][][] getTris()
	{
		return triangles;
	}

	public OBJFrame getFrame(int frameNum)
	{
		return frames.get(frameNum);
	}

	public Vec3[] getTextureCoordinates()
	{
		return textureCoordinates;
	}

	public void addFrames(File[] allFiles) throws FileNotFoundException
	{
		for (int i = 0; i < allFiles.length; i++)
		{
			frames.add(this.parseOBJ(allFiles[i]));
		}
	}

	@Override
	public String[] getFrameNames()
	{
		String[] frameNames = new String[this.frames.size()];

		for (int i = 0; i < frameNames.length; i++)
		{
			frameNames[i] = frames.get(i).getName();
		}

		return frameNames;
	}

	@Override
	public float[][] getScalarUVCoordsArray()
	{
		float[][] UVArray = new float[this.textureCoordinates.length][3];

		for (int i = 0; i < UVArray.length; i++)
		{
			UVArray[i][0] = this.textureCoordinates[i].x;
			UVArray[i][1] = this.textureCoordinates[i].y;
			UVArray[i][2] = 0.0f; //OBJ verts are NEVER on seam.
		}

		return UVArray;
	}

	@Override
	public int[][][] getUV_TriRelationArray()
	{
		int[][][] triArray = new int[this.triangles.length][3][3];

		for (int i = 0; i < triArray.length; i++)
		{
			for (int j = 0; j < 3; j++) //MDL has 1:1 relationship between verts and skin verts.
			{
				triArray[i][j][0] = triangles[i][j][0]; //vertex index is in index 0
				triArray[i][j][1] = triangles[i][j][1]; //uv info is in index 1
				triArray[i][j][2] = 1; //OBJ tris are ALWAYS front facing
			}
		}

		return triArray;
	}

	@Override
	public int getFrameIndexFromNameIndex(int selectedIndex)
	{
		return selectedIndex;
	}

	public int getNumFrames()
	{
		return this.frames.size();
	}

	@Override
	public void moveUVCoordBy(int index, float x, float y, int zoom)
	{
		float deltaX = x / (float) zoom / getTextureWidth();
		float deltaY = y / (float) zoom / getTextureHeight();
		textureCoordinates[index] = textureCoordinates[index].add(new Vec3(deltaX, deltaY, 0));
	}


	@Override
	public Vec3[] getVertices(int frame)
	{
		return this.frames.get(frame).getVertices();
	}
}
