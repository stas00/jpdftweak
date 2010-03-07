package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import jpdftweak.core.PageDimension;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.itextpdf.text.DocumentException;

public class PageSizeTab extends Tab {

	private JTextField scaleWidth, scaleHeight;
	private JCheckBox rotatePages, fixRotation,
		scalePages, scaleNoPreserve, scaleCenter, preserveHyperlinks;
	private JComboBox rotatePortrait, rotateLandscape, scaleSize;

	public PageSizeTab(MainForm mf) {
		super(new FormLayout("f:p, f:p:g, f:p", "f:p, f:p, f:p, 10dlu, f:p, 10dlu, f:p,f:p, f:p,f:p, f:p,f:p, 10dlu, f:p, f:p:g"));
		CellConstraints cc = new CellConstraints();
		this.add(rotatePages = new JCheckBox("Rotate pages"), cc.xyw(1, 1, 3));
		this.add(new JLabel("Portrait pages:"), cc.xy(1, 2));
		this.add(rotatePortrait = new JComboBox(new String[] {"Keep", "Right", "Upside-Down", "Left" }), cc.xyw(2, 2, 2));
		this.add(new JLabel("Landscape pages:"), cc.xy(1, 3));
		this.add(rotateLandscape = new JComboBox(new String[] {"Keep", "Right", "Upside-Down", "Left"}), cc.xyw(2, 3, 2));
		rotatePages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rotatePortrait.setEnabled(rotatePages.isSelected());
				rotateLandscape.setEnabled(rotatePages.isSelected());
				if (!rotatePages.isSelected()) {
					rotatePortrait.setSelectedIndex(0);
					rotateLandscape.setSelectedIndex(0);
				}
			}
		});
		rotatePortrait.setEnabled(false);
		rotateLandscape.setEnabled(false);
		this.add(new JSeparator(), cc.xyw(1, 4, 3));
		this.add(fixRotation = new JCheckBox("Remove implicit page rotation"), cc.xyw(1,5,3));	
		this.add(new JSeparator(), cc.xyw(1, 6, 3));
		this.add(scalePages = new JCheckBox("Scale pages"), cc.xyw(1,7,3));
		this.add(new JLabel("Page Size:"), cc.xy(1, 8));
		this.add(scaleSize = new JComboBox(), cc.xyw(2, 8, 2));
		this.add(new JLabel("Page Width:"), cc.xy(1, 9));
		this.add(scaleWidth = new JTextField(), cc.xy(2, 9));
		this.add(new JLabel("PostScript points"), cc.xy(3, 9));
		this.add(new JLabel("Page Height:"), cc.xy(1, 10));
		this.add(scaleHeight= new JTextField(), cc.xy(2, 10));
		this.add(new JLabel("PostScript points"), cc.xy(3, 10));
		this.add(scaleCenter = new JCheckBox("Center instead of enlarging"), cc.xyw(1,11,3));
		this.add(scaleNoPreserve = new JCheckBox("Do not preserve aspect ratio"), cc.xyw(1,12,3));
		scalePages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaleSize.setEnabled(scalePages.isSelected());
				scaleWidth.setEnabled(scalePages.isSelected());
				scaleHeight.setEnabled(scalePages.isSelected());
				scaleCenter.setEnabled(scalePages.isSelected());
				scaleNoPreserve.setEnabled(scalePages.isSelected());
			}
		});
		scaleSize.setEnabled(false);
		for(PageDimension ps : PageDimension.getCommonSizes()) {
			scaleSize.addItem(ps);
		}
		scaleSize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateScaleSize();
			}
		});
		scaleSize.setSelectedIndex(0);
		updateScaleSize();
		scaleWidth.setEnabled(false);
		scaleHeight.setEnabled(false);
		scaleCenter.setEnabled(false);
		scaleNoPreserve.setEnabled(false);
		this.add(new JSeparator(), cc.xyw(1, 13, 3));
		this.add(preserveHyperlinks = new JCheckBox("Preserve hyperlinks (EXPERIMENTAL)"), cc.xyw(1,14,3));	
	}
	
	protected void updateScaleSize() {
		scaleWidth.setText(""+((PageDimension)scaleSize.getSelectedItem()).getWidth());
		scaleHeight.setText(""+((PageDimension)scaleSize.getSelectedItem()).getHeight());
	}

	@Override
	public String getTabName() {
		return "Page Size";
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException {
		if (preserveHyperlinks.isSelected()) {
			tweak.preserveHyperlinks();
		}
		if (rotatePages.isSelected()) {
			tweak.rotatePages(rotatePortrait.getSelectedIndex(), rotateLandscape.getSelectedIndex());
		}
		if (fixRotation.isSelected()) {
			tweak.removeRotation();
		}
		if (scalePages.isSelected()) {
			float ww, hh;
			try {
				ww = Float.parseFloat(scaleWidth.getText());
				hh = Float.parseFloat(scaleHeight.getText());
			} catch (NumberFormatException ex) {
				throw new IOException("Invalid scale size");
			}
			tweak.scalePages(ww, hh, scaleCenter.isSelected(), !scaleNoPreserve.isSelected());
		}
		return tweak;
	}
}
