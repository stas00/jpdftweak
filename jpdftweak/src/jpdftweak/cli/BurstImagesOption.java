package jpdftweak.cli;

import java.io.IOException;

import jpdftweak.core.PdfInputFile;
import jpdftweak.core.PdfToImage;
import jpdftweak.core.PdfToImage.ImageType;
import jpdftweak.core.PdfTweak;

import com.itextpdf.text.DocumentException;

public class BurstImagesOption implements CommandOption {

	private PdfToImage.ColorMode colorMode = PdfToImage.ColorMode.RGB;
	private PdfToImage.ImageType fileType;
	private PdfToImage.TiffCompression compressionType = PdfToImage.TiffCompression.NONE;
	private int quality = 100;
	private boolean transparent = false;

	@Override
	public boolean supportsOption(String option) {
		return option.equals("-colormode") || option.equals("-tiffcompression")
				|| option.equals("-transparency")
				|| option.equals("-imagequality")
				|| option.equals("-burstfiletype");
	}

	@Override
	public boolean setOption(String option, String value) throws IOException, DocumentException{
		if (option.equals("-colormode")) {
			try{
				colorMode = PdfToImage.ColorMode.valueOf(value);
			} catch (Throwable e){
				throw new DocumentException(
						"Please refer to \"-help -colormode\"");
			}
		} else if (option.equals("-tiffcompression")) {
			try{
				compressionType = PdfToImage.TiffCompression.valueOf(value);
			} catch (Throwable e){
				throw new DocumentException(
						"Please refer to \"-help -tiffcompression\"");
			}
		} else if (option.equals("-transparency")) {
			if (value.equals("YES")) {
				transparent = true;
			} else if (value.equals("NO")) {
				transparent = false;
			} else {
				throw new DocumentException(
						"Please refer to \"-help -transparency\"");
			}
		} else if (option.equals("-imagequality")) {
			if (!checkQuality(value)) {
				throw new DocumentException(
						"Please refer to \"-help -imagequality\"");
			} else {
				quality = Integer.parseInt(value);
			}
		} else if (option.equals("-burstfiletype")) {
			try{
				fileType = PdfToImage.ImageType.valueOf(value);
			} catch (Throwable e){
				throw new DocumentException(
						"Please refer to \"-help -burstfiletype\"");
			}
		}
		if (!value.equals("PDF")) {
			String jarPath = PdfToImage.getJarFolder();
			try {
				PdfToImage.setJavaLibraryPath();
			} catch (NoSuchFieldException e) {
				System.err.println("Error: Could not change Java Library Path "+ e);
			} catch (IllegalAccessException e) {
				System.err.println("Error: Could not change Java Library Path "+ e);
			}
			String sharedLibraryName = PdfToImage.checkForLibraries();
			if(sharedLibraryName != null){
				if (sharedLibraryName.equals("nojmupdf")) {
					throw new DocumentException("\"JmuPdf.jar\" does not exist in lib folder");
				} else if(sharedLibraryName.contains(".")){
					throw new DocumentException("\"" + sharedLibraryName
							+ "\" does not exist in \"" + jarPath + "\"");
				}
			}
		}
		return true;
	}

	private boolean checkQuality(String quality) throws IOException {
		try {
			int numQuality = Integer.parseInt(quality);
			return numQuality >= 0 && numQuality <= 100;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	@Override
	public void run(PdfTweak tweak, PdfInputFile masterFile)
			throws IOException, DocumentException {
		if (fileType != null && fileType != PdfToImage.ImageType.PDF) {
			if ((fileType == PdfToImage.ImageType.TIFF)
					&& colorMode != PdfToImage.ColorMode.RGB) {
				transparent = false;
			}
			tweak.setPdfImages(new PdfToImage(true, colorMode, fileType, compressionType, quality, transparent));
		} else {
			tweak.setPdfImages(new PdfToImage(false, colorMode, fileType, compressionType, quality, transparent));
		}
	}

	@Override
	public String getSummary() {
		return " -colormode              Set the color mode of the exported images\n"
				+ " -tiffcompression        Set compression type for TIFF image files\n"
				+ " -transparency           Set if your image has transparent layers\n"
				+ " -imagequality           Set the quality for the exported images\n"
				+ " -burstfiletype          Set the file type of the burst pages\n";
	}

	@Override
	public String getHelp(String option) {
		if (option.equals("-colormode")) {
			return " -colormode {MODE}\n"
					+ "    Set the color mode of the exported images\n"
					+ "	 accepted values:{RGB, GRAY, BNW, BNWI}]\n"
					+ "	 *option applicable only on burst pages function.\n";
		} else if (option.equals("-tiffcompression")) {
			return " -tiffcompression {COMPRESSIONTYPE}\n"
					+ "    Set compression type for TIFF image files\n"
					+ "	 accepted values:{NONE, LWZ, JPEG, ZLIB, PACKBITS, DEFLATE}\n"
					+ "	 *option applicable only on burst pages function \n";
		} else if (option.equals("-transparency")) {
			return " -transparency {YES/NO}\n"
					+ "    Set if your image has transparent layers\n"
					+ "	 accepted values:{YES, NO}\n"
					+ "	 *option applicable only on burst pages function \n"
					+ "     and PNG,TIFF,PAM,GIF images\n";
		} else if (option.equals("-imagequality")) {
			return " -imagequality {0-100}\n"
					+ "    Set the quality for the exported images\n"
					+ "	 accepted values:{Integers [0,100]}\n"
					+ "	 *option applicable only on burst pages function \n";
		} else {
			return " -burstfiletype {FILETYPE}\n"
					+ "    Set the file type of the burst pages\n"
					+ "	 accepted values:{PDF, JPG, PNG, GIF, PAM, PNM, BMP, TIFF}\n"
					+ "	 *option applicable only on burst pages function\n";
		}
	}

}
