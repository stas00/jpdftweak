package jpdftweak.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import jpdftweak.Main;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfPageLabels;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.SimpleBookmark;
import com.itextpdf.text.pdf.PdfPageLabels.PdfPageLabelFormat;

public class PdfInputFile {
	private final File file;

	private final String ownerPassword;

	private PdfReader rdr;

	public PdfInputFile(File file, String ownerPassword) throws IOException {
		this.file = file;
		this.ownerPassword = ownerPassword;
		open();
	}

	private void open() throws IOException {
		RandomAccessFileOrArray raf = new RandomAccessFileOrArray(file.getAbsolutePath(), false, true);
		rdr = new PdfReader(raf, ownerPassword.getBytes("ISO-8859-1"));
		if (!rdr.isOpenedWithFullPermissions())
			throw new BadPasswordException("PdfReader not opened with owner password");
		rdr.consolidateNamedDestinations();
		rdr.removeUnusedObjects();
		rdr.shuffleSubsetNames();
	}

	public void close() {
		rdr.close();
	}

	public void reopen() throws IOException {
		close();
		open();
	}

	public File getFile() {
		return file;
	}

	public int getPageCount() {
		return rdr.getNumberOfPages();
	}
	
	public Rectangle getPageSize(int page) {
		return rdr.getPageSizeWithRotation(page);
	}

	@Override
	public String toString() {
		return file.getName();
	}

	protected PdfReader getReader() {
		return rdr;
	}

	protected PdfImportedPage getImportedPage(PdfWriter destination, int page) {
		return destination.getImportedPage(rdr, page);
	}

	public Map<String, String> getInfoDictionary() {
		Map<String, String> result = rdr.getInfo();
		if (result.containsKey("Producer") && result.get("Producer").indexOf(Document.getProduct()) == -1) {
			result.put("Producer", result.get("Producer") + "; modified by jPDF Tweak " + Main.VERSION + " (based on " + Document.getVersion() + ")");
		}
		return result;
	}

	public int getCryptoMode() {
		return rdr.getCryptoMode();
	}

	public boolean isMetadataEncrypted() {
		return rdr.isMetadataEncrypted();
	}

	public int getPermissions() {
		if (rdr.getCryptoMode() == 0) {
			// 40-bit encryption does not support some flags, but sets them.
			// Clear them so that they do not show up in -info output.
			return rdr.getPermissions() & 0xFFFF00FF;
		}
		return rdr.getPermissions();
	}

	public String getOwnerPassword() {
		return ownerPassword == null ? "" : ownerPassword;
	}
	public String getUserPassword() {
		byte[] userPwd = rdr.computeUserPassword();
		if (userPwd == null) return "";
		return new String(userPwd);
	}
	
	@SuppressWarnings("unchecked")
	public List<PdfBookmark> getBookmarks(int initialDepth) {
		List bmk = SimpleBookmark.getBookmark(rdr);
		return PdfBookmark.parseBookmarks(bmk, initialDepth);
	}

	public PdfPageLabelFormat[] getPageLabels() {
		return PdfPageLabels.getPageLabelFormats(rdr);
	}
}
