/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.QuakeMDL;

import Geometry.Vec3;
import Images.PCX;
import ModelFormat.WavefrontOBJ.OBJ;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import Utility.BinaryFileReader;
import ModelFormat.ModelFormat;
import javax.swing.JOptionPane;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author cjones
 */
public class MDL extends ModelFormat
{
	public static final int MDL_HEADER_SIZE = 84; //size in bytes of the header
	public static int[][] colourMap;
	private MDLHeader header;
	private MDLSkin[] skins;
	private MDLTexCoord[] texCoords;
	private MDLTriangle[] triangles;
	private MDLFrame[] frames;

	//<editor-fold defaultstate="collapsed" desc="Constructor from a filename">
	public MDL(String _filename)
	{
		BinaryFileReader data = new BinaryFileReader(_filename);
		header = new MDLHeader(data.nextInt(),
							   data.nextInt(),
							   data.nextFloatVec(),
							   data.nextFloatVec(),
							   data.nextFloat(),
							   data.nextFloatVec(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextInt(),
							   data.nextFloat());
		//header is 84 bytes long

		int group, nb;
		float[] time;
		skins = new MDLSkin[header.getNumSkins()]; //create an array of skins based on how many skins there are.
		int[] skinData;
		for (int i = 0; i < skins.length; i++) //fill the skin data in
		{
			skinData = new int[header.getSkinWidth() * header.getSkinHeight()];
			group = data.nextInt();
			if (group == 1) //grouped frame, extra bytes here for some stuff
			{
				nb = data.nextInt();
				time = new float[nb];
			}

			for (int j = 0; j < (header.getSkinWidth() * header.getSkinHeight());
				 j++)
			{
				skinData[j] = data.nextUnsignedByte();
			}

			//skins[i] = new MDLSkin(group, nb, time, skinData);
			skins[i] = new MDLSkin(group, skinData);
		}

		//fread (mdl->texcoords, sizeof (struct mdl_texcoord_t),  mdl->header.num_verts, fp);
		texCoords = new MDLTexCoord[header.getNumVerts()];
		for (int i = 0; i < texCoords.length; i++)
		{
			texCoords[i] = new MDLTexCoord(data.nextInt(), data.nextInt(), data.nextInt());
			//System.out.println("texcoords: " + texCoords[i]);
		}

		//fread (mdl->triangles, sizeof (struct mdl_triangle_t), mdl->header.num_tris, fp);
		triangles = new MDLTriangle[header.getNumTris()];
		for (int i = 0; i < triangles.length; i++)
			triangles[i] = new MDLTriangle(data.nextInt(), data.nextIntVec());

		MDLFrameElement frame;
		int type;
		MDLVertex mins, maxs, verts[];
		String name;
		frames = new MDLFrame[header.getNumFrames()];
		for (int i = 0; i < frames.length; i++)
		{
			//System.out.println("Start index: " + index);
			type = data.nextInt(); //fread (&mdl->frames[i].type, sizeof (long), 1, fp);
			mins = new MDLVertex(data.nextUnsignedByteVec(), data.nextUnsignedByte()); //fread (&mdl->frames[i].frame.bboxmin, sizeof (struct mdl_vertex_t), 1, fp);
			maxs = new MDLVertex(data.nextUnsignedByteVec(), data.nextUnsignedByte()); //fread (&mdl->frames[i].frame.bboxmax, sizeof (struct mdl_vertex_t), 1, fp);
			name = data.nextString(16); //fread (mdl->frames[i].frame.name, sizeof (char), 16, fp);

			//fread (mdl->frames[i].frame.verts, sizeof (struct mdl_vertex_t), mdl->header.num_verts, fp);
			verts = new MDLVertex[header.getNumVerts()];
			for (int j = 0; j < verts.length; j++)
				verts[j] = new MDLVertex(data.nextUnsignedByteVec(), data.nextUnsignedByte());

			frame = new MDLFrameElement(mins, maxs, name, verts);
			frames[i] = new MDLFrame(type, frame);
			//System.out.println(frames[i]);

			//System.out.println("End index: " + index);
		}
	}
	//</editor-fold>

	//<editor-fold defaultstate="collapsed" desc="Private class used in OBJ -> MDL conversion">
	private class OBJ2MDLVert
	{
		public int OBJVertIndex;
		public int OBJTextIndex;

		public OBJ2MDLVert(int OBJVertIndex, int OBJTextIndex)
		{
			this.OBJVertIndex = OBJVertIndex;
			this.OBJTextIndex = OBJTextIndex;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final OBJ2MDLVert other = (OBJ2MDLVert) obj;
			if (this.OBJVertIndex != other.OBJVertIndex)
				return false;
			//if (this.OBJTextIndex != other.OBJTextIndex)
			//	return false;
			return true;
		}

		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 37 * hash + this.OBJVertIndex;
			return hash;
		}

		@Override
		public String toString()
		{
			return String.format("v: %d, t: %d", OBJVertIndex, OBJTextIndex);
		}
	}
	//</editor-fold>

