package jpdftweak.core;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import jpdftweak.core.ShuffleRule.PageBase;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.interfaces.PdfEncryptionSettings;

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

	public PdfTweak(PdfInputFile singleFile) {
		currentReader = singleFile.getReader();
	}
	
	public PdfTweak(PdfInputFile firstFile, List<PdfPageRange> pageRanges) throws IOException, DocumentException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfReader firstReader = firstFile.getReader();
		Document document = new Document(firstReader.getPageSizeWithRotation(1));
		PdfCopy copy = new PdfCopy(document, baos);
		document.open();
		PdfImportedPage page;
		for(PdfPageRange pageRange : pageRanges) {
			int[] pages = pageRange.getPages();
			for (int i = 0; i < pages.length; i++) {
				page = pageRange.getInputFile().getImportedPage(copy, pages[i]);
				copy.addPage(page);
			}
		}
		PRAcroForm form = firstReader.getAcroForm();
		if (form != null) {
			copy.copyAcroForm(firstReader);
		}
		document.close();
		currentReader = new PdfReader(baos.toByteArray());
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
	}

	public void setEncryption(int mode, int permissions, byte[] ownerPassword, byte[] userPassword) {
		this.encryptionMode = mode;
		this.encryptionPermissions = permissions;
		this.userPassword = userPassword;
		this.ownerPassword = ownerPassword;
	}

	private void setEncryptionSettings(PdfEncryptionSettings w) throws DocumentException {
		if (encryptionMode != -1) {
			w.setEncryption(userPassword, ownerPassword, encryptionPermissions, encryptionMode);
		}
	}

	public void writeOutput(String outputFile, boolean burst, boolean uncompressed) throws IOException, DocumentException {
		cargoCult();
		try {
			if (uncompressed) {
				Document.compress = false;
			}
			int total = currentReader.getNumberOfPages();
			if (burst) {
				String fn = outputFile;
				if (fn.indexOf('*') == -1)
					throw new IOException("Output filename does not contain *");
				String prefix = fn.substring(0, fn.indexOf('*'));
				String suffix = fn.substring(fn.indexOf('*')+1);
				for(int pagenum=1; pagenum <= currentReader.getNumberOfPages(); pagenum++) {
					Document document = new Document(currentReader.getPageSizeWithRotation(1));
					PdfCopy copy = new PdfCopy(document,
							new FileOutputStream(prefix+pagenum+suffix));
					setEncryptionSettings(copy);
					document.open();
					PdfImportedPage page;
					page = copy.getImportedPage(currentReader, pagenum);
					copy.addPage(page);
					PRAcroForm form = currentReader.getAcroForm();
					if (form != null) {
						copy.copyAcroForm(currentReader);
					}
					document.close();
				}
			} else {
				PdfStamper stamper = new PdfStamper(currentReader, new FileOutputStream(outputFile));
				setEncryptionSettings(stamper);
				for (int i = 1; i <= total; i++) {
					currentReader.setPageContent(i, currentReader.getPageContent(i));
				}
				stamper.close();
			}
		} finally {
			Document.compress = true;
		}
		currentReader.close();
		currentReader = null;
	}

	public void rotatePages(int portraitCount, int landscapeCount) {
		for(int i=1; i<=currentReader.getNumberOfPages(); i++) {
			int rotation = currentReader.getPageRotation(i);
			Rectangle r = currentReader.getPageSizeWithRotation(i);
			int count;
			if (r.width() > r.height()) { // landscape
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
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, baos);
		PdfContentByte cb = null;
		PdfImportedPage page;
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			Rectangle currentSize = currentReader.getPageSizeWithRotation(i);
			currentSize = new Rectangle(currentSize.width(), currentSize.height()); // strip rotation
			document.setPageSize(currentSize);
			if (cb == null) {
				document.open();
				cb = writer.getDirectContent();
			} else {
				document.newPage();
			}
			int rotation = currentReader.getPageRotation(i);
			page = writer.getImportedPage(currentReader, i);
			if (rotation == 0) {
				cb.addTemplate(page, 1, 0, 0, 1, 0, 0);
			} else if (rotation == 90) {
				cb.addTemplate(page, 0, -1, 1, 0, 0, currentSize.height());
			} else if (rotation == 180) {
				cb.addTemplate(page, -1, 0, 0, -1, currentSize.width(), currentSize.height());
			} else if (rotation == 270) {
				cb.addTemplate(page, 0, 1, -1, 0, currentSize.width(), 0);
			} else {
				throw new IOException("Unparsable rotation value: "+rotation);
			}
		}
		document.close();
		currentReader = new PdfReader(baos.toByteArray());	
	}
	
	public void scalePages(float newWidth, float newHeight, boolean noEnlarge, boolean preserveAspectRatio) throws DocumentException, IOException {
		removeRotation();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
			float factorX = newSize.width() / currentSize.width();
			float factorY = newSize.height() / currentSize.height();
			if (noEnlarge) {
				if (factorX > 1) factorX=1;
				if (factorY>1) factorY=1;
			}
			if (preserveAspectRatio) {
				factorX = Math.min(factorX, factorY);
				factorY = factorX;
			}
			offsetX = (newSize.width() - (currentSize.width() * factorX)) / 2f;
			offsetY = (newSize.height() - (currentSize.height() * factorY)) / 2f;
			page = writer.getImportedPage(currentReader, i);
			cb.addTemplate(page, factorX, 0, 0, factorY, offsetX, offsetY);
		}
		document.close();
		currentReader = new PdfReader(baos.toByteArray());
	}
	
	public void shufflePages(int passLength, ShuffleRule[] shuffleRules) throws DocumentException, IOException {
		removeRotation();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Rectangle size = currentReader.getPageSize(1);
		for (int i = 1; i <= currentReader.getNumberOfPages(); i++) {
			if (currentReader.getPageSize(i).width() != size.width() ||
					currentReader.getPageSize(i).height() != size.height()) {
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
		int refcnt = ((cnt + (pl-1))/pl)*pl;
		for (int i = 0; i < cnt; i+=pl) {
			int idx = i, reverseIdx=Integer.MIN_VALUE;
			if (passLength <0) {
				idx = i/2;
				reverseIdx = refcnt - idx-pl;
			}
			for (ShuffleRule sr : shuffleRules) {
				if (sr.isNewPageBefore()) document.newPage();
				float s = (float)sr.getScale();
				float offsetx = (float)sr.getOffsetX();
				float offsety = (float)sr.getOffsetY();
				if (sr.isOffsetXPercent()) { offsetx = offsetx * size.width()/100;}
				if (sr.isOffsetXPercent()) { offsety = offsety * size.height()/100;}
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
				} else {
					document.add(Chunk.NEWLINE);
				}
			}
		}
		document.close();
		currentReader = new PdfReader(baos.toByteArray());		
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
}
