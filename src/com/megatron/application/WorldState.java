package com.megatron.application;

import java.util.*;

import com.jme3.terrain.heightmap.*;
import com.megatron.application.events.*;

/**
 * WorldState holds (what else?) the state of the world. Tools and toolsets will
 * modify the WorldState, and the application will display the worldstate.
 * 
 * The application observes the worldState, and so can be notified of "Events"
 * that occur within the world.
 * 
 * @author philippd
 * 
 */
public class WorldState extends Observable {

	public static final int SHIFT = 0x1;
	public static final int CTRL = 0x10;
	public static final int ALT = 0x100;
	public static final float TERRAIN_MIN = -50f;

	private HeightMap terrainHeightMap;
	private HeightMap originalHeightMap; // keep this, for "resetting".
	private int inputModifiers = 0; // SHIFT, CTRL, or ALT;
	private long seed = 0L;
	private int size = 256;

	/**
	 * Notify observers that something has changed.
	 * 
	 * @param evt
	 */
	public void triggerChangeEvent(Event evt) {
		this.setChanged();
		this.notifyObservers(evt);
	}

	public WorldState() {
		this(System.currentTimeMillis());
	}

	public WorldState(long seed) {
		this.seed = seed;
	}

	public HeightMap getTerrainHeightMap() {
		return terrainHeightMap;
	}

	public void setTerrainHeightMap(HeightMap terrainHeightMap) {
		this.terrainHeightMap = terrainHeightMap;
	}

	/**
	 * Indicates which "modifier" keys are currently pressed ({@link SHIFT},
	 * {@link CTRL}, or {@link ALT}, or some combination thereof).
	 * 
	 * @return
	 */
	public int getInputModifiers() {
		return inputModifiers;
	}

	public void setInputModifiers(int inputModifiers) {
		this.inputModifiers = inputModifiers;
	}

	/**
	 * Set a single Modifier ({@link SHIFT}, {@link CTRL}, or {@link ALT}),
	 * without affecting the currently set modifiers.
	 * 
	 * @param newModifier
	 */
	public void setInputModifier(int newModifier) {
		if (newModifier == SHIFT
				|| newModifier == ALT
				|| newModifier == CTRL) {
			this.inputModifiers |= newModifier;
		}
	}

	/**
	 * Clear a single Modifier ({@link SHIFT}, {@link CTRL}, or {@link ALT}),
	 * without affecting other currently set modifiers.
	 * 
	 * @param newModifier
	 */
	public void clearInputModifier(int newModifier) {
		if (newModifier == SHIFT
				|| newModifier == ALT
				|| newModifier == CTRL) {
			this.inputModifiers &= ~newModifier;
		}
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	/**
	 * 
	 * @param modifier
	 *            the modifier to verify.
	 * @return true if the specified modifier key ({@link SHIFT}, {@link CTRL},
	 *         or {@link ALT}, or any combination thereof) is currently pressed,
	 *         false otherwise.
	 */
	public boolean getInputModifier(int modifier) {
		return (this.inputModifiers & modifier) != 0;
	}

	public int getSize() {
		return size;
	}
}
