package jpdftweak.gui.tabs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import jpdftweak.core.PdfBookmark;
import jpdftweak.core.PdfTweak;
import jpdftweak.gui.MainForm;
import jpdftweak.gui.TableComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.lowagie.text.DocumentException;

public class BookmarkTab extends Tab {

	private JButton load;
	//private JButton importPdf, importXml, exportXml, filter;
	private TableComponent bookmarks;
	private JCheckBox changeBookmarks;
	private final MainForm mainForm;
	
	public BookmarkTab(MainForm mf) {
		super(new FormLayout("f:p:g, f:p", "f:p, f:p, f:p:g"));
		this.mainForm = mf;
		CellConstraints cc = new CellConstraints();
		add(changeBookmarks = new JCheckBox("Change chapter bookmarks"), cc.xy(1, 1));
		changeBookmarks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEnabledState();
			}
		});
		add(load = new JButton("Load from document"), cc.xy(2, 1));
		load.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PdfBookmark> bm = mainForm.getInputTab().loadBookmarks();
				bookmarks.clear();
				appendBookmarks(bm);
			}
		});
		// TODO bookmark buttons
		//JPanel panel = new JPanel(new GridLayout(1, 4));
		//panel.add(importPdf =new JButton("Import from PDF"));
		//panel.add(importXml =new JButton("Import from CSV"));
		//panel.add(exportXml =new JButton("Export to CSV"));
		//panel.add(filter = new JButton("Filter/Shift"));
		//add(panel, cc.xyw(1, 2, 2));
		add(bookmarks = new TableComponent(new String[] {"Depth", "Open",  "Title", "Page", "Position", "Bold", "Italic", "Options"},
				new Class[]{ Integer.class, Boolean.class, String.class, Integer.class, String.class, Boolean.class, Boolean.class, String.class},
				new Object[]{1, false, "", 1, "", false, false, ""}), cc.xyw(1, 3, 2));
		updateEnabledState();
	}

	protected void appendBookmarks(List<PdfBookmark> bm) {
		for (PdfBookmark b : bm) {
			 bookmarks.addRow(b.getDepth(), b.isOpen(), b.getTitle(), b.getPage(), b.getPagePosition(), b.isBold(), b.isItalic(), b.getMoreOptions());
		}
	}
	
	protected PdfBookmark getBookmark(Object[] row) {
		int depth = (Integer)row[0];
		boolean open = (Boolean)row[1];
		String title = (String)row[2];
		int page = (Integer)row[3];
		String pagePosition = (String)row[4];
		boolean bold = (Boolean)row[5];
		boolean italic = (Boolean)row[6];
		String moreOptions = (String)row[7];
		return new PdfBookmark(depth, title, open, page, pagePosition, bold, italic, moreOptions);
	}

	protected void updateEnabledState() {
		load.setEnabled(changeBookmarks.isSelected());
		//importPdf.setEnabled(changeBookmarks.isSelected());
		//importXml.setEnabled(changeBookmarks.isSelected());
		//exportXml.setEnabled(changeBookmarks.isSelected());
		//filter.setEnabled(changeBookmarks.isSelected());
		bookmarks.setEnabled(changeBookmarks.isSelected());
	}

	@Override
	public String getTabName() {
		return "Bookmarks";
	}

	@Override
	public PdfTweak run(PdfTweak tweak) throws IOException, DocumentException {
		if (changeBookmarks.isSelected()) {
			PdfBookmark[] bm = new PdfBookmark[bookmarks.getRowCount()];
			for (int i = 0; i < bm.length; i++) {
				bm[i] = getBookmark(bookmarks.getRow(i));
			}
			tweak.updateBookmarks(bm);
		}
		return tweak;
	}

}
