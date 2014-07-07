/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package OpenGL;

import Geometry.Vec3;
import org.lwjgl.opengl.GL11;

/**
 *
 * @author cjones
 */
public class Primitive
{
	public static void face(Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4)
	{
		GL11.glVertex3f(p1.x, p1.y, p1.z);
		GL11.glVertex3f(p2.x, p2.y, p2.z);
		GL11.glVertex3f(p3.x, p3.y, p3.z);
		GL11.glVertex3f(p4.x, p4.y, p4.z);
	}

	public static void drawCube(Vec3 org, float size)
	{
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		GL11.glColor3f(0.8f, 0.8f, 0.8f);

		GL11.glBegin(GL11.GL_QUADS);

		Vec3 vec = new Vec3(size, size, size);
		vec.scale(0.5f);

		face(org.add(vec.scale(new Vec3( 1.0f,  1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f,  1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f,  1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f,  1.0f,  1.0f))));

		face(org.add(vec.scale(new Vec3( 1.0f, -1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f, -1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f, -1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f, -1.0f, -1.0f))));

		face(org.add(vec.scale(new Vec3( 1.0f,  1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f,  1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f, -1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f, -1.0f,  1.0f))));

		face(org.add(vec.scale(new Vec3( 1.0f, -1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f, -1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f,  1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f,  1.0f, -1.0f))));

		face(org.add(vec.scale(new Vec3(-1.0f,  1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f,  1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f, -1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3(-1.0f, -1.0f,  1.0f))));

		face(org.add(vec.scale(new Vec3( 1.0f,  1.0f, -1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f,  1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f, -1.0f,  1.0f))),
			 org.add(vec.scale(new Vec3( 1.0f, -1.0f, -1.0f))));

		GL11.glEnd();
	}
}
