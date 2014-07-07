/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.QuakeMDL;

/**
 *
 * @author cjones
 */
public class MDLSkin
{
	private int group;     // 1 = group
	private int nb;        // number of pics
	private float[] time;   // time duration for each pi
	private int[] data;  // texture data (should be read and written as unsigned bytes)

	public MDLSkin(int _group, int _nb, float[] _time, int[] _data)
	{
		group = _group;
		nb = _nb;
		time = _time;
		data = _data;
	}

	public MDLSkin(int _group, int[] _data)
	{
		group = _group;
		nb = 1;
		time = new float[1];
		time[0] = 0.1f;
		data = _data;
	}

	public int getGroup()
	{
		return group;
	}

	public int[] getData()
	{
		return data;
	}
}
