package jpdftweak.cli;

import java.io.IOException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;
import jpdftweak.core.PdfTweak.PageBox;

import com.itextpdf.text.DocumentException;

public class CropOption implements CommandOption {

	private PdfTweak.PageBox cropTo;
	
	public boolean supportsOption(String option) {
		return option.equals("-crop");
	}
	
	public boolean setOption(String option, String value) {
		if (cropTo != null) {
			System.err.println("Error: more than one -crop option used.");
			return false;
		} 
		try {
			cropTo = PageBox.valueOf(value);
			return true;
		} catch (IllegalArgumentException e){
			System.err.println("Unknown crop box: "+value);
			return false;
		}
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile) throws DocumentException, IOException {
		if (cropTo != null)
			tweak.cropPages(cropTo);
	}

	public String getSummary() {
		return 
			" -crop                   Crop pages to the given page box.\n";
	}
	
	public String getHelp(String option) {

			return
				" -crop {BOXNAME}\n"+
				"    Crop all pages to the given page box.\n"+
				"    Valid box names are:\n"+
				"    MediaBox, CropBox, BleedBox, TrimBox, ArtBox";
	}
}
