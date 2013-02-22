/*
 *  Procedurality v. 0.1 rev. 20070307
 *  Copyright 2007 Oddlabs ApS
 *
 *
 *  This file is part of Procedurality.
 *  Procedurality is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *
 *  Procedurality is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package com.skyline.util;

import java.nio.ByteBuffer;

public final strictfp class Utils {

	public final static float intToRed(int color) {
		int red = (color >> 16) & 0xff;
		return ((float)red)/255;
	}

	public final static float intToGreen(int color) {
		int green = (color >> 8) & 0xff;
		return ((float)green)/255;
	}

	public final static float intToBlue(int color) {
		int blue = color & 0xff;
		return ((float)blue)/255;
	}

	public final static boolean isPowerOf2(int n) {
		return n == 0 || (n > 0 && (n & (n - 1)) == 0);
	}

	public final static int nextPowerOf2(int n) {
		int x = 1;
		while (x < n) {
			x <<= 1;
		}
		return x;
	}

	public final static void flip(ByteBuffer bytes, int width, int height) {
		byte[] line = new byte[width];
		byte[] line2 = new byte[width];

		for (int i = 0; i < height/2; i++) {
			bytes.position(i*width);
			bytes.get(line);
			bytes.position((height - i - 1)*width);
			bytes.get(line2);
			bytes.position(i*width);
			bytes.put(line2);
			bytes.position((height - i - 1)*width);
			bytes.put(line);
		}
	}

	public final static int powerOf2Log2(int n) {
		assert isPowerOf2(n): n + " is not a power of 2";
		for (int i = 0; i < 31; i++) {
			if ((n & 1) == 1) {
				return i;
			}
			n >>= 1;
		}
		return 0;
	}

}
