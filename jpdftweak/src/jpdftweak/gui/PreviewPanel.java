package jpdftweak.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import jpdftweak.core.PageDimension;
import jpdftweak.core.ShuffleRule;


public class PreviewPanel extends JPanel {

	private float pwidth = 1, pheight = 1;
	private ShuffleRule[] rules = new ShuffleRule[0];
	
	public void setConfig(ShuffleRule[] rules) {
		this.rules = rules;
		repaint();
	}

	public void setPageFormat(PageDimension dimension) {
		pwidth = dimension.getWidth();
		pheight = dimension.getHeight();
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setFont(new Font("Dialog", Font.PLAIN, 20));
		Graphics2D gg = (Graphics2D) g;
		int pw = getWidth()-10;
		int ph = (int)(pw * pheight / pwidth);
		int y = -ph;
		for (ShuffleRule sr : rules) {
			if (sr.isNewPageBefore()) {
				y+=ph+5;
				g.setColor(Color.WHITE);
				g.fillRect(4, y-1, pw+2, ph+2);
				g.setColor(Color.BLACK);
				g.drawRect(4, y-1, pw+2, ph+2);
			}
			AffineTransform oldTransform = gg.getTransform();
			g.translate(5, y+ph);
			// Begin transform
			gg.rotate(Math.toRadians(sr.getRotateAngle()));
			double ox = sr.getOffsetX(), oy = sr.getOffsetY();
			if (sr.isOffsetXPercent()) 
				ox = ox * pw / 100;
			else
				ox = ox * pw/pwidth;
			if (sr.isOffsetYPercent())
				oy = oy * ph /100;
			else 
				oy = oy * ph / pheight;
			gg.scale(sr.getScale(), sr.getScale());
			gg.translate(ox, -oy);
			// End transform
			g.setColor(Color.YELLOW);
			g.fillRect(0, -ph, pw, ph);
			g.setColor(Color.BLUE);
			g.drawRect(0, -ph, pw, ph);
			g.drawString(sr.getPageString(), 5, -ph+20);
			gg.setTransform(oldTransform);
		}
	}

}
