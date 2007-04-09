package jpdftweak.core;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;

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
		rdr = new PdfReader(file.getAbsolutePath(), ownerPassword
				.getBytes("ISO-8859-1"));
		if (!rdr.isOpenedWithFullPermissions())
			throw new IOException("Invalid owner password");
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
					result.put(key.toString().substring(1), infoDic.get(key).toString());
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
}
