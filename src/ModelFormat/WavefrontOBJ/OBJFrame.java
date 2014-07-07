/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.WavefrontOBJ;

import Geometry.Vec3;

/**
 *
 * @author cjones
 */
public class OBJFrame
{
	private String name;
	private Vec3[] vertices;
	private Vec3[] vertexNormals;
	private Vec3 mins, maxs;

	public OBJFrame(String _name, Vec3[] _vertices, Vec3[] _vertexNormals, Vec3 _mins, Vec3 _maxs)
	{
		name = _name;
		vertices = _vertices;
		vertexNormals = _vertexNormals;
		mins = new Vec3(_mins);
		maxs = new Vec3(_maxs);
	}

	public Vec3[] getVertexNormals()
	{
		return vertexNormals;
	}

	public Vec3[] getVertices()
	{
		return vertices;
	}

	public Vec3 getMins()
	{
		return mins;
	}

	public Vec3 getMaxs()
	{
		return maxs;
	}

	public String getName()
	{
		return name;
	}
}
