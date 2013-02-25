package com.skyline.application.tools;


public abstract class AbstractCursor implements Cursor{

	protected float power=0f;
	protected float radius=0f;
	protected int maxRenderHeight=100;
	protected int maxRenderRadius=100;
	protected float renderScale=1f;

	public AbstractCursor(float power, float radius, int maxRenderHeight, int maxRenderRadius, float renderScale){
		this.power=power;
		this.radius=radius;
		this.maxRenderHeight=maxRenderHeight;
		this.maxRenderRadius=maxRenderRadius;
		this.renderScale=renderScale;
	}
	
	public float getPower() {
		return power;
	}


	public void setPower(float power) {
		this.power = power;
	}


	public float getRadius() {
		return radius;
	}


	public void setRadius(float radius) {
		this.radius = radius;
	}


	public int getMaxRenderHeight() {
		return maxRenderHeight;
	}


	public int getMaxRenderRadius() {
		return maxRenderRadius;
	}

}
