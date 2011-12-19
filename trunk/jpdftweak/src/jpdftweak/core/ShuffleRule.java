package jpdftweak.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShuffleRule {
	
	private final boolean newPageBefore;
	private final PageBase pageBase;
	private final int pageNumber;
	private final char rotate;
	private final double scale;
	private final double offsetX;
	private final boolean offsetXPercent;
	private final double offsetY;
	private final boolean offsetYPercent;
	private final double frameWidth;

	public static enum PageBase {
		ABSOLUTE, BEGINNING, END
	}
	
	public static final String[] predefinedRuleSets = new String[] {
		"1:!+1N1+0%+0%=Default",
		"1:!-1N1+0%+0%=Reverse Page Order",
		"2:!+1L0.707+0%-100%,+2L.707+100%-100%=2-up Portrait",
		"2:!+1L0.707+0%-100%,+2L.707+0%-200%=2-up Landscape",
		"4:!+1N0.5+0%+100%,+2N0.5+100%+100%,+3N0.5+0%+0%,+4N0.5+100%+0%,=4-up",
		"9:!+1N.333+0%+200%,+2N.333+100%+200%,+3N.333+200%+200%,+4N.333+0%+100%,+5N.333+100%+100%,+6N.333+200%+100%,+7N.333+0%+0%,+8N.333+100%+0%,+9N.333+200%+0%,=9-up",
		"-4:!-4N1+0%+0%,!+1N1+0%+0%,!+2N1+0%+0%,!-3N1+0%+0%=Booklet Reorder",
		"-4:!-4L0.707+0%-100%,+1L.707+100%-100%,!+2L0.707+0%-100%,-3L.707+100%-100%=Booklet Portrait",
		"-4:!-4L0.707+0%-100%,+1L.707+0%-200%,!+2L0.707+0%-100%,-3L.707+0%-200%=Booklet Landscape",
		"4:!-4N0.5+0%+100%,+1N0.5+100%+100%,+2U0.5-200%-100%,-3U0.5-100%-100%=Booklet 2-Up Fold",
		"-8:!-8N0.5+0%+100%,+1N0.5+100%+100%,-6N0.5+0%+0%,+3N0.5+100%+0%,!+2N0.5+0%+100%,-7N0.5+100%+100%,+4N0.5+0%+0%,-5N0.5+100%+0%=Booklet 2-Up Cut",
		"8:!+1L0.353+300%-100%,+2R0.353-400%+100%,+3R0.353-300%+100%,+4R0.353-200%+100%,+5R0.353-100%+100%,+6L0.353+0%-100%,+7L0.353+100%-100%,+8L0.353+200%-100%=PocketMod",
		"8:!+1L0.353+300%-200%,+2R0.353-400%+0%,+3R0.353-300%+0%,+4R0.353-200%+0%,+5R0.353-100%+0%,+6L0.353+0%-200%,+7L0.353+100%-200%,+8L0.353+200%-200%=PocketMod UpsideDown",
		"1:!+1N0.25+150%+300%,+1L0.25+250%-100%,+1U0.25-250%-100%,+1R0.25-350%+183%=Star Test Pattern",
		"2:!+1L0.707+0%-100%,+1L.707+100%-100%=Tile Copy 2x1",
		"4:!+1N0.5+0%+100%,+1N0.5+100%+100%,+1N0.5+0%+0%,+1N0.5+100%+0%=Tile Copy 2x2",
		"8:!+1L0.3535+0%-100%,+1L0.3535+0%-200%,+1L0.3535+100%-100%,+1L0.3535+100%-200%,+1L0.3535+200%-100%,+1L0.3535+200%-200%,+1L0.3535+300%-100%,+1L0.3535+300%-200%,=Tile Copy 4x2",
		"9:!+1N0.333+0.0%+200.0%,+1N0.333+100.0%+200.0%,+1N0.333+200.0%+200.0%,+1N0.333+0.0%+100.0%,+1N0.333+100.0%+100.0%,+1N0.333+200.0%+100.0%,+1N0.333+0.0%+0.0%,+1N0.333+100.0%+0.0%,+1N0.333+200.0%+0.0%=Tile Copy 3x3",
		"16:!+1N0.25+0%+300%,+1N0.25+100%+300%,+1N0.25+200%+300%,+1N0.25+300%+300%,+1N0.25+0%+200%,+1N0.25+100%+200%,+1N0.25+200%+200%,+1N0.25+300%+200%,+1N0.25+0%+100%,+1N0.25+100%+100%,+1N0.25+200%+100%,+1N0.25+300%+100%,+1N0.25+0%+0%,+1N0.25+100%+0%,+1N0.25+200%+0%,+1N0.25+300%+0%=Tile Copy 4x4",
		"32:!+1L0.17675+0%-100%,+1L0.17675+0%-200%,+1L0.17675+100%-100%,+1L0.17675+100%-200%,+1L0.17675+200%-100%,+1L0.17675+200%-200%,+1L0.17675+300%-100%,+1L0.17675+300%-200%,+1L0.17675+400%-100%,+1L0.17675+400%-200%,+1L0.17675+500%-100%,+1L0.17675+500%-200%,+1L0.17675+600%-100%,+1L0.17675+600%-200%,+1L0.17675+700%-100%,+1L0.17675+700%-200%,+1L0.17675+0%-300%,+1L0.17675+0%-400%,+1L0.17675+100%-300%,+1L0.17675+100%-400%,+1L0.17675+200%-300%,+1L0.17675+200%-400%,+1L0.17675+300%-300%,+1L0.17675+300%-400%,+1L0.17675+400%-300%,+1L0.17675+400%-400%,+1L0.17675+500%-300%,+1L0.17675+500%-400%,+1L0.17675+600%-300%,+1L0.17675+600%-400%,+1L0.17675+700%-300%,+1L0.17675+700%-400%=Tile Copy 4x8",
		"1:!+1N2.0+0.0%-25.0%,!+1N2.0-50.0%-25.0%=Split 2-up Landscape (Scale to Portrait first)"
	};
	
	public ShuffleRule(boolean newPageBefore, PageBase pageBase, int pageNumber, char rotate, double scale, double offsetX, boolean offsetXPercent, double offsetY, boolean offsetYPercent, double frameWidth) {
		if ("NRLU".indexOf(rotate) == -1) throw new IllegalArgumentException(""+rotate);
		if (pageNumber <= 0) throw new IllegalArgumentException();
		this.newPageBefore = newPageBefore;
		this.pageBase = pageBase;
		this.pageNumber = pageNumber;
		this.rotate = rotate;
		this.scale = scale;
		this.offsetX = offsetX;
		this.offsetXPercent = offsetXPercent;
		this.offsetY = offsetY;
		this.offsetYPercent = offsetYPercent;
		this.frameWidth = frameWidth;
	}
	
	public boolean isNewPageBefore() {
		return newPageBefore;
	}

	public double getOffsetX() {
		return offsetX;
	}

	public boolean isOffsetXPercent() {
		return offsetXPercent;
	}
	
	public String getOffsetXString() {
		String result = offsetX+(offsetXPercent?"%":"");
		if (!result.startsWith("-")) result = "+" + result;
		return result;
	}

	public double getOffsetY() {
		return offsetY;
	}

	public boolean isOffsetYPercent() {
		return offsetYPercent;
	}

	public String getOffsetYString() {
		String result = offsetY+(offsetYPercent?"%":"");
		if (!result.startsWith("-")) result = "+" + result;
		return result;
	}
	
	public PageBase getPageBase() {
		return pageBase;
	}

	public int getPageNumber() {
		return pageNumber;
	}
	
	public String getPageString() {
		return (pageBase == PageBase.ABSOLUTE?"":
			(pageBase == PageBase.BEGINNING?"+": "-"))+
			pageNumber;
	}

	public char getRotate() {
		return rotate;
	}

	public double getScale() {
		return scale;
	}

	public String toString() {
		return (newPageBefore?"!":"")+getPageString()+rotate+scale+
			getOffsetXString()+getOffsetYString()+
			(frameWidth == 0 ? "" : "F"+frameWidth);
	}


	public static ShuffleRule parseRule(String rule) {
		Pattern p = Pattern.compile("(\\!?)([-+]?)([0-9]+)([NRLU])([0-9.]+)([+-][0-9.]+)(%?)([+-][0-9.]+)(%?)((F[0-9.]+)?)");
		Matcher m = p.matcher(rule);
		if (!m.matches()) throw new NumberFormatException(rule);
		return new ShuffleRule(m.group(1).length()==1, 
				m.group(2).length()==0?PageBase.ABSOLUTE : (m.group(2).charAt(0) == '+' ? PageBase.BEGINNING : PageBase.END),
				Integer.parseInt(m.group(3)), 
				m.group(4).charAt(0),
				Double.parseDouble(m.group(5)),
				Double.parseDouble(m.group(6)),
				m.group(7).length() == 1,
				Double.parseDouble(m.group(8)),
				m.group(9).length() == 1,
				m.group(10).length()==0 ? 0 : Double.parseDouble(m.group(11).substring(1)));
	}

	public int getRotateAngle() {
		switch(rotate) {
		case 'N': return 0;
		case 'R': return 90;
		case 'U': return 180;
		case 'L': return 270;
		default: throw new IllegalStateException();
		}
	}

	public static ShuffleRule[] parseRuleSet(String ruleSet, int[] out_passLength) {
		
		
		int pos = ruleSet.indexOf(":");
		if (pos == -1) throw new NumberFormatException("No colon found");
		int pos2 = ruleSet.indexOf(',');
		if (pos2 == -1 || pos2 > pos) pos2 = pos;
		int pages = Integer.parseInt(ruleSet.substring(0, pos2));
		int size = pos2 == pos ? 0 : Integer.parseInt(ruleSet.substring(pos2+1, pos));		
		String[] ruleStrings = ruleSet.substring(pos+1).split(",");
		ShuffleRule[] rules = new ShuffleRule[ruleStrings.length];
		for (int i = 0; i < rules.length; i++) {
			rules[i] = ShuffleRule.parseRule(ruleStrings[i]);
		}
		if (rules.length == 0) throw new NumberFormatException("No rules found");
		if (!rules[0].isNewPageBefore()) throw new NumberFormatException("First rule must have new page before");
		out_passLength[0] = pages;
		out_passLength[1] = size;
		return rules;
	}

	public double getFrameWidth() {
		return frameWidth;
	}
}
