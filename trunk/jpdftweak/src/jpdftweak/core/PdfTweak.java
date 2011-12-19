package jpdftweak.core;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;

import jpdftweak.core.ShuffleRule.PageBase;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PRAcroForm;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfTransition;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.PdfPageLabels.PdfPageLabelFormat;
import com.itextpdf.text.pdf.interfaces.PdfEncryptionSettings;
import com.itextpdf.text.pdf.internal.PdfViewerPreferencesImp;

public class PdfTweak {

	private static final PdfName[] INFO_NAMES = {
		PdfName.TITLE,
		PdfName.SUBJECT,
		PdfName.KEYWORDS,
		PdfName.AUTHOR,
		PdfName.CREATOR,
		PdfName.PRODUCER,
		PdfName.CREATIONDATE,
		PdfName.MODDATE
	};

	public static final int[] permissionBits = 
	{4, 8, 16, 32, 256, 512, 1024, 2048};
	public static final String[] permissionTexts = {
		"Printing", "ModifyContents", "Copy", "ModifyAnnotations", 
		"FillIn", "ScreenReaders", "Assembly", "HQPrinting"
	};

	public static final String[] TRANSITION_NAMES = new String[] {
		"None",
		"Out Vertical Split",
		"Out Horizontal Split",
		"In Vertical Split",
		"In Horizontal Split",
		"Vertical Blinds",
		"Vertical Blinds",
		"Inward Box",
		"Outward Box",
		"Left-Right Wipe",
		"Right-Left Wipe",
		"Bottom-Top Wipe",
		"Top-Bottom Wipe",
		"Dissolve",
		"Left-Right Glitter",
		"Top-Bottom Glitter",
		"Diagonal Glitter",
	};

	public static String[] getKnownInfoNames() {
		String[] result = new String[INFO_NAMES.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = INFO_NAMES[i].toString().substring(1);
		}
		return result;
	}

	private static final String PDFTK_PAGE_MARKER= "pdftk_PageNum";

	private PdfReader currentReader;
	private int encryptionMode = -1, encryptionPermissions = -1;
	private byte[] userPassword = null;
	private byte[] ownerPassword = null;
	private int[][] transitionValues;
	private Map<PdfName, PdfObject> optionalViewerPreferences;
	private int simpleViewerPreferences;
	private List<File> attachments = null;
	private PrivateKey key = null;
	private Certificate[] certChain = null;
	private int certificationLevel = 0;
	private boolean sigVisible=false;
	private final String inputFilePath, inputFileName, inputFileFullName;
	private boolean preserveHyperlinks;
	private File tempfile1 = null, tempfile2 = null;
	private List<File> inputFiles = new ArrayList<File>();
	private PdfToImage pdfImages;

	public PdfTweak(PdfInputFile singleFile, boolean useTempFiles) throws IOException {
		if (useTempFiles) {
			tempfile1 = File.createTempFile("~jpdftweak", ".tmp").getAbsoluteFile();
			tempfile2 = File.createTempFile("~jpdftweak", ".tmp").getAbsoluteFile();
			tempfile1.deleteOnExit();
			tempfile2.deleteOnExit();
		}
		currentReader = singleFile.getReader();
		File f = singleFile.getFile();
		inputFilePath = f.getAbsoluteFile().getParentFile().getAbsolutePath();
		inputFileFullName = f.getName();
		int pos = inputFileFullName.lastIndexOf('.');
		if (pos == -1) {
			inputFileName = inputFileFullName;
		} else {
			inputFileName = inputFileFullName.substring(0, pos);
		}
		inputFiles.add(f.getCanonicalFile());
	}

	public PdfTweak(PdfInputFile firstFile, List<PdfPageRange> pageRanges, boolean useTempFiles, int interleaveSize) throws IOException, DocumentException {
		this(firstFile, useTempFiles);
		OutputStream baos = createTempOutputStream();
		PdfReader firstReader = firstFile.getReader();
		Document document = new Document(firstReader.getPageSizeWithRotation(1));
		PdfCopy copy = new PdfCopy(document, baos);
		document.open();
		PdfImportedPage page;
		if (interleaveSize == 0) {
			int pagesBefore = 0;
			for(PdfPageRange pageRange : pageRanges) {
				int[] pages = pageRange.getPages(pagesBefore);
				for (int i = 0; i < pages.length; i++) {
					if (pages[i] == -1) {
						copy.addPage(pageRange.getInputFile().getPageSize(1), 0);
					} else {
						page = pageRange.getInputFile().getImportedPage(copy, pages[i]);
						copy.addPage(page);
					}
				}
				pagesBefore += pages.length;
				File f = pageRange.getInputFile().getFile().getCanonicalFile();
				if (!inputFiles.contains(f))
					inputFiles.add(f);
			}
		} else {
			int[][] pagesPerRange = new int[pageRanges.size()][];
			int maxLength = 0;
			for (int i = 0; i < pagesPerRange.length; i++) {
				PdfPageRange range = pageRanges.get(i);
				pagesPerRange[i]= range.getPages(0);;
				if (pagesPerRange[i].length > maxLength)
					maxLength = pagesPerRange[i].length;
			}
			int blockCount = (maxLength + interleaveSize - 1) / interleaveSize;
			for (int i = 0; i < blockCount; i++) {
				for (int j = 0; j < pageRanges.size(); j++) {
					PdfPageRange pageRange = pageRanges.get(j);
					int[] pages = pagesPerRange[j];
					for (int k = 0; k < interleaveSize; k++) {
						int pageIndex = i * interleaveSize + k;
						int pageNum = pageIndex < pages.length ? pages[pageIndex] : -1;
						if (pageNum == -1) {
							copy.addPage(pageRange.getInputFile().getPageSize(1), 0);
						} else {
							page = pageRange.getInputFile().getImportedPage(copy, pageNum);
							copy.addPage(page);
						}
					}					
				}
			}
		}
		PRAcroForm form = firstReader.getAcroForm();
		if (form != null) {
			copy.copyAcroForm(firstReader);
		}
		copyXMPMetadata(firstReader, copy);
		document.close();
		copyInformation(firstReader, currentReader = getTempPdfReader(baos));
	}
	
	
	public void setPdfImages(PdfToImage pdfImages){
		this.pdfImages = pdfImages;
	}
	
