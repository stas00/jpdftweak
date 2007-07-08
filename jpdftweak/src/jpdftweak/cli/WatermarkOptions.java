package jpdftweak.cli;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfTweak;

import com.lowagie.text.DocumentException;

public class WatermarkOptions implements CommandOption {
	
	private String background = null;
	
	private String text = null;
	private int textsize;
	private float textopacity;
	
	private int position = -1;
	private int nsize;
	private float xoff, yoff;
	
	public boolean supportsOption(String option) {
		return option.equals("-background") || option.equals("-watermark") ||  option.equals("-numbers");
	}
	
	public boolean setOption(String option, String value) {
		if (option.equals("-background")) {
			background = value;
		}
		if (option.equals("-watermark")) {
			Matcher m = Pattern.compile("(.*),([0-9]+),([0-9.]+)").matcher(value);
			if (!m.matches()) {
				System.err.println("Error: Invalid parameter for -watermark option");
				return false;
			}
			text = m.group(1);
			textsize = Integer.parseInt(m.group(2));
			textopacity = Float.parseFloat(m.group(3));
		}
		if (option.equals("-numbers")) {
			Matcher m = Pattern.compile("([LRTBC]|[LR][TB]),([0-9]+),(-?[0-9]+),(-?[0-9]+)").matcher(value);
			if (!m.matches()) {
				System.err.println("Error: Invalid parameter for -numbers option");
				return false;	
			}
			position = Arrays.asList(new String[] {
					"LB", "B", "RB",
					"L", "C", "R",
					"LT", "T", "RT"
			}).indexOf(m.group(1));
			if (position == -1) {
				throw new RuntimeException(m.group(1));
			}
			nsize = Integer.parseInt(m.group(2));
			xoff = Integer.parseInt(m.group(3));
			yoff = Integer.parseInt(m.group(4));
		}
		return true;
	}

	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (background != null || text != null || position != -1) {
			tweak.addWatermark(background == null ? null : new PdfInputFile(new File(background),""), 
					text, textsize, textopacity, position, nsize, xoff, yoff);
		}		
	}
	
	public String getSummary() {
		return 
			" -background             Add file as background watermark\n"+
			" -watermark              Add text watermark\n"+
			" -numbers                Add page numbers\n";
	}
	
	public String getHelp(String option) {
		if (option.equals("-background")) {
			return
				" -background {FILENAME}\n"+
				"    Add first page of file {FILENAME} as background watermark.\n" +
				"    This watermark file may not be password protected.";

		} else if (option.equals("-watermark")) {
			return
				" -watermark {TEXT},{FONTSIZE},{OPACITY}\n"+
				"    Add a text watermark. {OPACITY} is a float between 0 and 1,\n" +
				"    for example 0.25.";
		} else if (option.equals("-numbers")) {
			return
				" -numbers {POSITION},{FONTSIZE},{XOFFSET},{YOFFSET}\n"+
				"    Add page numbers. {XOFFSET} and {YOFFSET} are in PostScript\n" +
				"    points. {POSITION} may be one of:\n\n" +
				"        L left edge\n" +
				"        R right edge\n" +
				"        T top edge\n" +
				"        B bottom edge\n" +
				"        LB left bottom corner\n" +
				"        RB right bottom corner\n" +
				"        LT left top corner\n" +
				"        RT right top corner\n" +
				"        C center of page";
		}
		throw new RuntimeException();
	}
}
