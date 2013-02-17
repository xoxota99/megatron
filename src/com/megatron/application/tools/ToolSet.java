package com.megatron.application.tools;

import java.util.*;

import com.megatron.application.*;
import com.megatron.model.*;

/**
 * A ToolSet is a collection of related tools. A particular instance of a tool
 * may belong to more than one toolset.
 * 
 * ToolSet observes the WorldState, and passes events onto it's tools.
 * 
 * @author philippd
 * 
 */
public class ToolSet extends Observable implements Observer {

	private List<Tool> tools;
	private String name;

	public ToolSet(String name) {
		this.name = name;
		tools = new ArrayList<Tool>();
		// Zero slot is always taken by the NULL tool. This tool does nothing.
		addTool(new GlobalTool("[None]", "[None]") {
			@Override
			public void execute(WorldState context) {
				// LOL Do nothing.
			}

			@Override
			public boolean isContinuous() {
				return false;
			}
		});
	}

	public ToolSet(String name, List<Tool> tools) {
		this(name);
		for(Tool t : tools){
			addTool(t);
		}
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void update(Observable o, Object arg) {
		// Pass WorldState events onto Tools.
		if (o instanceof WorldState) {
			this.setChanged();
			this.notifyObservers(arg);
		}
	}

	public void addTool(Tool tool) {
		tools.add(tool);
		addObserver(tool);
	}

	public void addTool(int index, Tool tool) {
		tools.add(index, tool);
		addObserver(tool);
	}

	public Tool getTool(int index) {
		return tools.get(index);
	}

	public int size() {
		return tools.size();
	}

}
