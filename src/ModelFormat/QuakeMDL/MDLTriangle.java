/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.QuakeMDL;

/**
 *
 * @author cjones
 */
public class MDLTriangle
{
	private int facesFront;  // 0 = backface, 1 = frontface
	private int[] vertexIndices = new int[3];   //3 vertex indices

	public MDLTriangle(int _facesFront, int[] _vertexIndices)
	{
		facesFront = _facesFront;
		vertexIndices = _vertexIndices;
		//System.out.printf("facesFront: %d, vertex indices: %d, %d %d.\n", facesFront, vertices[0], vertices[1], vertices[2]);
	}

	public int[] getVertexIndices()
	{
		return vertexIndices;
	}

	public int getFacesFront()
	{
		return facesFront;
	}

	public boolean isFacingFront()
	{
		return facesFront == 1;
	}
}
