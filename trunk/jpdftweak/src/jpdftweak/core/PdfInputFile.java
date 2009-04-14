package jpdftweak.core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Rectangle;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfPageLabels;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.text.pdf.PdfPageLabels.PdfPageLabelFormat;

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

	public void reopen() throws IOException {
		rdr.close();
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
		Map<String, String> result = new HashMap<String, String>();
		PdfDictionary trailer = rdr.getTrailer();
		if (trailer != null && trailer.isDictionary()) {
			PdfObject info = PdfReader.getPdfObject(trailer.get(PdfName.INFO));
			if (info != null && info.isDictionary()) {
				PdfDictionary infoDic = (PdfDictionary) info;
				for (Object key_ : infoDic.getKeys()) {
					PdfName key = (PdfName) key_;
					String value = infoDic.get(key).toString();
					if (infoDic.get(key) instanceof PdfString) {
						PdfString s = (PdfString) infoDic.get(key);
						value = s.toUnicodeString();						
					}
					result.put(key.toString().substring(1), value);
				}
			}
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