	public PdfToImage getPdfImages(){
		return this.pdfImages;
	}
	
	private void copyXMPMetadata(PdfReader reader, PdfWriter writer) throws IOException {
		PdfObject xmpObject = PdfReader.getPdfObject(reader.getCatalog().get(PdfName.METADATA));
		if (xmpObject != null && xmpObject.isStream()) {
			byte[] xmpMetadata = PdfReader.getStreamBytesRaw((PRStream)xmpObject);
			writer.setXmpMetadata(xmpMetadata);
		}
	}

	private OutputStream createTempOutputStream() throws IOException {
		if (tempfile1 != null) {
			File swap = tempfile1;
			tempfile1 = tempfile2;
			tempfile2 = swap;
			if (!tempfile1.delete()) 
				throw new IOException("Cannot delete "+tempfile1);
			return new FileOutputStream(tempfile1);
		} else {
			return new ByteArrayOutputStream();
		}
	}
	
	private PdfReader getTempPdfReader(OutputStream out) throws IOException {
		if (tempfile1 != null) {
			return new PdfReader(new RandomAccessFileOrArray(tempfile1.getPath(), false, true), null);
		} else {
			byte[] bytes = ((ByteArrayOutputStream)out).toByteArray();
			return new PdfReader(bytes);
		}
	}

	public void cleanup() {
		if (tempfile1 != null) tempfile1.delete();
		if (tempfile2 != null) tempfile2.delete();
	}
	
	/** 
	 * Some stuff that is unconditionally done by pdftk.
	 * Maybe it helps.
	 */
	private void cargoCult() {
		currentReader.consolidateNamedDestinations();
		currentReader.removeUnusedObjects();
		currentReader.shuffleSubsetNames();
	}

	public void updateInfoDictionary(Map<String, String> newInfo) {
		PdfDictionary trailer = currentReader.getTrailer();
		if( trailer != null && trailer.isDictionary() ) {
			PdfObject info= 
				PdfReader.getPdfObject( trailer.get( PdfName.INFO ) );
			if( info != null && info.isDictionary() ) {
				PdfDictionary infoDic = (PdfDictionary)info;
				for(Map.Entry<String, String>entry : newInfo.entrySet()) {
					if (entry.getValue().length() == 0) {
						infoDic.remove(new PdfName( entry.getKey()));
					} else {
						infoDic.put(new PdfName(entry.getKey()), new PdfString(entry.getValue(), PdfObject.TEXT_UNICODE));
					}
				}
			}
		}	
		// remove XMP metadata
		currentReader.getCatalog().remove(PdfName.METADATA);
	}
	
	private void copyInformation(PdfReader source, PdfReader destination) 
	{
		PdfDictionary srcTrailer = source.getTrailer();
		PdfDictionary dstTrailer = destination.getTrailer();
		if(srcTrailer != null && srcTrailer.isDictionary() && dstTrailer != null && dstTrailer.isDictionary()) {
			PdfObject srcInfo = PdfReader.getPdfObject(srcTrailer.get(PdfName.INFO));
			PdfObject dstInfo = PdfReader.getPdfObject(dstTrailer.get(PdfName.INFO));
			if (srcInfo != null && srcInfo.isDictionary() && dstInfo != null && dstInfo.isDictionary()) {
				PdfDictionary srcInfoDic = (PdfDictionary) srcInfo;
				PdfDictionary dstInfoDic = (PdfDictionary) dstInfo;
				for (Object k : srcInfoDic.getKeys()) {
					PdfName key = (PdfName) k;
					PdfObject value = srcInfoDic.get(key);
					dstInfoDic.put(key, value);
				}
			}
		}
		source.close();
	}

	public void setEncryption(int mode, int permissions, byte[] ownerPassword, byte[] userPassword) throws IOException {
		this.encryptionMode = mode;
		this.encryptionPermissions = permissions;
		this.userPassword = userPassword;
		this.ownerPassword = ownerPassword;
		if (ownerPassword.length == 0)
			throw new IOException("Owner password may not be empty");
	}

	private void setEncryptionSettings(PdfEncryptionSettings w) throws DocumentException {
		if (encryptionMode != -1) {
			w.setEncryption(userPassword, ownerPassword, encryptionPermissions, encryptionMode);
		}
	}

