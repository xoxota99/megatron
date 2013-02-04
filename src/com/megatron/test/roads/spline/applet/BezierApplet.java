package com.megatron.test.roads.spline.applet;
/*
bezier.java         by Gengbin Zheng
 */

import java.applet.Applet;
import java.awt.Button;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;

@SuppressWarnings("serial")
public class BezierApplet extends Applet {
	Button draw1Button, draw2Button, modifyButton, deleteButton, clearButton;
	BezierCanvas canvas;
	TextField statusBar;

	public void init() {
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);

		GridBagConstraints constraints = new GridBagConstraints();

		draw1Button = new Button("Draw Bezier");
		draw2Button = new Button("Draw B-Spline");
		modifyButton = new Button("Modify");
		deleteButton = new Button("Delete curve");
		clearButton = new Button("Clear All");

		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 1;
		layout.setConstraints(draw1Button, constraints);
		add(draw1Button);

		layout.setConstraints(draw2Button, constraints);
		add(draw2Button);

		layout.setConstraints(modifyButton, constraints);
		add(modifyButton);

		constraints.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(deleteButton, constraints);
		add(deleteButton);

		constraints.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(clearButton, constraints);
		add(clearButton);

		canvas = new BezierCanvas();
		constraints.weighty = 1;
		layout.setConstraints(canvas, constraints);
		add(canvas);

		statusBar = new TextField("Draw Bezier: click to add a point, double click to finish drawing", 45);
		statusBar.setEditable(false);

		constraints.weighty = 0;
		layout.setConstraints(statusBar, constraints);
		add(statusBar);

		resize(550, 450); // Set window size
	}

	public boolean action(Event evt, Object arg)
	{
		if (evt.target instanceof Button)
		{
			HandleButtons(arg);
		}
		return true;
	}

	protected void HandleButtons(Object label)
	{
		String helpMsg;

		if (label == "Clear All")
			helpMsg = "All curves are cleared.";
		else if (label == "Draw Bezier")
			helpMsg = "Draw Bezier: click to add a point, double click to finish drawing";
		else if (label == "Draw B-Spline")
			helpMsg = "Draw B-Spline: click to add a point, double click to finish drawing.";
		else if (label == "Modify")
			helpMsg = "Modify: select a control point, drag mouse to modify and release to finish.";
		else if (label == "Delete curve")
			helpMsg = "Delete: select a curve, click to delete.";
		else
			helpMsg = "";

		statusBar.setText(helpMsg);

		canvas.HandleButtons(label);
	}
}