	public MDL(OBJ obj)
	{
		int[][][] objTris = obj.getTris(); //for convenience
		header = new MDLHeader(); //blank header, we'll be filling this in as we process to OBJ

		triangles = new MDLTriangle[objTris.length]; //make the array of triangles (OBJ -> MDL no change in # of tris)
		///// Set the header to the correct number of tris.
		header.setNumTris(triangles.length);

		ArrayList<OBJ2MDLVert> newVertices = new ArrayList();
		for (int i = 0; i < objTris.length; i++)
		{
			int[][] objTri = objTris[i];
			int[] mdlTriVertIndices = new int[3];

			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				boolean differentTextureIndex = false;
				OBJ2MDLVert temp = new OBJ2MDLVert(objTri[vIndex][0], objTri[vIndex][1]);

				if (!newVertices.contains(temp) || newVertices.isEmpty())
				{ //DOESN'T contain this obj vert index yet, so this is simple, add it as a new vertex
					newVertices.add(temp);

					/*
					This last bit is the 'magic':
					We make a new triangle with 3 indices, and we assign the NEW index (size - 1)
					to this triangle.  This will be the MDL triangles.
					 */
					mdlTriVertIndices[vIndex] = newVertices.size() - 1;
				}
				else //DOES contain this obj vert index.  Now we need to check if it is referencing the same tex coord or not.
				{
					for (int checkIndex = 0; checkIndex < newVertices.size(); checkIndex++)
					{
						/*
						If the tex coord is NOT the same, than this MUST be a
						new vertex, so we create a new vertex with the same
						index to the vert, but the different tex coord.
						This will become a NEW MDLVertex.
						 */
						if (newVertices.get(checkIndex).OBJVertIndex == temp.OBJVertIndex
							&& newVertices.get(checkIndex).OBJTextIndex != temp.OBJTextIndex)
						{
							differentTextureIndex = true;
							newVertices.add(temp);

							/*
							This last bit is the 'magic':
							We make a new triangle with 3 indices, and we assign the NEW index (size - 1)
							to this triangle.  This will be the MDL triangles.
							 */
							mdlTriVertIndices[vIndex] = newVertices.size() - 1;
							break;
						}
					}

					if (!differentTextureIndex)
					{
						for (int searchIndex = 0; searchIndex < newVertices.size(); searchIndex++)
						{
							if (objTri[vIndex][0] == newVertices.get(searchIndex).OBJVertIndex //match the index from the OBJ to the newVertices index
								&& newVertices.get(searchIndex).OBJTextIndex == temp.OBJTextIndex)
							{
								/*
								This last bit is the 'magic':
								Since THIS particular vertex was identical to another
								one already in the list, we just search through that
								list, find the index, and assign that index to the new
								tri.
								 */
								mdlTriVertIndices[vIndex] = searchIndex;
								break;
							}
						}
					}
				}
			}

			triangles[i] = new MDLTriangle(1, mdlTriVertIndices); //These are properly converted MDL triangles now!
		}
		/*
		We now know how many actual vertices there are and what texCoord each of them
		will have!
		 */

		///// Set the header to the correct number of verts.
		header.setNumVerts(newVertices.size());

		// Since we know how many verts there are, we know how many texcoords there are as well.
		texCoords = new MDLTexCoord[header.getNumVerts()];
		for (int i = 0; i < texCoords.length; i++)
		{
			int s = (int) ((obj.getTextureCoordinates()[newVertices.get(i).OBJTextIndex].x) * obj.getTextureWidth() + 0.0f);
			int t = (int) ((obj.getTextureCoordinates()[newVertices.get(i).OBJTextIndex].y) * obj.getTextureHeight() + 0.0f);
			texCoords[i] = new MDLTexCoord(0, s, t);
			//System.out.println("texcoords: " + texCoords[i]);
		}



