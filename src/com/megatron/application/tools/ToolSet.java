package com.megatron.application.tools;

import java.util.*;

import com.megatron.application.*;

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
	}

	public ToolSet(String name, List<Tool> tools) {
		this(name);
		this.tools = tools;
		for (Tool t : tools) {
			this.addObserver(t);
		}
	}

	public List<Tool> getTools() {
		return tools;
	}

	public void setTools(List<Tool> tools) {
		this.tools = tools;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void update(Observable o, Object arg) {
		//Pass WorldState events onto Tools.
		if (o instanceof WorldState) {
			this.setChanged();
			this.notifyObservers(arg);
		}
	}
}
