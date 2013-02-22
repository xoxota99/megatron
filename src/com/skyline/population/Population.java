package com.skyline.population;

import com.skyline.model.*;


public final class Population {
	private Population() {
	}

	public static void trim(WorldState worldState) {
		if(worldState!=null){
			trim(worldState.getPopDensity());
		}
	}
	protected static void trim(float[][] popDensity) {
		// drop the edges.
		for(int x=0;x<popDensity.length;x++){
			popDensity[x][0]=0;
			popDensity[0][x]=0;
			popDensity[x][popDensity.length-1]=0;
			popDensity[popDensity.length-1][x]=0;
		}
	}
}
