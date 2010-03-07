package jpdftweak.core;

import com.itextpdf.text.pdf.PdfBoolean;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;

public class ViewerPreference {
	
	public static final ViewerPreference[] SUPPORTED_VIEWER_PREFERENCES = {
		new ViewerPreference("Hide Toolbar", PdfName.HIDETOOLBAR, PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Hide Menubar", PdfName.HIDEMENUBAR,  PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Hide Window UI", PdfName.HIDEWINDOWUI,  PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Fit Window To PDF", PdfName.FITWINDOW, PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Center Window on screen", PdfName.CENTERWINDOW, PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Display document tile", PdfName.DISPLAYDOCTITLE, PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Page Mode after leaving fullscreen", PdfName.NONFULLSCREENPAGEMODE, PdfName.USENONE, PdfName.USEOUTLINES, PdfName.USETHUMBS, PdfName.USEOC),
		new ViewerPreference("Direction", PdfName.DIRECTION, PdfName.L2R, PdfName.R2L),
		new ViewerPreference("View area", PdfName.VIEWAREA, PdfName.MEDIABOX, PdfName.CROPBOX, PdfName.BLEEDBOX, PdfName.TRIMBOX, PdfName.ARTBOX),
		new ViewerPreference("View clip", PdfName.VIEWCLIP, PdfName.MEDIABOX, PdfName.CROPBOX, PdfName.BLEEDBOX, PdfName.TRIMBOX, PdfName.ARTBOX),
		new ViewerPreference("Print area", PdfName.PRINTAREA, PdfName.MEDIABOX, PdfName.CROPBOX, PdfName.BLEEDBOX, PdfName.TRIMBOX, PdfName.ARTBOX),
		new ViewerPreference("Print clip", PdfName.PRINTCLIP, PdfName.MEDIABOX, PdfName.CROPBOX, PdfName.BLEEDBOX, PdfName.TRIMBOX, PdfName.ARTBOX),
		new ViewerPreference("Print scaling", PdfName.PRINTSCALING, PdfName.APPDEFAULT, PdfName.NONE),
		new ViewerPreference("Duplex", PdfName.DUPLEX, PdfName.SIMPLEX, PdfName.DUPLEXFLIPSHORTEDGE, PdfName.DUPLEXFLIPLONGEDGE),
		new ViewerPreference("Pick Tray by PDF size", PdfName.PICKTRAYBYPDFSIZE, PdfBoolean.PDFTRUE, PdfBoolean.PDFFALSE),
		new ViewerPreference("Number of copies", PdfName.NUMCOPIES, new PdfNumber(1), new PdfNumber(2), new PdfNumber(3), new PdfNumber(4), new PdfNumber(5)),
	};
	
	private final String name;
	private final PdfName internalName;
	private final PdfObject[] possibleValues;

	public ViewerPreference(String name, PdfName internalName, PdfObject... possibleValues) {
		this.name = name;
		this.internalName = internalName;
		this.possibleValues = possibleValues;
	}
	
	public String getName() {
		return name;
	}
	
	public PdfName getInternalName() {
		return internalName;
	}
	
	public PdfObject[] getPossibleValues() {
		return possibleValues;
	}
}
