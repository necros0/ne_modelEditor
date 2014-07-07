/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.QuakeMDL;

import Geometry.Vec3;

/**
 *
 * @author cjones
 */
public class MDLHeader
{
	private int ident;            //magic number: "IDPO" (1330660425 as an int)
	private int version;          //version: 6
	private Vec3 scale;           //scale factor
	private Vec3 translate;       //translation vector
	private float boundingRadius;
	private Vec3 eyePosition;	  //eyes' position
	private int numSkins;        //number of textures
	private int skinWidth;        //texture width
	private int skinHeight;       //texture height
	private int numVerts;        //number of vertices
	private int numTris;         //number of triangles
	private int numFrames;       //number of frames
	private int syncType;         //0 = synchronized, 1 = random
	private int flags;            //state flag
	private float size;

	public MDLHeader()
	{
		ident = 1330660425;
		version = 6;
		scale = new Vec3(0.0f, 0.0f, 0.0f);
		translate = new Vec3(0.0f, 0.0f, 0.0f);
		boundingRadius = 0.0f;
		eyePosition = new Vec3(0.0f, 0.0f, 0.0f);
		numSkins = 0;
		skinWidth = 0;
		skinHeight = 0;
		numVerts = 0;
		numTris = 0;
		numFrames = 0;
		syncType = 0;
		flags = 0;
		size = 0.0f;
	}

	public MDLHeader(int _ident,
					 int _version,
					 float[] _vec3_scale,
					 float[] _vec3_translate,
					 float _boundingRadius,
					 float[] _vec3_eyePosition,
					 int _numSkins,
					 int _skinWidth,
					 int _skinHeight,
					 int _numVerts,
					 int _numTris,
					 int _numFrames,
					 int _syncType,
					 int _flags,
					 float _size)
	{
		ident = _ident;
		version = _version;
		scale = new Vec3(_vec3_scale[0], _vec3_scale[1], _vec3_scale[2]);
		translate = new Vec3(_vec3_translate[0], _vec3_translate[1], _vec3_translate[2]);
		boundingRadius = _boundingRadius;
		eyePosition = new Vec3(_vec3_eyePosition[0], _vec3_eyePosition[1], _vec3_eyePosition[2]);
		numSkins = _numSkins;
		skinWidth = _skinWidth;
		skinHeight = _skinHeight;
		numVerts = _numVerts;
		numTris = _numTris;
		numFrames = _numFrames;
		syncType = _syncType;
		flags = _flags;
		size = _size;
	}

	public float getBoundingRadius()
	{
		return boundingRadius;
	}

	public Vec3 getEyePosition()
	{
		return eyePosition;
	}

	public int getFlags()
	{
		return flags;
	}

	public int getIdent()
	{
		return ident;
	}

	public int getNumFrames()
	{
		return numFrames;
	}

	public int getNumTris()
	{
		return numTris;
	}

	public int getNumVerts()
	{
		return numVerts;
	}

	public Vec3 getScale()
	{
		return scale;
	}

	public float getSize()
	{
		return size;
	}

	public int getSkinHeight()
	{
		return skinHeight;
	}

	public int getSkinWidth()
	{
		return skinWidth;
	}

	public int getSyncType()
	{
		return syncType;
	}

	public Vec3 getTranslate()
	{
		return translate;
	}

	public int getVersion()
	{
		return version;
	}

	public int getNumSkins()
	{
		return numSkins;
	}

	public void setBoundingRadius(float boundingRadius)
	{
		this.boundingRadius = boundingRadius;
	}

	public void setEyePosition(Vec3 eyePosition)
	{
		this.eyePosition = new Vec3(eyePosition);
	}

	public void setFlags(int flags)
	{
		this.flags = flags;
	}

	public void setIdent(int ident)
	{
		this.ident = ident;
	}

	public void setNumFrames(int numFrames)
	{
		this.numFrames = numFrames;
	}

	public void setNumSkins(int numSkins)
	{
		this.numSkins = numSkins;
	}

	public void setNumTris(int numTris)
	{
		this.numTris = numTris;
	}

	public void setNumVerts(int numVerts)
	{
		this.numVerts = numVerts;
	}

	public void setScale(Vec3 scale)
	{
		this.scale = new Vec3(scale);
	}

	public void setSize(float size)
	{
		this.size = size;
	}

	public void setSkinHeight(int skinHeight)
	{
		this.skinHeight = skinHeight;
	}

	public void setSkinWidth(int skinWidth)
	{
		this.skinWidth = skinWidth;
	}

	public void setSyncType(int syncType)
	{
		this.syncType = syncType;
	}

	public void setTranslate(Vec3 translate)
	{
		this.translate = new Vec3(translate);
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@Override
	public String toString()
	{
		return String.format("ident: %d\n"
							 + "version: %d\n"
							 + "scale: %s\n"
							 + "translate: %s\n"
							 + "boundingRadius: %f\n"
							 + "eyePosition: %s\n"
							 + "numSkins: %d\n"
							 + "skinWidth: %d\n"
							 + "skinHeight: %d\n"
							 + "numVerts: %d\n"
							 + "numTris: %d\n"
							 + "numFrames: %d\n"
							 + "syncType: %d\n"
							 + "flags: %d\n"
							 + "size: %f",
							 ident,
							 version,
							 scale,
							 translate,
							 boundingRadius,
							 eyePosition,
							 numSkins,
							 skinWidth,
							 skinHeight,
							 numVerts,
							 numTris,
							 numFrames,
							 syncType,
							 flags,
							 size);
	}
}
