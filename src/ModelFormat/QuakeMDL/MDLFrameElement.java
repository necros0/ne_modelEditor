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
public class MDLFrameElement
{
	private MDLVertex mins; // bouding box min
	private MDLVertex maxs; // bouding box max
	//should the above be Vec3's?
	private String name; //this cannot be long than 15 (16 - 1 for \0) characters
	private MDLVertex[] vertices;  // vertex list of the frame

	public MDLFrameElement(MDLVertex _mins, MDLVertex _maxs, String _name, MDLVertex[] _vertices)
	{
		mins = _mins;
		maxs = _maxs;
		name = _name;
		vertices = _vertices;
	}

	public String getName()
	{
		return name;
	}

	public MDLVertex[] getVertices()
	{
		return vertices;
	}

	public MDLVertex getMins()
	{
		return mins;
	}

	public MDLVertex getMaxs()
	{
		return maxs;
	}

	//FIXME: This is a HACK
	public void HACK_setName(String _name)
	{
		name = _name;
	}




	private String listVerts()
	{
		String str = "";

		for (int i = 0; i < vertices.length; i++)
		{
			str = str + "\t" + vertices[i] + "\n";
		}
		return str;
	}

	@Override
	public String toString()
	{
		return String.format("name: %s, mins: [%s], maxs: [%s]" /*, \n\tvertex list:\n%s"*/, name, mins, maxs/*, listVerts()*/);
		//return String.format("name: %s, mins: [%s], maxs: [%s]", name, mins, maxs);
	}
}