		int numFrames = obj.getNumFrames();

		///// Update header to the correct number of frames
		header.setNumFrames(numFrames);
		frames = new MDLFrame[numFrames]; //create the array for the number of frames
		Vec3 mins = new Vec3(obj.getFrame(0).getMins()), maxs = new Vec3(obj.getFrame(0).getMins());

		for (int frameIndex = 0; frameIndex < numFrames; frameIndex++)
		{
			if (obj.getFrame(frameIndex).getMins().x < mins.x)
				mins.x = obj.getFrame(frameIndex).getMins().x;
			if (obj.getFrame(frameIndex).getMins().y < mins.y)
				mins.y = obj.getFrame(frameIndex).getMins().y;
			if (obj.getFrame(frameIndex).getMins().z < mins.z)
				mins.z = obj.getFrame(frameIndex).getMins().z;

			if (obj.getFrame(frameIndex).getMaxs().x > maxs.x)
				maxs.x = obj.getFrame(frameIndex).getMaxs().x;
			if (obj.getFrame(frameIndex).getMaxs().y > maxs.y)
				maxs.y = obj.getFrame(frameIndex).getMaxs().y;
			if (obj.getFrame(frameIndex).getMaxs().z > maxs.z)
				maxs.z = obj.getFrame(frameIndex).getMaxs().z;
		}

		Vec3 scale = new Vec3();
		scale = (maxs.subtract(mins)).scale(1 / 255f);
		/*scale.x = (maxs.x - mins.x) / 255.0f;
		scale.y = (maxs.y - mins.y) / 255.0f;
		scale.z = (maxs.z - mins.z) / 255.0f;*/
		header.setScale(scale);
		header.setTranslate(mins);

		for (int frameIndex = 0; frameIndex < numFrames; frameIndex++)
		{
			MDLVertex[] frameVerts = new MDLVertex[header.getNumVerts()];

			for (int vIndex = 0; vIndex < header.getNumVerts(); vIndex++)
			{
				Vec3 vertOrigin = obj.getFrame(frameIndex).getVertices()[newVertices.get(vIndex).OBJVertIndex];
				vertOrigin = vertOrigin.subtract(header.getTranslate());

				vertOrigin.x = vertOrigin.x / header.getScale().x;
				vertOrigin.y = vertOrigin.y / header.getScale().y;
				vertOrigin.z = vertOrigin.z / header.getScale().z;

				MDLVertex tempVert = new MDLVertex(vertOrigin, 0);
				frameVerts[vIndex] = tempVert;
			}

			frames[frameIndex] = new MDLFrame(MDLFrame.SINGLE_FRAME,
											  new MDLFrameElement(new MDLVertex(obj.getFrame(frameIndex).getMins(), 0),
																  new MDLVertex(obj.getFrame(frameIndex).getMaxs(), 0),
																  "frame",
																  frameVerts));

		}

		int numSkins = 1; //TODO: Find out # of skins from OBJ...
		header.setNumSkins(numSkins);
		header.setSkinWidth(obj.getTextureWidth());
		header.setSkinHeight(obj.getTextureHeight());
		skins = new MDLSkin[numSkins];
		for (int skinIndex = 0; skinIndex < skins.length; skinIndex++)
		{
			skins[skinIndex] = new MDLSkin(0, obj.getTextureData());
		}

		//System.out.println("scale: " + header.getScale());
		//System.out.println("translate: " + header.getTranslate());
		//System.out.println("verts: " + header.getNumVerts());
	}

	/*
	I don't like handing out the array like this. :\
	 */
	public MDLFrame[] getFrames()
	{
		return this.frames;
	}

	public MDLHeader getHeader()
	{
		return header;
	}

	public MDLTriangle[] getTriangles()
	{
		return triangles;
	}

	public MDLSkin[] getSkins()
	{
		return skins;
	}

	public MDLTexCoord[] getTexCoords()
	{
		return texCoords;
	}

	public Vec3[] getSimpleTexCoords()
	{
		Vec3[] texCoordArray = new Vec3[header.getNumTris()];

		for (int i = 0; i < texCoordArray.length; i++)
			texCoordArray[i] = new Vec3(texCoords[i].getS() / header.getSkinWidth(), texCoords[i].getT() / header.getSkinHeight(), 0.0f);

		return texCoordArray;
	}

