package com.megatron.test.roads.spline.applet;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Event;
import java.awt.Frame;
import java.awt.Label;

@SuppressWarnings("serial")
class ErrorFrame extends Frame
{
	Label label;
	Button button;
	String errMsg;

	ErrorFrame(String msg)
	{
		super("Error!");
		errMsg = msg;

		BorderLayout layout = new BorderLayout();
		setLayout(layout);

		label = new Label(errMsg);
		add("North", label);

		button = new Button("Ok");
		add("South", button);

		setSize(200, 100);
	}

	public boolean action(Event evt, Object arg)
	{
		if (arg == "Ok")
			dispose();
		return true;
	}
}
