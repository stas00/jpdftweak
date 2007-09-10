jPDF Tweak
==========

This program requires Java 5 or higher. Download it from java.sun.com.

Start it by double clicking the jar file, or by

java -jar jpdftweak.jar

For the command line options, add -help to the command line above.

If you have the compact version and have problems, you might try
if they are present in the "normal" version as well.

See the "manual" folder for short manual.

License
~~~~~~~

jPDF Tweak is licensed under GNU General Public License Version 2,
see license.txt.

Contact me
~~~~~~~~~~

Please send bug reports and suggestions to <schierlm@users.sourceforge.net>.

ChangeLog
~~~~~~~~~

+++ 2007-09-10 Released version 0.9

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