/*	@Override
	public void drawToOpenGL(int currentFrame)
	{
		Vec3 org = new Vec3(0f, 0f, 0f);
		GL11.glBegin(GL11.GL_TRIANGLES); // Start Drawing

		//int triIndex = 1;
		for (int triIndex = 0; triIndex < header.getNumTris(); triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				org = this.frames[currentFrame].getFrameElements()[0].getVertices()[this.getTriangles()[triIndex].getVertexIndices()[vIndex]].getOrigin();

				org = (org.scale(header.getScale())).add(header.getTranslate());

				// Compute texture coordinates
				float s = this.texCoords[this.triangles[triIndex].getVertexIndices()[vIndex]].getS();
				float t = this.texCoords[this.triangles[triIndex].getVertexIndices()[vIndex]].getT();

				if (!this.triangles[triIndex].isFacingFront() && this.texCoords[this.triangles[triIndex].getVertexIndices()[vIndex]].isOnSeam())
				{
					s = s + header.getSkinWidth() * 0.5f; // Backface
				}

				// Scale s and t to range from 0.0 to 1.0
				s = (s + 0.5f) / (float) header.getSkinWidth();
				t = (t + 0.5f) / (float) header.getSkinHeight();

				// Pass texture coordinates to OpenGL
				GL11.glTexCoord2f(s, t);

				// Normal vector
				//GL11.glNormal3f(-0.525731f, 0.000000f, 0.850651f); //glNormal3fv(anorms_table[pvert - > normalIndex]);

				GL11.glVertex3f(org.x, org.y, org.z);
			}
		}

		GL11.glEnd(); //stop drawing
	}
*/
	/*@Override
	public void drawVerticesToOpenGL(int currentFrame, ArrayList<Integer> selectedVertices)
	{
		boolean isVertexSelected = false;
		Vec3 org = new Vec3(0f, 0f, 0f);
		GL11.glPointSize(4.0f);
		GL11.glBegin(GL11.GL_POINTS); // Start Drawing
		OpenGL3DView.setDefaultVertexDisplayColour();
		for (int triIndex = 0; triIndex < header.getNumTris(); triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++)
			{
				org = this.getFrames()[currentFrame].getFrameElements()[0].getVertices()[this.getTriangles()[triIndex].getVertexIndices()[vIndex]].getOrigin();

				org = org.scale(header.getScale());
				org = org.add(header.getTranslate());

				if (!selectedVertices.isEmpty())
				{
					Iterator<Integer> it = selectedVertices.iterator();
					while (it.hasNext())
					{
						Integer selIndex = it.next();
						if (selIndex.intValue() == this.triangles[triIndex].getVertexIndices()[vIndex])
						{
							isVertexSelected = true;
							break;
						}
					}
				}

				if (isVertexSelected)
					OpenGL3DView.setSelectedVertexDisplayColour();
				else if (this.texCoords[triangles[triIndex].getVertexIndices()[vIndex]].isOnSeam())
					OpenGL3DView.setDefaultVertexOnseamDisplayColour();
				else
					OpenGL3DView.setDefaultVertexDisplayColour();


				GL11.glVertex3f(org.x, org.y, org.z);

				//if (isVertexSelected)
				//{
				isVertexSelected = false;
				//	OpenGL3DView.setDefaultVertexDisplayColour();
				//}
			}
		}
		GL11.glEnd(); //stop drawing
	}*/

	@Override
	public int UploadTextureToOpenGL(int skinIndex)
	{
		int texID;
		int[] skinData = this.getSkins()[skinIndex].getData();

		byte[] pixels = new byte[header.getSkinWidth() * header.getSkinHeight() * 3];

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

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, 3, header.getSkinWidth(), header.getSkinHeight(), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, pixelsBuffer);

		return texID;
	}

	@Override
	public void setTexture(PCX pcxImage)
	{
		JOptionPane.showMessageDialog(null, "Texture loading on MDLs not supported yet.");
		//throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean getHasTexture()
	{
		return this.header.getNumSkins() > 0;
	}

	@Override
	public int[] getTextureData()
	{
		return this.skins[0].getData();
	}

	@Override
	public int getTextureWidth()
	{
		return this.header.getSkinWidth();
	}

	@Override
	public int getTextureHeight()
	{
		return this.header.getSkinHeight();
	}

	@Override
	public void exportMDL(String _filename)
	{
		int sizeEstimate = MDL_HEADER_SIZE; //start with the header size.

		//TODO: REMEMBER, skin groups have a DIFFERENT sized struct!!!  If group == 1, you'll have to calculate differently
		sizeEstimate += this.header.getNumSkins() * 4; //each skin has a 4 byte int

		//add in bytes for each skin (width * height)
		sizeEstimate += this.header.getNumSkins() * this.header.getSkinWidth() * this.header.getSkinHeight();

		//add in size for texcoords
		sizeEstimate += this.header.getNumVerts() * (4 * 3); //mdl_texcoord_t is 3 4byte ints * number of verts

		sizeEstimate += this.header.getNumTris() * (4 * 4); //mdl_triangle_t is 1 4byte int + array of 3 4byte ints (16 bytes total)

		sizeEstimate += this.header.getNumFrames() * (4 + 4 + 4 + 16 + (this.header.getNumVerts() * 4));
		//long/int + struct mdl_vertex_t + struct mdl_vertex_t + char[16] + struct mdl_vertex_t * numVerts ALL * numFrames

		System.out.println("Estimated file size: " + sizeEstimate + " bytes.");

		ByteBuffer buffer = ByteBuffer.allocate(sizeEstimate);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(this.header.getIdent());
		buffer.putInt(this.header.getVersion());
		buffer.putFloat(this.header.getScale().x);
		buffer.putFloat(this.header.getScale().y);
		buffer.putFloat(this.header.getScale().z);
		buffer.putFloat(this.header.getTranslate().x);
		buffer.putFloat(this.header.getTranslate().y);
		buffer.putFloat(this.header.getTranslate().z);
		buffer.putFloat(this.header.getBoundingRadius());
		buffer.putFloat(this.header.getEyePosition().x);
		buffer.putFloat(this.header.getEyePosition().y);
		buffer.putFloat(this.header.getEyePosition().z);
		buffer.putInt(this.header.getNumSkins());
		buffer.putInt(this.header.getSkinWidth());
		buffer.putInt(this.header.getSkinHeight());
		buffer.putInt(this.header.getNumVerts());
		buffer.putInt(this.header.getNumTris());
		buffer.putInt(this.header.getNumFrames());
		buffer.putInt(this.header.getSyncType());
		buffer.putInt(this.header.getFlags());
		buffer.putFloat(this.header.getSize());

		for (int i = 0; i < this.header.getNumSkins(); i++) //write skin data
		{
			buffer.putInt(this.getSkins()[i].getGroup());
			for (int j = 0; j < this.getSkins()[i].getData().length; j++)
				buffer.put((byte) this.getSkins()[i].getData()[j]); //cast the fake ints (unsigned bytes) back to bytes.
		}

		for (int i = 0; i < this.header.getNumVerts(); i++)
		{
			buffer.putInt(this.texCoords[i].getOnseam());
			buffer.putInt((int) Math.round(this.texCoords[i].getS()));
			buffer.putInt((int) Math.round(this.texCoords[i].getT()));
		}

		for (int i = 0; i < this.header.getNumTris(); i++)
		{
			buffer.putInt(this.triangles[i].getFacesFront());
			buffer.putInt(this.triangles[i].getVertexIndices()[0]);
			buffer.putInt(this.triangles[i].getVertexIndices()[1]);
			buffer.putInt(this.triangles[i].getVertexIndices()[2]);
		}

		//sizeEstimate += this.header.getNumFrames() * (4 + 4 + 4 + 16 + (this.header.getNumVerts() * 4));
		for (int i = 0; i < this.header.getNumFrames(); i++)
		{
			buffer.putInt(this.frames[i].getType());
			buffer.put((byte) this.frames[i].getFrameElements()[0].getMins().getOrigin().x);
			buffer.put((byte) this.frames[i].getFrameElements()[0].getMins().getOrigin().y);
			buffer.put((byte) this.frames[i].getFrameElements()[0].getMins().getOrigin().z);
			buffer.put((byte) 0); //normal index which is unused
			buffer.put((byte) this.frames[i].getFrameElements()[0].getMaxs().getOrigin().x);
			buffer.put((byte) this.frames[i].getFrameElements()[0].getMaxs().getOrigin().y);
			buffer.put((byte) this.frames[i].getFrameElements()[0].getMaxs().getOrigin().z);
			buffer.put((byte) 0); //normal index which is unused
			String name = this.frames[i].getFrameElements()[0].getName();
			for (int j = 0; j < 16; j++)
			{
				if (j >= name.length())
					buffer.put((byte) '\0'); //let's be cool and zero out the unused chars.
				else
					buffer.put((byte) name.charAt(j)); //FFS, chars are 4 bytes in java. :(
			}
			for (int j = 0; j < this.header.getNumVerts(); j++)
			{
				buffer.put((byte) this.frames[i].getFrameElements()[0].getVertices()[j].getOrigin().x);
				buffer.put((byte) this.frames[i].getFrameElements()[0].getVertices()[j].getOrigin().y);
				buffer.put((byte) this.frames[i].getFrameElements()[0].getVertices()[j].getOrigin().z);
				buffer.put((byte) this.frames[i].getFrameElements()[0].getVertices()[j].getNormalIndex());
			}
		}

		byte[] bytes = buffer.array();

		File outputFile = new File(_filename);
		FileOutputStream outputStream = null;
		try
		{
			outputStream = new FileOutputStream(outputFile);
			outputStream.write(bytes);
		}
		catch (Exception ex)
		{
			Logger.getLogger(MDL.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException(ex.getMessage());
		}
		finally
		{
			try
			{
				outputStream.close();
			}
			catch (IOException ex)
			{
				Logger.getLogger(MDL.class.getName()).log(Level.SEVERE, null, ex);
				throw new RuntimeException(ex.getMessage());
			}
		}
	}

	@Override
	public int getFrameIndexFromNameIndex(int selectedIndex)
	{
		return selectedIndex;
	}

	@Override
	public float[][] getScalarUVCoordsArray()
	{
		float[][] UVArray = new float[this.texCoords.length][3];

		for (int i = 0; i < UVArray.length; i++)
		{
			UVArray[i][0] = (float) this.texCoords[i].getS() / this.header.getSkinWidth();
			UVArray[i][1] = (float) this.texCoords[i].getT() / this.header.getSkinHeight();
			UVArray[i][2] = this.texCoords[i].getOnseam(); //2 = onseam values
		}

		return UVArray;
	}

	@Override
	public int[][][] getUV_TriRelationArray()
	{
		int[][][] triArray = new int[this.triangles.length][3][3];

		for (int triIndex = 0; triIndex < triArray.length; triIndex++)
		{
			for (int vIndex = 0; vIndex < 3; vIndex++) //MDL has 1:1 relationship between verts and skin verts.
			{
				triArray[triIndex][vIndex][0] = triArray[triIndex][vIndex][1] = triangles[triIndex].getVertexIndices()[vIndex];
				triArray[triIndex][vIndex][2] = triangles[triIndex].getFacesFront(); //2 = front facing tri value
			}
		}

		return triArray;
	}

	@Override
	public void moveUVCoordBy(int index, float deltaX, float deltaY, int zoom)
	{
		deltaX = deltaX / zoom;
		deltaY = deltaY / zoom;

		texCoords[index].setS(texCoords[index].getS() + deltaX);
		texCoords[index].setT(texCoords[index].getT() + deltaY);
	}

	@Override
	public String[] getFrameNames()
	{
		MDLFrameElement[] frameElements;

		int totalFrames = 0;
		for (int i = 0; i < frames.length; i++)
			totalFrames = totalFrames + frames[i].getFrameElements().length;

		ArrayList<String> frameNames = new ArrayList();



		for (int i = 0; i < frames.length; i++)
		{
			frameElements = frames[i].getFrameElements();
			for (int j = 0; j < frameElements.length; j++)
				frameNames.add(frameElements[j].getName());
		}

		return frameNames.toArray(new String[frameNames.size()]);
	}

	@Override
	public Vec3[] getVertices(int frame)
	{
		MDLVertex[] vertices = this.frames[frame].getFrameElements()[0].getVertices();
		Vec3[] vOrigins = new Vec3[vertices.length];

		for (int vIndex = 0; vIndex < vertices.length; vIndex++)
		{
			Vec3 vOrigin = vertices[vIndex].getOrigin();
			vOrigin = vOrigin.scale(header.getScale()); //pre-scale vertex positions.
			vOrigin = vOrigin.add(header.getTranslate());
			vOrigins[vIndex] = vOrigin;
		}

		return vOrigins;
	}


}
