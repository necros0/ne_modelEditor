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
public class MDLVertex
{
	/* Compressed vertex */
	private Vec3 org; //this is read as a 3 unsigned byte array (3 c chars)
	private int normalIndex; //this is an unsigned byte (c char) referencing a normal in the anorms.h file

	public MDLVertex(int[] _org, int _normalIndex)
	{
		org = new Vec3(_org);
		normalIndex = _normalIndex;
	}

	public MDLVertex(Vec3 _org, int _normalIndex)
	{
		org = new Vec3(_org);
		normalIndex = _normalIndex;
	}

	public Vec3 getOrigin()
	{
		return org;
	}

	public int getNormalIndex()
	{
		return normalIndex;
	}

	@Override
	public String toString()
	{
		return String.format("'%s', normal: %d", org, normalIndex);
	}
}
