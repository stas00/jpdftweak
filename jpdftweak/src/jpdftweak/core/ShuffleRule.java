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

	public static enum PageBase {
		ABSOLUTE, BEGINNING, END
	}
	
	public static final String[] predefinedRuleSets = new String[] {
		"1:!+1N1+0%+0%=Default",
		"2:!+1L0.707+0%-100%,+2L.707+100%-100%=2-up Portrait",
		"2:!+1L0.707+0%-100%,+2L.707+0%-200%=2-up Landscape",
		"4:!+1N0.5+0%+100%,+2N0.5+100%+100%,+3N0.5+0%+0%,+4N0.5+100%+0%,=4-up",
		"9:!+1N.333+0%+200%,+2N.333+100%+200%,+3N.333+200%+200%,+4N.333+0%+100%,+5N.333+100%+100%,+6N.333+200%+100%,+7N.333+0%+0%,+8N.333+100%+0%,+9N.333+200%+0%,=9-up",
		"-4:!-4N1+0%+0%,!+1N1+0%+0%,!+2N1+0%+0%,!-3N1+0%+0%=Booklet Reorder",
		"-4:!-4L0.707+0%-100%,+1L.707+100%-100%,!+2L0.707+0%-100%,-3L.707+100%-100%=Booklet Portrait",
		"-4:!-4L0.707+0%-100%,+1L.707+0%-200%,!+2L0.707+0%-100%,-3L.707+0%-200%=Booklet Landscape",
		/* // TODO define all these!
		"=Booklet 2-Up Fold",
		"=Booklet 2-Up Cut",
		"=PocketMod",
		"=PocketMod UpsideDown",
		*/
		"1:!+1N0.25+150.0%+300.0%,+1L0.25+250.0%-100.0%,+1U0.25-250.0%-100.0%,+1R0.25-350.0%+183.0%=Star Test Pattern",
	};
	
	public ShuffleRule(boolean newPageBefore, PageBase pageBase, int pageNumber, char rotate, double scale, double offsetX, boolean offsetXPercent, double offsetY, boolean offsetYPercent) {
		if ("NRLU".indexOf(rotate) == -1) throw new IllegalArgumentException(""+rotate);
		if (pageNumber == 0) throw new IllegalArgumentException();
		this.newPageBefore = newPageBefore;
		this.pageBase = pageBase;
		this.pageNumber = pageNumber;
		this.rotate = rotate;
		this.scale = scale;
		this.offsetX = offsetX;
		this.offsetXPercent = offsetXPercent;
		this.offsetY = offsetY;
		this.offsetYPercent = offsetYPercent;
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
			getOffsetXString()+getOffsetYString();
	}


	public static ShuffleRule parseRule(String rule) {
		Pattern p = Pattern.compile("(\\!?)([-+]?)([0-9]+)([NRLU])([0-9.]+)([+-][0-9.]+)(%?)([+-][0-9.]+)(%?)");
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
				m.group(9).length() == 1);
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
		int pages = Integer.parseInt(ruleSet.substring(0, pos));
		String[] ruleStrings = ruleSet.substring(pos+1).split(",");
		ShuffleRule[] rules = new ShuffleRule[ruleStrings.length];
		for (int i = 0; i < rules.length; i++) {
			rules[i] = ShuffleRule.parseRule(ruleStrings[i]);
		}
		if (rules.length == 0) throw new NumberFormatException("No rules found");
		if (!rules[0].isNewPageBefore()) throw new NumberFormatException("First rule must have new page before");
		out_passLength[0] = pages;
		return rules;
	}
}
