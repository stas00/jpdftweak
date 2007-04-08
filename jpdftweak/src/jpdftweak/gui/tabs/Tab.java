package jpdftweak.gui.tabs;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.io.IOException;
import java.lang.annotation.Documented;

import javax.swing.JPanel;

import com.lowagie.text.DocumentException;

import jpdftweak.core.PdfTweak;

public abstract class Tab extends JPanel {
	
	public Tab(LayoutManager layout) {
		super(layout);
	}
	public abstract String getTabName();
	public void checkRun() throws IOException {}
	public abstract PdfTweak run(PdfTweak input) throws IOException, DocumentException;
}
