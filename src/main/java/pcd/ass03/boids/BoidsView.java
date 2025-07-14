package pcd.ass03.boids;

import pcd.ass03.boids.BoidsProtocol.*;

import akka.actor.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;

public class BoidsView implements ChangeListener {

	private JFrame frame;
	private BoidsPanel boidsPanel;
	private BoidsModel model;
	private JSlider cohesionSlider, separationSlider, alignmentSlider;
	private JTextField nBoidsTextFields;
	private int width, height;
	private int nStartingBoids;
	private boolean modelPaused;
	private ActorRef masterActor;
	
	public BoidsView(BoidsModel model, int width, int height, int nBoids) {
		this.model = model;
		this.width = width;
		this.height = height;
		this.nStartingBoids = nBoids;
		this.modelPaused = true;
		
		this.frame = new JFrame("Boids Simulation");
        this.frame.setSize(width, height);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);

        this.boidsPanel = new BoidsPanel(this, this.model);
		cp.add(BorderLayout.CENTER, this.boidsPanel);

        JPanel slidersPanel = new JPanel();

		slidersPanel.setLayout(new GridLayout(2, 2));

		this.separationSlider = makeSlider();
        this.alignmentSlider = makeSlider();
		this.cohesionSlider = makeSlider();
		this.nBoidsTextFields = makeTextField();
        
        slidersPanel.add(new JLabel("Separation"));
        slidersPanel.add(separationSlider);
        slidersPanel.add(new JLabel("Alignment"));
        slidersPanel.add(alignmentSlider);
        slidersPanel.add(new JLabel("Cohesion"));
        slidersPanel.add(cohesionSlider);
		slidersPanel.add(new JLabel("N°. Boids"));
		slidersPanel.add(nBoidsTextFields);

		cp.add(BorderLayout.SOUTH, slidersPanel);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 2));

		JButton toggleSimulation = new JButton("Play");
		JButton resetSimulation = new JButton("Reset");
		buttonPanel.add(toggleSimulation);
		buttonPanel.add(resetSimulation);

		// Listener Play/Pause button
		toggleSimulation.addActionListener((e) -> {
			if (isModelPaused()) {
				log("Pressed Play!");
				toggleSimulation.setText("Pause");
				resetSimulation.setEnabled(false);
				masterActor.tell(new StartSimulationMsg(), ActorRef.noSender());
			} else if (!isModelPaused()) {
				log("Pressed Pause!");
				toggleSimulation.setText("Play");
				resetSimulation.setEnabled(true);
				masterActor.tell(new PauseSimulationMsg(), ActorRef.noSender());
			}
			setStatusSimulation(!isModelPaused());
		});

		// Listener Reset button
		resetSimulation.addActionListener((e) -> {
			log("Pressed Reset!");
			setStatusSimulation(true);
			toggleSimulation.setText("Play");
			resetSimulation.setEnabled(false);
			masterActor.tell(new ResetSimulationMsg(this.nStartingBoids), ActorRef.noSender());
		});

		// Listener Boids textField
		nBoidsTextFields.addActionListener((e) -> {
			String text = nBoidsTextFields.getText();
			if (isNumber(text)) {
				nBoidsTextFields.setForeground(Color.GREEN);
				this.nStartingBoids = Integer.parseInt(text);
				log("Changed boids number: " + this.nStartingBoids);
				setStatusSimulation(true);
				toggleSimulation.setText("Play");
				resetSimulation.setEnabled(false);
				masterActor.tell(new ResetSimulationMsg(this.nStartingBoids), ActorRef.noSender());
			} else {
				nBoidsTextFields.setForeground(Color.RED);
				nBoidsTextFields.setText("Please enter an integer.");
			}
		});

		cp.add(BorderLayout.NORTH, buttonPanel);

		frame.setContentPane(cp);	
		
        frame.setVisible(true);
	}

	public void update(int frameRate) {
		boidsPanel.setFrameRate(frameRate);
		boidsPanel.repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == separationSlider) {
			var weight = separationSlider.getValue();
			log("Separation Weight: " + String.format("%.1f", 0.1 * weight));
			masterActor.tell(new UpdateSeparationWeightMsg(0.1 * weight), ActorRef.noSender());
			//model.setSeparationWeight(0.1*val);
		} else if (e.getSource() == cohesionSlider) {
			var weight = cohesionSlider.getValue();
			log("Cohesion Weight: " + String.format("%.1f", 0.1 * weight));
			masterActor.tell(new UpdateCohesionWeightMsg(0.1 * weight), ActorRef.noSender());
			//model.setCohesionWeight(0.1*val);
		} else if (e.getSource() == alignmentSlider) {
			var weight = alignmentSlider.getValue();
			log("Alignment Weight: " + String.format("%.1f", 0.1 * weight));
			masterActor.tell(new UpdateAlignmentWeightMsg(0.1 * weight), ActorRef.noSender());
			//model.setAlignmentWeight(0.1*val);
		}
	}

	public void setMasterActor(ActorRef masterActor) {
		this.masterActor = masterActor;
	}

	private void setStatusSimulation(boolean status) {
		this.modelPaused = status;
	}

	private boolean isModelPaused() {
		return this.modelPaused;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private JSlider makeSlider() {
		var slider = new JSlider(JSlider.HORIZONTAL, 1, 20, 10);
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		Hashtable labelTable = new Hashtable<>();
		labelTable.put(1, new JLabel("0.1"));
		labelTable.put(10, new JLabel("1.0"));
		labelTable.put(20, new JLabel("2.0"));
		slider.setLabelTable( labelTable );
		slider.setPaintLabels(true);
        slider.addChangeListener(this);

		return slider;
	}

	private JTextField makeTextField() {
		this.nBoidsTextFields = new JTextField(String.valueOf(this.nStartingBoids));
		this.nBoidsTextFields.setForeground(Color.BLUE);

		return this.nBoidsTextFields;
	}

	private boolean isNumber(String text) {
		try {
			Integer.parseInt(text);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	private static void log(String msg) {
		System.out.println("[" + Thread.currentThread().getName() + "]: " + msg);
	}

}
