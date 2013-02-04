package com.megatron.test.roads.spline.applet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

class BezierLine extends PointList {
	Point bpt[];
	int bnum;
	boolean ready;
	final int MAXPOINT = 1800;
	final int ENOUGH = 2;
	final int RECURSION = 900;
	int nPointAlloc;
	int enough; // control how well we draw the curve.
	int nRecur; // counter of number of recursion
	Point buffer[][];
	int nBuf, nBufAlloc;

	BezierLine() {
		bpt = new Point[MAXPOINT];
		nPointAlloc = MAXPOINT;
		bnum = 0;
		enough = ENOUGH;
		showLine = true;
		ready = false;
		buffer = null;
	}

	protected int distance(Point p0, Point p1, Point p2)
	{
		int a, b, y1, x1, d1, d2;

		if (p1.x == p2.x && p1.y == p2.y)
			return Math.min(Math.abs(p0.x - p1.x), Math.abs(p0.y - p1.y));
		a = p2.x - p1.x;
		b = p2.y - p1.y;
		y1 = b * (p0.x - p1.x) + a * p1.y;
		x1 = a * (p0.y - p1.y) + b * p1.x;
		d1 = Math.abs(y1 - a * p0.y);
		d2 = Math.abs(x1 - b * p0.x);
		if (a == 0)
			return Math.abs(d2 / b);
		if (b == 0)
			return Math.abs(d1 / a);
		return Math.min(Math.abs(d1 / a), Math.abs(d2 / b));
	}

	protected void curve_split(Point p[], Point q[], Point r[], int num)
	{
		int i, j;

		// for (i=0;i<num;i++) q[i] = new Point(p[i]);
		for (i = 0; i < num; i++) {
			q[i].x = p[i].x;
			q[i].y = p[i].y;
		}
		for (i = 1; i <= num - 1; i++) {
			// r[num-i] = new Point(q[num-1]);
			r[num - i].x = q[num - 1].x;
			r[num - i].y = q[num - 1].y;
			for (j = num - 1; j >= i; j--) {
				// q[j] = new Point((q[j-1].x+q[j].x)/2, (q[j-1].y+q[j].y)/2);
				q[j].x = (q[j - 1].x + q[j].x) / 2;
				q[j].y = (q[j - 1].y + q[j].y) / 2;
			}
		}
		// r[0] = new Point(q[num-1]);
		r[0].x = q[num - 1].x;
		r[0].y = q[num - 1].y;
	}

	// reuse buffer
	private Point get_buf(int num)[]
	{
		Point b[];
		if (buffer == null)
		{
			buffer = new Point[500][num];
			nBufAlloc = 500;
			nBuf = 0;
		}
		if (nBuf == 0)
		{
			b = new Point[num];
			for (int i = 0; i < num; i++)
				b[i] = new Point();
			return b;
		}
		else {
			nBuf--;
			b = buffer[nBuf];
			return b;
		}
	}

	private void put_buf(Point b[])
	{
		if (nBuf >= nBufAlloc)
		{
			Point newBuf[][] = new Point[nBufAlloc + 500][num];
			for (int i = 0; i < nBuf; i++)
				newBuf[i] = buffer[i];
			nBufAlloc += 500;
			buffer = newBuf;
		}
		buffer[nBuf] = b;
		nBuf++;
	}

	protected boolean bezier_generation(Point pt[], int num, Point result[], int n[])
	{
		Point qt[], rt[]; // for split
		int d[], i, max;

		nRecur++;
		if (nRecur > RECURSION)
			return false;

		d = new int[MAXCNTL];
		for (i = 1; i < num - 1; i++)
			d[i] = distance(pt[i], pt[0], pt[num - 1]);
		max = d[1];
		for (i = 2; i < num - 1; i++)
			if (d[i] > max)
				max = d[i];
		if (max <= enough || nRecur > RECURSION) {
			if (n[0] == 0) {
				if (bnum > 0) {
					result[0].x = pt[0].x;
					result[0].y = pt[0].y;
				}
				else
					result[0] = new Point(pt[0]);
				n[0] = 1;
			}
			// reuse
			if (bnum > n[0]) {
				result[n[0]].x=pt[num - 1].x;
				result[n[0]].y=pt[num - 1].y;
			}
			else
				result[n[0]] = new Point(pt[num - 1]);
			n[0]++;
			if (n[0] == MAXPOINT - 1)
				return false;
		}
		else {
			// qt = new Point[num];
			// rt = new Point[num];
			qt = get_buf(num);
			rt = get_buf(num);
			curve_split(pt, qt, rt, num);
			if (!bezier_generation(qt, num, result, n))
				return false;
			put_buf(qt);
			if (!bezier_generation(rt, num, result, n))
				return false;
			put_buf(rt);
		}
		return true;
	}

	public boolean try_bezier_generation(Point pt[], int num, Point result[], int n[])
	{
		int oldN = n[0];

		if (enough == ENOUGH && num > 6)
			enough += 3;
		// if (enough > ENOUGH) enough -= 5;
		nRecur = 0;
		// in case of recursion stack overflow, relax "enough" and keep trying
		while (!bezier_generation(pt, num, bpt, n))
		{
			n[0] = oldN;
			enough += 5;
			nRecur = 0;
		}
		return true;
	}

	boolean createFinal()
	{
		int n[];
		n = new int[1];
		if (!try_bezier_generation(pt, num, bpt, n))
		{
			bnum = 0;
			return false;
		}
		else {
			bnum = n[0];
			return true;
		}
	}

	boolean done()
	{
		num--;
		showLine = false;
		ready = true;
		return createFinal();
	}

	void draw(Graphics g)
	{
		g.setColor(new Color(x, y, z));
		if (showLine)
		{
			super.draw(g);
			if (curPt != -1)
				g.drawRect(pt[curPt].x - range, pt[curPt].y - range, 2 * range + 1, 2 * range + 1);
		}

		if (ready)
			for (int i = 0; i < bnum - 1; i++)
			{
				g.drawLine(bpt[i].x, bpt[i].y, bpt[i + 1].x, bpt[i + 1].y);
			}
	}
}