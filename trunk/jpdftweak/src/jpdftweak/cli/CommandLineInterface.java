package jpdftweak.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lowagie.text.DocumentException;

import jpdftweak.Main;
import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfPageRange;
import jpdftweak.core.PdfTweak;

public class CommandLineInterface {

	private CommandOption[] options = {
			new PreserveHyperlinksOption(),
			new RotateOption(),
			new ScaleOption(),
			new WatermarkOptions(),
			new ShuffleOption(),
			new PageNumberOption(),
			new BookmarksOption(),
			new AttachOption(),
			new TransitionOption(),
			new ViewerOptions(),
			new DocInfoOption(),
			new EncryptOptions(),
			new SignOptions(),
			new InfoOption()
	};
	
	public CommandLineInterface(String[] args) throws IOException, DocumentException {
		PdfInputFile input = null;
		List<PdfPageRange> pageRanges = new ArrayList<PdfPageRange>();
		Map<String,PdfInputFile> aliases = new HashMap<String, PdfInputFile>();
		String output = null;
		boolean burstOutput = false, uncompressedOutput=false, markedOutput = false;
		String password = "";
		
		Pattern inputOption = Pattern.compile("-i((~?[0-9]+)(-(~?[0-9]+)?)?)?([eo]?)");
		Pattern outputOption = Pattern.compile("-o[mub]*");
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-help") || args[i].equals("-?") || args[i].equals("/?")) {
				if (i == args.length-1) {
					showHelp();
				} else {
					showHelpFor(args[i+1]);
				}
				return;
			} else if (args[i].equals("-v") || args[i].equals("-version")) {
				System.out.println("JPDF Tweak " + Main.VERSION);
				return;
			} else if (args[i].startsWith("-")) {
				if (i == args.length-1) {
					System.err.println("Option without parameters: "+args[i]);
					return;
				} else if (args[i].equals("-password")) {
					password = args[i+1];
				} else if (inputOption.matcher(args[i]).matches()) {
					Matcher m = inputOption.matcher(args[i]);
					m.matches();
					int from, to;
					if (m.group(2) == null) {
						from=1;
						to = -1;
					} else {
						from = Integer.parseInt(m.group(2).replace('~', '-'));
						if(m.group(3) == null) {
							to = from;
						} else if (m.group(4) == null) {
							to = -1;
						} else {
							to = Integer.parseInt(m.group(4).replace('~', '-'));
						}
					}
					if (!aliases.containsKey(args[i+1])) {
						aliases.put(args[i+1], new PdfInputFile(new File(args[i+1]), password));
					}
					PdfPageRange pr = new PdfPageRange(aliases.get(args[i+1]), from, to, !m.group(5).equals("e"), !m.group(5).equals("o"));
					pageRanges.add(pr);
				} else if (args[i].startsWith("-i=")) {
					aliases.put(args[i].substring(3), new PdfInputFile(new File(args[i+1]), password));
				} else if (outputOption.matcher(args[i]).matches()) {
					output = args[i+1];
					markedOutput = args[i].contains("m");
					uncompressedOutput = args[i].contains("u");
					burstOutput = args[i].contains("b");
				} else {
					boolean handled = false;
					for (CommandOption option : options) {
						if (option.supportsOption(args[i])) {
							if(!option.setOption(args[i], args[i+1])) return;
							handled = true;
							break;
						}
					}
					if (!handled) {
						System.err.println("Unknown option: "+args[i]);
						return;
					}
				}
				i++;
			} else {
				if (output != null) {
					if (!aliases.containsKey(args[i])) {
						aliases.put(args[i], new PdfInputFile(new File(args[i]), password));
					}
					PdfPageRange pr = new PdfPageRange(aliases.get(args[i]), 1, -1, true, true);
					pageRanges.add(pr);					
				} else if (input != null || pageRanges.size()> 0 || aliases.size() > 0) {
					output = args[i];
				} else {
					input = new PdfInputFile(new File(args[i]), password);
				}
			}
		}
		if (input != null && pageRanges.size() > 0) {
			System.err.println("Invalid combination of input files.");
			return;
		}
		if (input == null && pageRanges.size() == 0) {
			System.err.println("No input files.");
			return;
		}
		PdfTweak tweak;
		if (input != null) {
			tweak = new PdfTweak(input);
		} else if (aliases.containsKey("master")) {
			input = aliases.get("master");
			tweak = new PdfTweak(input, pageRanges);
		} else {
			input = pageRanges.get(0).getInputFile();
			tweak = new PdfTweak(input, pageRanges);
		}
		for (CommandOption option : options) {
			option.run(tweak, input);
		}
		if (output == null) {
			System.err.println("Cannot write PDF: No output file.");
		} else {
			if (markedOutput) {
				if (uncompressedOutput)
					tweak.addPageMarks();
				else
					tweak.removePageMarks();
			}
			tweak.writeOutput(output, burstOutput, uncompressedOutput);
			System.err.println("Output file written successfully.");
		}
	}

	private void showHelp() {
		System.out.println("jPDF Tweak "+Main.VERSION+"\n\n"+
				"Usage: jpdftweak {inputfile} [-o[opt]] {outputfile}\n"+
				"       jpdftweak -i[opt] {inputfile} [...] [-o[opt]] {outputfile}\n"+
				"       jpdftweak -o[opt] {outputfile} [-i[opt]] {inputfile} [...]\n"+
				"\n"+
				"The first syntax handles a single input file, the second and third one use\n" +
				"multiple file input. The third syntax is especially useful if you want to use\n" +
				"wildcards (like *.pdf) for the input file.\n"+
				"You may add options and transformation and their parameter everywhere in the\n" +
				"commandline, except between a -i/-o switch and the filename.\n" +
				"Parameters are case sensitive.\n\n"+
				"Parameters\n"+
				"~~~~~~~~~~\n"+
				" -help                   Show this help.\n"+
				" -help {transformation}  Show help for a transformation\n"+
				" -v[ersion]              Show version\n"+
				" -password {password}    Use password for opening next input file\n"+
				" -i[{options}]           next parameter is input file, see '-help -i'\n"+
				" -i={ALIAS}              use ALIAS for next input file name, '-help -i'\n"+
				" -o[{options}]           next parameter is output file, see '-help -o'\n"+
				"\n"+
				"Transformations\n"+
				"~~~~~~~~~~~~~~~");
		for(CommandOption option: options) {
			System.out.print(option.getSummary());
		}		
	}
	
	private void showHelpFor(String option) {
		if (!option.startsWith("-")) option="-"+option;
		if (option.equals("-i")) {
			System.out.println("\n"+
					"Input Files\n"+
					"~~~~~~~~~~~\n"+
					"If you use -i without options, or no -i parameters at all, all pages of the\n" +
					"given PDF file will be loaded. You can also use the following terms:\n"+
					" -i3                       Load page 3 only\n"+
					" -i~2                      Load the page before the last page only\n"+
					" -i3-9                     Load pages 3-9\n"+
					" -i5-                      Load pages 5-end\n"+
					" -ie                       Load even pages only\n"+
					" -i1-~3o                   Load all but the last two pages, only odd pages\n"+
					"You can load the file to an alias name using -i=ALIAS and use that alias name\n" +
					"later; this is useful if you want to reference a file more than once and it has\n" +
					"a long path/filename, or it requires a password. If you know 'pdftk', you know\n" +
					"aliases as well, only that they are optional in jPDF Tweak and not limited to a\n"+
					"single character.\n\n" +
					"Example:\n"+
					"  jpdftweak -o mixedmode.pdf -password secret -i=A secret.pdf \\\n"+
					"            -password moresecret -i=Ox knox.pdf \\\n" +
					"            -i Ox -i1-3 A -i Ox -i4- A -i Ox\n" +
					"This will take knox.pdf, then page 1-3 of secret.pdf, then knox.pdf again,\n" +
					"then the rest of secret.pdf and finally knox.pdf again.");
			
		} else if (option.equals("-o")) {
			System.out.println("\n"+
					"Output files\n"+
					"~~~~~~~~~~~~\n"+
					"Use -o to specify the output filename; you can add flags:\n"+
					"  -ou  save uncompressed, -om add Pdfmarks, -ob burst pages. \n"+
					"Flags can be combined like -oub.");	
		} else {
			System.out.println();
			for (CommandOption o : options) {
				if (o.supportsOption(option)) {
					System.out.println(o.getHelp(option));
					return;
				}
			}
			System.out.println("No help found for option "+option+".\n" +
					"Use -help without option to get a list of options.");
		}
	}

}