	public void writeOutput(String outputFile, boolean multipageTiff, boolean burst, boolean uncompressed, boolean sizeOptimize, boolean fullyCompressed) throws IOException, DocumentException{
		if(!outputFile.contains(File.separator)){
			File temp = new File(outputFile);
			outputFile = temp.getAbsolutePath();
		}
		if (sizeOptimize) {
			Document document = new Document(currentReader.getPageSizeWithRotation(1));
			OutputStream baos = createTempOutputStream();
			PdfSmartCopy copy = new PdfSmartCopy(document, baos);
			document.open();
			PdfImportedPage page;
			for(int i=0; i<currentReader.getNumberOfPages(); i++) {
				page = copy.getImportedPage(currentReader, i+1);
				copy.addPage(page);
			}
			PRAcroForm form = currentReader.getAcroForm();
			if (form != null) {
				copy.copyAcroForm(currentReader);
			}
			copy.setOutlines(SimpleBookmark.getBookmark(currentReader));
			PdfViewerPreferencesImp.getViewerPreferences(currentReader.getCatalog()).addToCatalog(copy.getExtraCatalog());
			copyXMPMetadata(currentReader, copy);
			PdfPageLabelFormat[] formats = PdfPageLabels.getPageLabelFormats(currentReader);
			if (formats != null) {
				PdfPageLabels lbls = new PdfPageLabels();
				for (PdfPageLabelFormat format : formats) {
					lbls.addPageLabel(format);			
				}
				copy.setPageLabels(lbls);
			}
			document.close();
			copyInformation(currentReader, currentReader = getTempPdfReader(baos));
		}
		outputFile = outputFile.replace("<F>", inputFileName);
		outputFile = outputFile.replace("<FX>", inputFileFullName);
		outputFile = outputFile.replace("<P>", inputFilePath);
		if (outputFile.contains("<#>")) {
			for (int i = 1;; i++) {
				String f = outputFile.replace("<#>", "" + i);
				if (!new File(f).exists()) {
					outputFile = f;
					break;
				}
			}
		}
		if (!burst && inputFiles.contains(new File(outputFile).getCanonicalFile()))
			throw new IOException("Output file must be different from input file(s)");
		cargoCult();
		try {
			if (uncompressed && pdfImages == null) {
				Document.compress = false;
			}
			int total = currentReader.getNumberOfPages();
			if(multipageTiff){
				if (outputFile.indexOf('*') != -1)
					throw new IOException("TIFF multipage filename should not contain *");
				Document document = new Document(currentReader.getPageSizeWithRotation(1));
				OutputStream baos = new ByteArrayOutputStream();
				PdfCopy copy = new PdfCopy(document,baos);
				document.open();
				PdfImportedPage page;
				for(int pagenum=1; pagenum <= currentReader.getNumberOfPages(); pagenum++) {
					page = copy.getImportedPage(currentReader, pagenum);
					copy.addPage(page);
				}
				PRAcroForm form = currentReader.getAcroForm();
				if (form != null) {
					copy.copyAcroForm(currentReader);
				}
				document.close();
				pdfImages.convertToMultiTiff(((ByteArrayOutputStream)baos).toByteArray(),outputFile);
			}else if(burst){
				String fn = outputFile;
				if (fn.indexOf('*') == -1)
					throw new IOException("Output filename does not contain *");
				String prefix = fn.substring(0, fn.indexOf('*'));
				String suffix = fn.substring(fn.indexOf('*')+1);
				String[] pageLabels = PdfPageLabels.getPageLabels(currentReader);
				PdfCopy copy = null;
				OutputStream baos = null;
				for(int pagenum=1; pagenum <= currentReader.getNumberOfPages(); pagenum++) {
					Document document = new Document(currentReader.getPageSizeWithRotation(1));
					String pageNumber = ""+pagenum;
					if (pageLabels != null && pagenum <= pageLabels.length)
						pageNumber = pageLabels[pagenum - 1];
					File outFile = new File(prefix+pageNumber+suffix);
					if (inputFiles.contains(outFile.getCanonicalFile()))
						throw new IOException("Output file must be different from input file(s)");
					if (!outFile.getParentFile().isDirectory())
						outFile.getParentFile().mkdirs();
					if(pdfImages.shouldExecute()){
						baos = new ByteArrayOutputStream();
						copy = new PdfCopy(document,baos);
					} else {
						copy = new PdfCopy(document,new FileOutputStream(outFile));
						setEncryptionSettings(copy);
						if (fullyCompressed)
							copy.setFullCompression();
					}
					document.open();
					PdfImportedPage page;
					page = copy.getImportedPage(currentReader, pagenum);
					copy.addPage(page);
					PRAcroForm form = currentReader.getAcroForm();
					if (form != null) {
						copy.copyAcroForm(currentReader);
					}
					document.close();
					if(pdfImages.shouldExecute()){
						pdfImages.convertToImage(((ByteArrayOutputStream)baos).toByteArray(),prefix+pageNumber+suffix);
					}
				}
			} else {
				PdfStamper stamper;
				if (key != null) {
					stamper = PdfStamper.createSignature(currentReader, new FileOutputStream(outputFile), '\0', null, true);
					PdfSignatureAppearance sap = stamper.getSignatureAppearance();
					sap.setCrypto(key, certChain, null, PdfSignatureAppearance.WINCER_SIGNED);
					sap.setCertificationLevel(certificationLevel);
					if (sigVisible)
						sap.setVisibleSignature(new Rectangle(100, 100, 200, 200), 1, null);
				} else {
					stamper = new PdfStamper(currentReader, new FileOutputStream(outputFile));
				}
				setEncryptionSettings(stamper);
				if (fullyCompressed)
					stamper.setFullCompression();
				for (int i = 1; i <= total; i++) {
					currentReader.setPageContent(i, currentReader.getPageContent(i));
				}
				if (transitionValues != null) {
					for (int i = 0; i < total; i++) {
						PdfTransition t = transitionValues[i][0] == 0 ? null : new PdfTransition(transitionValues[i][0], transitionValues[i][1]);
						stamper.setTransition(t, i+1);
						stamper.setDuration(transitionValues[i][2], i+1);
					}
				}
				if (optionalViewerPreferences != null) {
					stamper.setViewerPreferences(simpleViewerPreferences);
					for(Map.Entry<PdfName, PdfObject> e : optionalViewerPreferences.entrySet()) {
						stamper.addViewerPreference(e.getKey(), e.getValue());
					}
				}
				if (attachments != null) {
					for (File f : attachments) {
						stamper.addFileAttachment(f.getName(), null, f.getAbsolutePath(), f.getName());
					}
				}
				stamper.close();
			}
		} finally {
			Document.compress = true;
		}
		currentReader.close();
		currentReader = null;
		if (tempfile1 != null && !tempfile1.delete()) 
			throw new IOException("Cannot delete "+tempfile1);
		if (tempfile2 != null && !tempfile2.delete()) 
			throw new IOException("Cannot delete "+tempfile2);
		tempfile1 = tempfile2 = null;
	}

