package pcd.ass03;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.util.Hashtable;

public class BoidsView implements ChangeListener {

	private JFrame frame;
	private BoidsPanel boidsPanel;
	private JSlider cohesionSlider, separationSlider, alignmentSlider, boidSlider;
	private BoidsModel model;
	private int width, height;
	
	public BoidsView(BoidsModel model, int width, int height) {
		this.model = model;
		this.width = width;
		this.height = height;
		
		frame = new JFrame("Boids Simulation");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);

        boidsPanel = new BoidsPanel(this, model);
		cp.add(BorderLayout.CENTER, boidsPanel);

        JPanel slidersPanel = new JPanel();

		slidersPanel.setLayout(new GridLayout(2, 2));
        
        cohesionSlider = makeSlider();
        separationSlider = makeSlider();
        alignmentSlider = makeSlider();
		boidSlider = makeBoidSlider();
        
        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);
		slidersPanel.add(new JLabel("N°. Boids"));
		slidersPanel.add(boidSlider);

		cp.add(BorderLayout.SOUTH, slidersPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));

		JButton toggleSimulation = new JButton("Play");
		toggleSimulation.addActionListener((e) -> {
			model.setStatusSimulation(!model.isModelPaused());
			toggleSimulation.setText(model.isModelPaused() ? "Play" : "Pause");
		});

		buttonPanel.add(toggleSimulation);

		JButton startStopSimulation = new JButton("Reset");
		startStopSimulation.addActionListener((e) -> {
			model.setStatusSimulation(true);
			toggleSimulation.setText("Play");
			model.generateBoids(model.getBoids().size());
		} );

		buttonPanel.add(startStopSimulation);
		        
		cp.add(BorderLayout.NORTH, buttonPanel);

		frame.setContentPane(cp);	
		
        frame.setVisible(true);
	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20, 10);        
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("0") );
		labelTable.put( 10, new JLabel("1") );
		labelTable.put( 20, new JLabel("2") );
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
        slider.addChangeListener(this);
		return slider;
	}

	private JSlider makeBoidSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 0, 20,0);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put( 0, new JLabel("1500"));
		labelTable.put( 10, new JLabel(String.valueOf(6 * BoidsSimulation.N_BOIDS)));
		labelTable.put( 20, new JLabel(String.valueOf(11 * BoidsSimulation.N_BOIDS)));
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
		slider.addChangeListener(this);
		return slider;
	}
	
	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var val = separationSlider.getValue();
			model.setSeparationWeight(0.1*val);
		} else if (e.getSource() == cohesionSlider) {
			var val = cohesionSlider.getValue();
			model.setCohesionWeight(0.1*val);
		} else if (e.getSource() == alignmentSlider) {
			var val = alignmentSlider.getValue();
			model.setAlignmentWeight(0.1*val);
		} else {
			var val = boidSlider.getValue();
			model.setBoidCount(BoidsSimulation.N_BOIDS + val * BoidsSimulation.N_BOIDS);
		}
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

}
