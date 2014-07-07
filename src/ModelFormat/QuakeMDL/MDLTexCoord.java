/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ModelFormat.QuakeMDL;

/**
 *
 * @author cjones
 */
public class MDLTexCoord
{
	private int onseam;
	private float s; //mdl docs said these are shorts, but they are not!
	private float t;

	public MDLTexCoord(int _onseam, int _s, int _t)
	{
		onseam = _onseam;
		s = _s;
		t = _t;
		//System.out.printf("onseam: %d, s: %d, t: %d\n", onseam, s, t);
	}

	public float getS()
	{
		return s;
	}

	public float getT()
	{
		return t;
	}

	public void setS(float s)
	{
		this.s = s;
	}

	public void setT(float t)
	{
		this.t = t;
	}

	public int getOnseam()
	{
		return onseam;
	}

	public void setOnseam(int onseam)
	{
		/* Preach says:
		 * From: http://celephais.net/board/view_thread.php?id=60335&start=755&end=755
		OK, so here's the actual deal. It does relate to onseam vertices.
		Winquake is picky about the flag in a way glquake isn't. You MUST use
		the value 32 to indicate a vertex is onseam. Setting this value to 1
		works fine in glquake but glitches out in software. I'm guessing
		there's a mixture of code in winquake between

		if(v.onseam) //checks for any non-zero value
		and
		if(v.onseam == 32) //checks for 32 only

		So if you aren't careful you get half-onseam rendering, which is a
		big mess. Lesson learnt.  GLquake probably just uses the former test
		everywhere which explains how I could get away with being sloppy
		so far...
		 */
		this.onseam = 32;
	}

	public boolean isOnSeam()
	{
		return onseam != 0;
	}

	@Override
	public String toString()
	{
		return String.format("s: %d, t: %d", s, t);
	}
}