	public void cropPages(PageBox cropTo) throws IOException, DocumentException {
		OutputStream baos = createTempOutputStream();
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, baos);
		PdfContentByte cb = null;
		int[] rotations = new int[currentReader.getNumberOfPages()];
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			PageBox box = cropTo;
			Rectangle pageSize = currentReader.getPageSize(i);
			Rectangle currentSize = null;
			while (box != null) {
				 currentSize = currentReader.getBoxSize(i, box.getBoxName());
				 if (currentSize != null)
					 break;
				 box = box.defaultBox;
			}
			if (currentSize == null)
				currentSize = pageSize;
			document.setMargins(0, 0, 0, 0);
			document.setPageSize(new Rectangle(currentSize.getWidth(), currentSize.getHeight())); 
			if (cb == null) {
				document.open();
				cb = writer.getDirectContent();
			} else {
				document.newPage();
			}
			rotations[i-1] = currentReader.getPageRotation(i);
			PdfImportedPage page = writer.getImportedPage(currentReader, i);
			cb.addTemplate(page, pageSize.getLeft()-currentSize.getLeft(), pageSize.getBottom()-currentSize.getBottom());
			if (preserveHyperlinks) {
				List links = currentReader.getLinks(i);
				for (int j = 0; j < links.size(); j++) {
					PdfAnnotation.PdfImportedLink link = (PdfAnnotation.PdfImportedLink) links.get(j);
					if (link.isInternal()) {
						link.transformDestination(1, 0, 0, 1, pageSize.getLeft()-currentSize.getLeft(), pageSize.getBottom()-currentSize.getBottom());
					}
					link.transformRect(1, 0, 0, 1, pageSize.getLeft()-currentSize.getLeft(), pageSize.getBottom()-currentSize.getBottom());
					writer.addAnnotation(link.createAnnotation(writer));
				}
			}
		}
		copyXMPMetadata(currentReader, writer);
		document.close();
		copyInformation(currentReader, currentReader = getTempPdfReader(baos));
		// restore rotation
		for(int i=1; i<=currentReader.getNumberOfPages(); i++) {
			PdfDictionary dic = currentReader.getPageN(i);
			dic.put(PdfName.ROTATE, new PdfNumber(rotations[i-1]));
		}
	}
	
	public void rotatePages(int portraitCount, int landscapeCount) {
		for(int i=1; i<=currentReader.getNumberOfPages(); i++) {
			int rotation = currentReader.getPageRotation(i);
			Rectangle r = currentReader.getPageSizeWithRotation(i);
			int count;
			if (r.getWidth() > r.getHeight()) { // landscape
				count = landscapeCount;
			} else {
				count = portraitCount;
			}
			rotation = (rotation + 90* count) % 360;
			PdfDictionary dic = currentReader.getPageN(i);
			dic.put(PdfName.ROTATE, new PdfNumber(rotation));
		}
	}

	public void removeRotation() throws DocumentException, IOException {
		boolean needed=false;
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			if (currentReader.getPageRotation(i) != 0) needed=true;
		}
		if (!needed) return;
		OutputStream baos = createTempOutputStream();
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, baos);
		PdfContentByte cb = null;
		PdfImportedPage page;
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			Rectangle currentSize = currentReader.getPageSizeWithRotation(i);
			currentSize = new Rectangle(currentSize.getWidth(), currentSize.getHeight()); // strip rotation
			document.setPageSize(currentSize);
			if (cb == null) {
				document.open();
				cb = writer.getDirectContent();
			} else {
				document.newPage();
			}
			int rotation = currentReader.getPageRotation(i);
			page = writer.getImportedPage(currentReader, i);
			float a, b, c, d, e, f;
			if (rotation == 0) {
				a=1; b=0; c=0; d=1; e=0; f=0;
			} else if (rotation == 90) {
				a=0; b=-1; c=1; d=0; e=0; f=currentSize.getHeight();
			} else if (rotation == 180) {
				a=-1; b=0; c=0; d=-1; e=currentSize.getWidth(); f=currentSize.getHeight();
			} else if (rotation == 270) {
				a=0; b=1; c=-1; d=0; e=currentSize.getWidth(); f=0;
			} else {
				throw new IOException("Unparsable rotation value: "+rotation);
			}
			cb.addTemplate(page, a, b, c, d, e, f);
			if (preserveHyperlinks) {
				List links = currentReader.getLinks(i);
				for (int j = 0; j < links.size(); j++) {
					PdfAnnotation.PdfImportedLink link = (PdfAnnotation.PdfImportedLink) links.get(j);
					if (link.isInternal()) {
						int dPage = link.getDestinationPage();
						int dRotation = currentReader.getPageRotation(dPage);
						Rectangle dSize = currentReader.getPageSizeWithRotation(dPage);
						float aa, bb, cc, dd, ee, ff;
						if (dRotation == 0) {
							aa=1; bb=0; cc=0; dd=1; ee=0; ff=0;
						} else if (dRotation == 90) {
							aa=0; bb=-1; cc=1; dd=0; ee=0; ff=dSize.getHeight();
						} else if (dRotation == 180) {
							aa=-1; bb=0; cc=0; dd=-1; ee=dSize.getWidth(); ff=dSize.getHeight();
						} else if (dRotation == 270) {
							aa=0; bb=1; cc=-1; dd=0; ee=dSize.getWidth(); ff=0;
						} else {
							throw new IOException("Unparsable rotation value: "+dRotation);
						}
						link.setDestinationPage(dPage);
						link.transformDestination(aa, bb, cc, dd, ee, ff);
					}
					link.transformRect(a, b, c, d, e, f);
					writer.addAnnotation(link.createAnnotation(writer));
				}
			}
		}
		copyXMPMetadata(currentReader, writer);
		document.close();
		copyInformation(currentReader, currentReader = getTempPdfReader(baos));	
	}

	public void scalePages(float newWidth, float newHeight, boolean noEnlarge, boolean preserveAspectRatio) throws DocumentException, IOException {
		removeRotation();
		OutputStream baos = createTempOutputStream();
		Rectangle newSize = new Rectangle(newWidth, newHeight);
		Document document = new Document(newSize, 0, 0, 0, 0);
		PdfWriter writer = PdfWriter.getInstance(document, baos);
		document.open();
		PdfContentByte cb = writer.getDirectContent();
		PdfImportedPage page;
		float offsetX, offsetY;
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			document.newPage();
			Rectangle currentSize = currentReader.getPageSizeWithRotation(i);
			if(currentReader.getPageRotation(i) != 0) throw new RuntimeException(""+currentReader.getPageRotation(i));
			float factorX = newSize.getWidth() / currentSize.getWidth();
			float factorY = newSize.getHeight() / currentSize.getHeight();
			if (noEnlarge) {
				if (factorX > 1) factorX=1;
				if (factorY>1) factorY=1;
			}
			if (preserveAspectRatio) {
				factorX = Math.min(factorX, factorY);
				factorY = factorX;
			}
			offsetX = (newSize.getWidth() - (currentSize.getWidth() * factorX)) / 2f;
			offsetY = (newSize.getHeight() - (currentSize.getHeight() * factorY)) / 2f;
			page = writer.getImportedPage(currentReader, i);
			cb.addTemplate(page, factorX, 0, 0, factorY, offsetX, offsetY);
			if (preserveHyperlinks) {
				List links = currentReader.getLinks(i);
				for (int j = 0; j < links.size(); j++) {
					PdfAnnotation.PdfImportedLink link = (PdfAnnotation.PdfImportedLink) links.get(j);
					if (link.isInternal()) {
						int dPage = link.getDestinationPage();
						Rectangle dSize = currentReader.getPageSizeWithRotation(dPage);
						float dFactorX = newSize.getWidth() / dSize.getWidth();
						float dFactorY = newSize.getHeight() / dSize.getHeight();
						if (noEnlarge) {
							if (dFactorX > 1) dFactorX=1;
							if (dFactorY > 1) dFactorY=1;
						}
						if (preserveAspectRatio) {
							dFactorX = Math.min(dFactorX, dFactorY);
							dFactorY = dFactorX;
						}
						float dOffsetX = (newSize.getWidth() - (dSize.getWidth() * dFactorX)) / 2f;
						float dOffsetY = (newSize.getHeight() - (dSize.getHeight() * dFactorY)) / 2f;
						link.setDestinationPage(dPage);
						link.transformDestination(dFactorX, 0, 0, dFactorY, dOffsetX, dOffsetY);
					}
					link.transformRect(factorX, 0, 0, factorY, offsetX, offsetY);
					writer.addAnnotation(link.createAnnotation(writer));
				}
			}
		}
		copyXMPMetadata(currentReader, writer);
		document.close();
		copyInformation(currentReader, currentReader = getTempPdfReader(baos));
	}

	public void shufflePages(int passLength, int blockSize, ShuffleRule[] shuffleRules) throws DocumentException, IOException {
		removeRotation();
		OutputStream baos = createTempOutputStream();
		Rectangle size = currentReader.getPageSize(1);
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			if (currentReader.getPageSize(i).getWidth() != size.getWidth() ||
					currentReader.getPageSize(i).getHeight() != size.getHeight()) {
				throw new IOException("Pages must have equals sizes to be shuffled. Use the Scale option on the PageSize tab first.");
			}
			if (currentReader.getPageRotation(i) != 0) throw new RuntimeException();
		}
		Document document = new Document(size, 0, 0, 0, 0);
		PdfWriter writer = PdfWriter.getInstance(document, baos);
		document.open();
		PdfContentByte cb = writer.getDirectContent();
		PdfTemplate page;
		int pl = Math.abs(passLength);
		int cnt = currentReader.getNumberOfPages();
		int passes = blockSize == 0 ? 1 : (cnt + blockSize - 1) / blockSize; 
		int[] destinationPageNumbers = null;
		ShuffleRule[] destinationShuffleRules = null;
		if (preserveHyperlinks){
			destinationPageNumbers = new int[cnt+1];
			destinationShuffleRules = new ShuffleRule[cnt+1];
			int ddPage=0;
			for(int pass = 0; pass < passes; pass++) {
				int passcnt = pass == passes - 1 ? cnt - pass * blockSize : blockSize; 
				int refcnt = ((passcnt + (pl-1))/pl)*pl;
				for (int i = 0; i < passcnt; i+=pl) {
					int idx = i;
					int reverseIdx=refcnt - idx - pl;
					if (passLength <0) {
						idx = i/2;
						reverseIdx = refcnt - idx-pl;
					}
					idx += pass * blockSize;
					reverseIdx += pass * blockSize;
					for (ShuffleRule sr : shuffleRules) {
						if (sr.isNewPageBefore()) ddPage++;
						int pg = sr.getPageNumber();
						if (sr.getPageBase() == PageBase.BEGINNING) {
							pg += idx;
						} else if (sr.getPageBase() == PageBase.END) {
							pg += reverseIdx;
						}
						if (pg < 1)
							throw new IOException("Invalid page number. Check your n-up rules.");
						if (pg <= cnt) {
							destinationPageNumbers[pg] = ddPage;
							destinationShuffleRules[pg] = sr;
						}
					}
				}
			}
		}
		for(int pass = 0; pass < passes; pass++) {
			int passcnt = pass == passes - 1 ? cnt - pass * blockSize : blockSize; 
			int refcnt = ((passcnt + (pl-1))/pl)*pl;
			for (int i = 0; i < passcnt; i+=pl) {
				int idx = i;
				int reverseIdx=refcnt - idx - pl;;
				if (passLength <0) {
					idx = i/2;
					reverseIdx = refcnt - idx-pl;
				}
				idx += pass * blockSize;
				reverseIdx += pass * blockSize;
				for (ShuffleRule sr : shuffleRules) {
					if (sr.isNewPageBefore()) document.newPage();
					float s = (float)sr.getScale();
					float offsetx = (float)sr.getOffsetX();
					float offsety = (float)sr.getOffsetY();
					if (sr.isOffsetXPercent()) { offsetx = offsetx * size.getWidth()/100;}
					if (sr.isOffsetXPercent()) { offsety = offsety * size.getHeight()/100;}
					float a, b, c, d, e, f;
					switch(sr.getRotate()) {
					case 'N': 
						a=s; b=0; c=0; d=s; e=offsetx*s; f=offsety*s; break;
					case 'R':
						a=0; b=-s; c=s; d=0; e=offsety*s; f=-offsetx*s; break;
					case 'U':
						a=-s; b=0; c=0; d=-s; e=-offsetx*s; f=-offsety*s; break;
					case 'L':
						a=0; b=s; c=-s; d=0; e=-offsety*s; f=offsetx*s; break;	
					default: 
						throw new RuntimeException(""+sr.getRotate());
					}
					int pg = sr.getPageNumber();
					if (sr.getPageBase() == PageBase.BEGINNING) {
						pg += idx;
					} else if (sr.getPageBase() == PageBase.END) {
						pg += reverseIdx;
					}
					if (pg < 1)
						throw new IOException("Invalid page number. Check your n-up rules.");
					if (pg <= cnt) {
						page = writer.getImportedPage(currentReader, pg);
						cb.addTemplate(page, a, b, c, d, e, f);
						if (preserveHyperlinks) {
							List links = currentReader.getLinks(pg);
							for (int j = 0; j < links.size(); j++) {
								PdfAnnotation.PdfImportedLink link = (PdfAnnotation.PdfImportedLink) links.get(j);
								if (link.isInternal()) {
									int dPage = link.getDestinationPage();
									ShuffleRule dsr = destinationShuffleRules[dPage];								
									float dS = (float)dsr.getScale();
									float dOffsetx = (float)dsr.getOffsetX();
									float dOffsety = (float)dsr.getOffsetY();
									if (dsr.isOffsetXPercent()) { dOffsetx = dOffsetx * size.getWidth()/100;}
									if (dsr.isOffsetXPercent()) { dOffsety = dOffsety * size.getHeight()/100;}
									float aa, bb, cc, dd, ee, ff;
									switch(dsr.getRotate()) {
									case 'N': 
										aa=dS; bb=0; cc=0; dd=dS; ee=dOffsetx*dS; ff=dOffsety*dS; break;
									case 'R':
										aa=0; bb=-dS; cc=dS; dd=0; ee=dOffsety*dS; ff=-dOffsetx*dS; break;
									case 'U':
										aa=-dS; bb=0; cc=0; dd=-dS; ee=-dOffsetx*dS; ff=-dOffsety*dS; break;
									case 'L':
										aa=0; bb=dS; cc=-dS; dd=0; ee=-dOffsety*dS; ff=dOffsetx*dS; break;	
									default: 
										throw new RuntimeException(""+sr.getRotate());
									}
									link.setDestinationPage(destinationPageNumbers[dPage]);
									link.transformDestination(aa, bb, cc, dd, ee, ff);
								}
								link.transformRect(a, b, c, d, e, f);
								writer.addAnnotation(link.createAnnotation(writer));
							}
						}
						if (sr.getFrameWidth() > 0) {
							cb.setLineWidth((float)sr.getFrameWidth());
							cb.rectangle(e, f, a*size.getWidth()+c*size.getHeight(), b*size.getWidth()+d*size.getHeight());
							cb.stroke();
						}
					} else {
						writer.setPageEmpty(false);
					}
				}
			}
		}
		copyXMPMetadata(currentReader, writer);
		document.close();
		copyInformation(currentReader, currentReader = getTempPdfReader(baos));	
	}

	public void addPageMarks() {
		int pageCount = currentReader.getNumberOfPages();
		for (int i = 1; i <= pageCount; ++i) {
			PdfDictionary p = currentReader.getPageN(i);
			if (p != null && p.isDictionary()) {
				p.put(new PdfName(PDFTK_PAGE_MARKER), new PdfNumber(i));
			}
		}
	}

	public void removePageMarks() {
		int pageCount= currentReader.getNumberOfPages();
		for (int i = 1; i <= pageCount; ++i) {
			PdfDictionary p = currentReader.getPageN(i);
			if (p != null && p.isDictionary()) {
				p.remove(new PdfName(PDFTK_PAGE_MARKER));
			}
		}
	}

	public void updateBookmarks(PdfBookmark[] bm) throws DocumentException, IOException{
		OutputStream baos = createTempOutputStream();
		PdfStamper stamper = new PdfStamper(currentReader, baos);
		stamper.setOutlines(PdfBookmark.makeBookmarks(bm));
		stamper.close();
		currentReader = getTempPdfReader(baos);
	}

	public void addWatermark(PdfInputFile wmFile, String wmText, int wmSize, float wmOpacity, Color wmColor, int pnPosition, boolean pnFlipEven, int pnSize, float pnHOff, float pnVOff, String mask) throws DocumentException, IOException {
		OutputStream baos = createTempOutputStream();  
		int pagecount = currentReader.getNumberOfPages();
		PdfGState gs1 = new PdfGState();
		gs1.setFillOpacity(wmOpacity);
		PdfStamper stamper = new PdfStamper(currentReader, baos);
		BaseFont bf = BaseFont.createFont("Helvetica", BaseFont.WINANSI,
				false);
		float txtwidth=0;
		PdfImportedPage wmTemplate = null;
		String[] pageLabels = null;
		PdfPageLabelFormat[] pageLabelFormats = null;
		if (wmText != null) {
			txtwidth = bf.getWidthPoint(wmText, wmSize);
		}
		if (wmFile != null) {
			wmTemplate = stamper.getImportedPage(wmFile.getReader(), 1);
		}
		if (mask != null && mask.length() > 0) {
			pageLabels = PdfPageLabels.getPageLabels(currentReader);
			if (pageLabels == null) {
				pageLabels = new String[pagecount];
				for (int i = 1; i <= pagecount; i++) {
					pageLabels[i-1] = "" + i;
				}
			}
			pageLabelFormats = PdfPageLabels.getPageLabelFormats(currentReader);
			if (pageLabelFormats == null || pageLabelFormats.length == 0) {
				pageLabelFormats = new PdfPageLabelFormat[] {
					new PdfPageLabelFormat(1, PdfPageLabels.DECIMAL_ARABIC_NUMERALS, "", 1)
				};
			}
		}
		for (int i = 1; i <= pagecount; i++) {
			if (wmTemplate != null) {
				PdfContentByte underContent = stamper.getUnderContent(i);
				underContent.addTemplate(wmTemplate, 0, 0);
				if (preserveHyperlinks) {
					List links = currentReader.getLinks(i);
					PdfWriter w = underContent.getPdfWriter();
					for (int j = 0; j < links.size(); j++) {
						PdfAnnotation.PdfImportedLink link = (PdfAnnotation.PdfImportedLink) links.get(j);
						if (link.isInternal()) 
							continue; // preserving internal links would be pointless here
						w.addAnnotation(link.createAnnotation(w));
					}
				}
			}
			PdfContentByte overContent = stamper.getOverContent(i);
			Rectangle size = currentReader.getPageSizeWithRotation(i);
			if (wmText != null) {
				float angle = (float) Math.atan(size.getHeight() / size.getWidth());
				float m1 = (float) Math.cos(angle);
				float m2 = (float) - Math.sin(angle);
				float m3 = (float) Math.sin(angle);
				float m4 = (float) Math.cos(angle);
				float xoff = (float) ( -Math.cos(angle) * txtwidth / 2 - Math
						.sin(angle)
						* wmSize / 2);
				float yoff = (float) (Math.sin(angle) * txtwidth / 2 - Math
						.cos(angle)
						* wmSize / 2);
				overContent.saveState();
				overContent.setGState(gs1);
				overContent.beginText();
				if (wmColor != null)
					overContent.setColorFill(new BaseColor(wmColor));
				overContent.setFontAndSize(bf, wmSize);
				overContent.setTextMatrix(m1, m2, m3, m4, xoff + size.getWidth() / 2,
						yoff + size.getHeight() / 2);
				overContent.showText(wmText);
				overContent.endText();
				overContent.restoreState();
			}
			if (pnPosition != -1) {
				overContent.beginText();
				overContent.setFontAndSize(bf, pnSize);
				int pnXPosition = pnPosition % 3;
				if (pnFlipEven && i % 2 == 0)
					pnXPosition = 2 - pnXPosition;
				float xx = pnHOff * ((pnXPosition == 2) ? -1 : 1) + size.getWidth() * pnXPosition / 2.0f;
				float yy = pnVOff * ((pnPosition / 3 == 2) ? -1 : 1) + size.getHeight() * (pnPosition / 3) / 2.0f;
				String number = "" + i;
				if (mask != null && mask.length() > 0) {
					int pagenumber = i;
					for(PdfPageLabelFormat format : pageLabelFormats) {
						if (format.physicalPage <= i) {
							pagenumber = i - format.physicalPage + format.logicalPage;
						}
					}
					String pagenumbertext = pageLabels[i-1];
					try {
						number = String.format(mask, i, pagecount, pagenumber, pagenumbertext);
					} catch (IllegalFormatException ex) {
						throw new IOException(ex.toString());
					}
				}
				if ((pnXPosition != 1 && pnHOff * 2 < bf.getWidthPoint(number, pnSize)) ||
					    (pnPosition / 3 == 0 && pnVOff < bf.getDescentPoint(number, pnSize)) || 
					    (pnPosition / 3 == 2 && pnVOff < bf.getAscentPoint(number, pnSize))) {
					throw new IOException("Page number "+number+" is not within page bounding box");
				}
				overContent.showTextAligned(PdfContentByte.ALIGN_CENTER, number, xx, yy, 0);
				overContent.endText();
			}
		}
		stamper.close();
		currentReader = getTempPdfReader(baos);
	}

	public int getPageCount() {
		return currentReader.getNumberOfPages();
	}

	public void setTransition(int page, int type, int tduration, int pduration) {
		if (transitionValues == null) {
			transitionValues = new int[getPageCount()][3];
			for(int i=0; i<transitionValues.length; i++) {
				transitionValues[i][2] = -1;
			}
		}
		transitionValues[page-1][0] = type;
		transitionValues[page-1][1] = tduration;
		transitionValues[page-1][2] = pduration;
	}

	public void setViewerPreferences(int simplePrefs, Map<PdfName, PdfObject> optionalPrefs) {
		this.optionalViewerPreferences = optionalPrefs;
		this.simpleViewerPreferences = simplePrefs;
	}

	public void addFile(File f) {
		if (attachments == null) attachments = new ArrayList<File>();
		attachments.add(f);
	}

	public void setSignature(File keystoreFile,String alias, char[] password, int certificationLevel,  boolean visible) throws IOException {		
		try {
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(keystoreFile), password);
			key = (PrivateKey)ks.getKey(alias, password);
			if (key == null)
				throw new IOException("No private key found with alias " + alias);
			certChain = ks.getCertificateChain(alias);
			this.certificationLevel = certificationLevel;
			this.sigVisible = visible;
		} catch (GeneralSecurityException ex) {
			IOException ioe = new IOException(ex.toString());
			ioe.initCause(ex);
			throw ioe;
		}
	}

	public void setPageNumbers(PdfPageLabelFormat[] labelFormats) throws DocumentException, IOException {
		PdfPageLabels lbls = new PdfPageLabels();
		for (PdfPageLabelFormat format : labelFormats) {
			lbls.addPageLabel(format);			
		}
		Document document = new Document(currentReader.getPageSizeWithRotation(1));
		OutputStream baos = createTempOutputStream();
		PdfCopy copy = new PdfCopy(document, baos);
		document.open();
		PdfImportedPage page;
		for(int i=0; i<currentReader.getNumberOfPages(); i++) {
			page = copy.getImportedPage(currentReader, i+1);
			copy.addPage(page);
		}
		PRAcroForm form = currentReader.getAcroForm();
		if (form != null) {
			copy.copyAcroForm(currentReader);
		}
		copy.setPageLabels(lbls);
		copyXMPMetadata(currentReader, copy);
		document.close();
		copyInformation(currentReader, currentReader = getTempPdfReader(baos));
	}

	public void preserveHyperlinks() {
		preserveHyperlinks = true;
	}
	
	public enum PageBox {
		MediaBox(null), 
		CropBox(PageBox.MediaBox), 
		BleedBox(PageBox.CropBox), 
		TrimBox(PageBox.CropBox),
		ArtBox(PageBox.TrimBox);
		
		public final PageBox defaultBox;
		private PageBox(PageBox defaultBox) {
			this.defaultBox = defaultBox;
		}
		
		private String getBoxName() {
			return name().substring(0, name().length()-3).toLowerCase();
		}
	}
}
