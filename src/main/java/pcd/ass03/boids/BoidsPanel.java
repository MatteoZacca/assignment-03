package pcd.ass03.boids;

import pcd.ass03.boids.BoidsProtocol.BoidState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BoidsPanel extends JPanel {

    private List<BoidState> currentStates;
	private BoidsView view;
	private BoidsModel model;
    private int framerate;

    public BoidsPanel(BoidsView view, BoidsModel model) {
    	this.currentStates = new ArrayList<>();
        this.model = model;
    	this.view = view;
    }

    public void updateFrame(List<BoidState> states, int framerate) {
        this.currentStates = states;
        this.framerate = framerate;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.WHITE);
        
        var w = view.getWidth();
        var h = view.getHeight();
        var envWidth = model.getWidth();
        var xScale = w/envWidth;

        g.setColor(Color.BLUE);

        for (BoidState boid : currentStates) {
        	var x = boid.pos().x();
        	var y = boid.pos().y();
        	int px = (int)(w/2 + x*xScale);
        	int py = (int)(h/2 - y*xScale);
            g.fillOval(px,py, 5, 5);
        }
        
        g.setColor(Color.BLACK);
        g.drawString("Num. Boids: " + currentStates.size(), 10, 25);
        g.drawString("Framerate: " + framerate, 10, 40);
   }
}
