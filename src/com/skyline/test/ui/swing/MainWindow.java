package com.skyline.test.ui.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;

import com.oddlabs.procedurality.*;
import com.oddlabs.procedurality.Terrain.*;
import com.skyline.model.*;
import com.skyline.terrain.*;

public class MainWindow {

	private JFrame frame;
	// private worldState worldState;
	private WorldState worldState;
	private JPanel renderTarget;
	private String nextPipelineStep = "terrain";
	private BufferedImage imgMap;
	private int[] palette = {};

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initModel();
		initView();
	}

	private void initModel() {
		worldState = new WorldState();
		ChannelFactory cf;
		cf = new Mountain(worldState.getSize(), Utils.powerOf2Log2(worldState.getSize()) - 6, 0.5f, worldState.getTerrainSeed());
		 cf = new Perlin(worldState.getSize(),worldState.getSize(),1,5,0.4f,10,worldState.getSeed(),Perlin.CUBIC,Perlin.WOOD2);
		// cf = new Hill(worldState.getSize(), Hill.CIRCLE);
		worldState.setTerrainHeightMap(Terrain.createRandomHeightMap(worldState.getSize(), 64f, worldState.getTerrainSeed()));
	}

	private void updateView() {
		if (renderTarget != null) {
			Graphics2D g2d = (Graphics2D) renderTarget.getGraphics();
			Graphics imgGfx = imgMap.getGraphics();
			imgGfx.setColor(new Color(0x001133));
			imgGfx.fillRect(0, 0, worldState.getSize(), worldState.getSize());
			float[] data = worldState.getTerrainHeightMap().getHeightMap();
			for (int x = 0; x < worldState.getSize(); x++) {
				for (int z = 0; z < worldState.getSize(); z++) {
					float height = data[z * worldState.getSize() + x];
					int val = (int) (0xFF * height);

					int rgb = (val << 16)
							| (val << 8)
							| (val);
					// RoadEngine.out.println("redrawing");
					imgMap.setRGB(x, z, rgb);
				}
			}
			g2d.drawImage(imgMap, 0, 0, renderTarget.getWidth(), renderTarget.getHeight(), new ImageObserver() {

				@Override
				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
					// TODO Auto-generated method stub
					renderTarget.repaint();
					return true;
				}

			});
		}
	}

	// private void executePipelineStep() {
	// Command cmd = Globals.catalog.getCommand(nextPipelineStep);
	// if (cmd != null) {
	// try {
	// if (cmd.execute(worldState)) {
	// // unlock the next pipeline step.
	// RoadEngine.out.println("Hey, we're done with '" + nextPipelineStep +
	// "'. What comes next?");
	// updateView(worldState);
	// }
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }

	/**
	 * Initialize the contents of the frame.
	 */
	private void initView() {
		imgMap = new BufferedImage(worldState.getSize(), worldState.getSize(), BufferedImage.TYPE_INT_RGB);

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		frame.getContentPane().setLayout(gridBagLayout);

		JPanel pnlLeft = new JPanel();
		GridBagConstraints gbc_pnlLeft = new GridBagConstraints();
		gbc_pnlLeft.gridheight = 2;
		gbc_pnlLeft.fill = GridBagConstraints.BOTH;
		gbc_pnlLeft.gridx = 0;
		gbc_pnlLeft.gridy = 0;
		frame.getContentPane().add(pnlLeft, gbc_pnlLeft);
		GridBagLayout gbl_pnlLeft = new GridBagLayout();
		gbl_pnlLeft.columnWidths = new int[] { 179, 0 };
		gbl_pnlLeft.rowHeights = new int[] { 270, 0, 0, 0 };
		gbl_pnlLeft.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_pnlLeft.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		pnlLeft.setLayout(gbl_pnlLeft);

		JPanel pnlTopLeft = new JPanel();
		pnlTopLeft.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		pnlTopLeft.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagLayout gbl_pnlTopLeft = new GridBagLayout();
		gbl_pnlTopLeft.columnWidths = new int[] { 177, 0 };
		gbl_pnlTopLeft.rowHeights = new int[] { 19, 19, 19, 19, 19, 19, 19, 19, 19, 0 };
		gbl_pnlTopLeft.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_pnlTopLeft.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 };
		pnlTopLeft.setLayout(gbl_pnlTopLeft);

		ActionListener actionPipelineCommandSetter = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JRadioButton) { // bad
					JRadioButton btn = (JRadioButton) e.getSource();
					nextPipelineStep = btn.getActionCommand();
					System.out.println("Next pipeline Step is " + nextPipelineStep);
				}
			}
		};

		JRadioButton rdbtnTerrain = new JRadioButton(Messages.getString("MainWindow.rdbtnTerrain.text")); //$NON-NLS-1$
		rdbtnTerrain.setActionCommand("terrain");
		rdbtnTerrain.addActionListener(actionPipelineCommandSetter);
		rdbtnTerrain.setSelected(true);
		GridBagConstraints gbc_rdbtnTerrain = new GridBagConstraints();
		gbc_rdbtnTerrain.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnTerrain.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnTerrain.gridx = 0;
		gbc_rdbtnTerrain.gridy = 0;
		pnlTopLeft.add(rdbtnTerrain, gbc_rdbtnTerrain);

		JRadioButton rdbtnPopDensity = new JRadioButton(Messages.getString("MainWindow.rdbtnPopDensity.text")); //$NON-NLS-1$
		rdbtnPopDensity.setActionCommand("population");
		rdbtnPopDensity.addActionListener(actionPipelineCommandSetter);
		rdbtnPopDensity.setEnabled(false);
		GridBagConstraints gbc_rdbtnPopDensity = new GridBagConstraints();
		gbc_rdbtnPopDensity.anchor = GridBagConstraints.WEST;
		gbc_rdbtnPopDensity.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnPopDensity.gridx = 0;
		gbc_rdbtnPopDensity.gridy = 1;
		pnlTopLeft.add(rdbtnPopDensity, gbc_rdbtnPopDensity);

		JRadioButton rdbtnHighways = new JRadioButton(Messages.getString("MainWindow.rdbtnHighways.text")); //$NON-NLS-1$
		rdbtnHighways.setActionCommand("highways");
		rdbtnHighways.addActionListener(actionPipelineCommandSetter);
		rdbtnHighways.setEnabled(false);
		GridBagConstraints gbc_rdbtnHighways = new GridBagConstraints();
		gbc_rdbtnHighways.anchor = GridBagConstraints.WEST;
		gbc_rdbtnHighways.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnHighways.gridx = 0;
		gbc_rdbtnHighways.gridy = 2;
		pnlTopLeft.add(rdbtnHighways, gbc_rdbtnHighways);

		JRadioButton rdbtnRoads = new JRadioButton(Messages.getString("MainWindow.rdbtnRoads.text")); //$NON-NLS-1$
		rdbtnRoads.setActionCommand("roads");
		rdbtnRoads.addActionListener(actionPipelineCommandSetter);
		rdbtnRoads.setEnabled(false);
		GridBagConstraints gbc_rdbtnRoads = new GridBagConstraints();
		gbc_rdbtnRoads.anchor = GridBagConstraints.WEST;
		gbc_rdbtnRoads.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnRoads.gridx = 0;
		gbc_rdbtnRoads.gridy = 3;
		pnlTopLeft.add(rdbtnRoads, gbc_rdbtnRoads);

		JRadioButton rdbtnStreets = new JRadioButton(Messages.getString("MainWindow.rdbtnStreets.text")); //$NON-NLS-1$
		rdbtnStreets.setActionCommand("streets");
		rdbtnStreets.addActionListener(actionPipelineCommandSetter);
		rdbtnStreets.setEnabled(false);
		GridBagConstraints gbc_rdbtnStreets = new GridBagConstraints();
		gbc_rdbtnStreets.anchor = GridBagConstraints.WEST;
		gbc_rdbtnStreets.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnStreets.gridx = 0;
		gbc_rdbtnStreets.gridy = 4;
		pnlTopLeft.add(rdbtnStreets, gbc_rdbtnStreets);

		JRadioButton rdbtnBlocks = new JRadioButton(Messages.getString("MainWindow.rdbtnBlocks.text")); //$NON-NLS-1$
		rdbtnBlocks.setActionCommand("blocks");
		rdbtnBlocks.addActionListener(actionPipelineCommandSetter);
		rdbtnBlocks.setEnabled(false);
		GridBagConstraints gbc_rdbtnBlocks = new GridBagConstraints();
		gbc_rdbtnBlocks.anchor = GridBagConstraints.WEST;
		gbc_rdbtnBlocks.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnBlocks.gridx = 0;
		gbc_rdbtnBlocks.gridy = 5;
		pnlTopLeft.add(rdbtnBlocks, gbc_rdbtnBlocks);

		JRadioButton rdbtnLots = new JRadioButton(Messages.getString("MainWindow.rdbtnLots.text")); //$NON-NLS-1$
		rdbtnLots.setActionCommand("lots");
		rdbtnLots.addActionListener(actionPipelineCommandSetter);
		rdbtnLots.setEnabled(false);
		GridBagConstraints gbc_rdbtnLots = new GridBagConstraints();
		gbc_rdbtnLots.anchor = GridBagConstraints.WEST;
		gbc_rdbtnLots.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnLots.gridx = 0;
		gbc_rdbtnLots.gridy = 6;
		pnlTopLeft.add(rdbtnLots, gbc_rdbtnLots);

		JRadioButton rdbtnZoning = new JRadioButton(Messages.getString("MainWindow.rdbtnZoning.text")); //$NON-NLS-1$
		rdbtnZoning.setActionCommand("zoning");
		rdbtnZoning.addActionListener(actionPipelineCommandSetter);
		rdbtnZoning.setEnabled(false);
		GridBagConstraints gbc_rdbtnZoning = new GridBagConstraints();
		gbc_rdbtnZoning.anchor = GridBagConstraints.WEST;
		gbc_rdbtnZoning.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnZoning.gridx = 0;
		gbc_rdbtnZoning.gridy = 7;
		pnlTopLeft.add(rdbtnZoning, gbc_rdbtnZoning);

		JRadioButton rdbtnBuildings = new JRadioButton(Messages.getString("MainWindow.rdbtnBuildings.text")); //$NON-NLS-1$
		rdbtnBuildings.setActionCommand("buildings");
		rdbtnBuildings.addActionListener(actionPipelineCommandSetter);
		rdbtnBuildings.setEnabled(false);
		GridBagConstraints gbc_rdbtnBuildings = new GridBagConstraints();
		gbc_rdbtnBuildings.anchor = GridBagConstraints.WEST;
		gbc_rdbtnBuildings.insets = new Insets(0, 0, 5, 0);
		gbc_rdbtnBuildings.gridx = 0;
		gbc_rdbtnBuildings.gridy = 8;
		pnlTopLeft.add(rdbtnBuildings, gbc_rdbtnBuildings);
		GridBagConstraints gbc_pnlTopLeft = new GridBagConstraints();
		gbc_pnlTopLeft.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTopLeft.anchor = GridBagConstraints.NORTHWEST;
		gbc_pnlTopLeft.gridx = 0;
		gbc_pnlTopLeft.gridy = 0;
		pnlLeft.add(pnlTopLeft, gbc_pnlTopLeft);

		JPanel pnlBottomLeft = new JPanel();
		pnlBottomLeft.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		pnlBottomLeft.setAlignmentX(Component.LEFT_ALIGNMENT);
		GridBagLayout gbl_pnlBottomLeft = new GridBagLayout();
		gbl_pnlBottomLeft.columnWidths = new int[] { 177, 0 };
		gbl_pnlBottomLeft.rowHeights = new int[] { 29, 29, 29, 29, 0 };
		gbl_pnlBottomLeft.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_pnlBottomLeft.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		pnlBottomLeft.setLayout(gbl_pnlBottomLeft);

		JButton btnClear = new JButton(Messages.getString("MainWindow.btnClear.text"));
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.fill = GridBagConstraints.BOTH;
		gbc_btnClear.insets = new Insets(0, 0, 5, 0);
		gbc_btnClear.gridx = 0;
		gbc_btnClear.gridy = 0;
		pnlBottomLeft.add(btnClear, gbc_btnClear);

		JButton btnGenerate = new JButton(Messages.getString("MainWindow.btnGenerate.text")); //$NON-NLS-1$
		btnGenerate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// executePipelineStep();
			}
		});
		GridBagConstraints gbc_btnGenerate = new GridBagConstraints();
		gbc_btnGenerate.fill = GridBagConstraints.BOTH;
		gbc_btnGenerate.insets = new Insets(0, 0, 5, 0);
		gbc_btnGenerate.gridx = 0;
		gbc_btnGenerate.gridy = 1;
		pnlBottomLeft.add(btnGenerate, gbc_btnGenerate);

		JButton btnGenerateNext = new JButton(Messages.getString("MainWindow.btnGenerateNext.text")); //$NON-NLS-1$
		GridBagConstraints gbc_btnGenerateNext = new GridBagConstraints();
		gbc_btnGenerateNext.fill = GridBagConstraints.BOTH;
		gbc_btnGenerateNext.insets = new Insets(0, 0, 5, 0);
		gbc_btnGenerateNext.gridx = 0;
		gbc_btnGenerateNext.gridy = 2;
		pnlBottomLeft.add(btnGenerateNext, gbc_btnGenerateNext);

		JButton btnGenerateAll = new JButton(Messages.getString("MainWindow.btnGenerateAll.text")); //$NON-NLS-1$
		GridBagConstraints gbc_btnGenerateAll = new GridBagConstraints();
		gbc_btnGenerateAll.fill = GridBagConstraints.BOTH;
		gbc_btnGenerateAll.gridx = 0;
		gbc_btnGenerateAll.gridy = 3;
		pnlBottomLeft.add(btnGenerateAll, gbc_btnGenerateAll);
		GridBagConstraints gbc_pnlBottomLeft = new GridBagConstraints();
		gbc_pnlBottomLeft.anchor = GridBagConstraints.SOUTHWEST;
		gbc_pnlBottomLeft.gridx = 0;
		gbc_pnlBottomLeft.gridy = 2;
		pnlLeft.add(pnlBottomLeft, gbc_pnlBottomLeft);

		Component verticalGlue = Box.createVerticalGlue();
		GridBagConstraints gbc_verticalGlue = new GridBagConstraints();
		gbc_verticalGlue.insets = new Insets(0, 0, 5, 0);
		gbc_verticalGlue.gridx = 0;
		gbc_verticalGlue.gridy = 1;
		pnlLeft.add(verticalGlue, gbc_verticalGlue);

		JPanel pnlRight = new JPanel();
		pnlRight.setBackground(Color.DARK_GRAY);
		GridBagConstraints gbc_pnlRight = new GridBagConstraints();
		gbc_pnlRight.fill = GridBagConstraints.BOTH;
		gbc_pnlRight.gridx = 1;
		gbc_pnlRight.gridy = 0;
		frame.getContentPane().add(pnlRight, gbc_pnlRight);

		this.renderTarget = pnlRight;
		pnlRight.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("renderTarget hidden");

			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("renderTarget moved");

			}

			@Override
			public void componentResized(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("renderTarget resized");
				updateView();
			}

			@Override
			public void componentShown(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				System.out.println("renderTarget shown");
				updateView();
			}
		});
	}

}
