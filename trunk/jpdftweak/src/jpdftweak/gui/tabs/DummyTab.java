package jpdftweak.gui.tabs;

import java.awt.BorderLayout;

import javax.swing.JLabel;

import jpdftweak.core.PdfTweak;

public class DummyTab extends Tab {

	private final String name;

	public DummyTab(String name) {
		super(new BorderLayout());
		this.name = name;
		add(BorderLayout.CENTER, new JLabel("Not implemented yet."));
	}
	
	@Override
	public String getTabName() {
		return name;
	}

	@Override
	public PdfTweak run(PdfTweak input) {
		return input;
	}

}
