package com.megatron.application.tools;

import java.util.*;

/**
 * Tools sit on the fence between the "logical" coordinate system (which uses
 * the coordinates established by the worldState.size), and the "real-world"
 * coordinate system, which can be affected by scaling, translating, rotating,
 * etc in the renderer.
 * 
 * Wherever possible, we try to operate on the underlying data, and ignore
 * details of the renderer.
 * 
 * Tools observe their respective ToolBars, which will pass along WorldState
 * events.
 * 
 * @author philippd
 * 
 */
public interface Tool extends Observer {
	public String getName();

	public String getToolTipText();
}
