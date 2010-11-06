jPDF Tweak
==========

This program requires Java 5 or higher. Download it from java.sun.com.

Start it by double clicking the jar file, or by

java -jar jpdftweak.jar

For the command line options, add -help to the command line above.

If you have the compact version and have problems, you might try
if they are present in the "normal" version as well.

See the "manual" folder for short manual.

Java Memory Limit
~~~~~~~~~~~~~~~~~

By default, a Java program may only access up to 64 MB of RAM. When
processing large PDFs, this can cause out of memory errors.

You can increase the memory that will be used by jPDF Tweak by editing
the jpdftweak.bat file with a text editor; change

@java -jar jpdftweak.jar %*

to for example

@java -Xmx256M -jar jpdftweak.jar %*

to allow for 256 MB of RAM. Alternatively, if you want to perform lots 
of operations, you can check the "Use temporary files" option on the
Output tab.

License
~~~~~~~

jPDF Tweak is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version. See license.txt.

Contact me
~~~~~~~~~~

Please send bug reports and suggestions to <schierlm@users.sourceforge.net>.

ChangeLog
~~~~~~~~~

+++ 2010-11-06 Released version 1.0-rc1 +++

- Formal changes
  * As the program got quite stable, this is the first version that
    is no longer beta but a release candidate.
  * Update to iText 5.0.5, BouncyCastle 1.45 and JGoodies Forms 1.3
  * Due to changes in licensing of the PDF library used by jPDF Tweak
    (iText), the license of the current version changed to GNU Affero
    General Public License version 3 (AGPLv3+). If you know a good reason
    why to create a separate branch that stays GPLv2+ and uses the old
    version of iText, write me - maybe you can convince me.
- Support more than one transformation block at once in the command line 
  interface
- Add support for PDF 1.5 compression (Acrobat 6.0)
- Enable multiselect in the file chooser for multifile and batch modes
- Page number improvements:
  * Add support for printing page labels either as pre-formatted logical
    page numbers (i. e. Roman/Arabic etc.) or as custom formatted logical
    page numbers (e. g. with leading zeroes)
  * Add page number UI on watermark tab to change (printed) page numbers 
    before performing shuffle rule.
  * Add option to print page numbers on inner/outer edge (flip on 
    even/odd pages)
- Bug fixes:
  * Improve validation of page ranges on the input tab
  * Fix handling of dirty cell editors and invalid cell values
  * Fix -ot (temp files) command line option
  * Fix handling of XMP metadata when changing document info
  * Preserve outlines (bookmarks), viewer preferences and page numbers
    when optimizing for size

+++ 2009-07-13 Released version 0.9.5 +++

- update to iText 2.1.7, BouncyCastle 1.43 and JGoodies Forms 1.2.1
- Add option to change mask used for printing page numbers
  (to do things like "Page 2 of 11").
- New "optimize for size" option
- Add an option to save intermediary results into temporary files
- Add color option for text watermark
- Add "Tile Copy" shuffle rules by Stefan Michel
- Show a red ugly dialog box when a fatal exception occurs
- Complain if output file is one of the input files
- Complain when setting permissions without setting an owner password
- bug fixes:
  * Do not show logical page twice (once instead of physical page) when
    loading page numbers.
  * Close all input/output streams when finished.
  * Fix exception when removing elements from the attachment tab
  * Copy info dictionary after every operation that might destroy it.
  * fix a ClassCastException and catch NullPointerException 
    when trying to parse shuffle rules.
  * fix crash when deleting a row in a table when a cell editor is active
  * catch BadPasswordException to only show a password prompt when the reason
    is really a bad password
  * Fix adding new PDF bookmarks to a PDF file that did not have any
  * Fix a typo in "Penguin Small/Large Paperback" page size
  * catch OutOfMemoryError and display an error message


+++ 2007-09-10 Released version 0.9 +++

- update to iText 2.0.5
- Preserve hyperlinks when resizing and shuffling if desired
- Add frames to n-up printings if desired
- Add page label edit support
- Add batch processing support to UI (and more wildcards for output tab)
- Add command line support
- Export bookmarks to CSV, import from CSV or PDF
- Add more predefined shuffle rules
- bug fixes:
  * load unicode strings in info dictionary correctly
  * Encryption GUI fixes:
    + Fix initialization of "Do not encrypt metadata" checkbox
    + fix wrong permissions displayed when using 40-bit encryption;
    + use 128-bit encryption by default;
    + disable unsupported checkboxes when using 40-bit encryption.
  * fix crash when loading chapter bookmarks from pdf without bookmarks
  * fix crash when trying to output pages in reverse order


+++ 2007-04-09 Released version 0.1 +++

- First public release
