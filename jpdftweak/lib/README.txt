OBTAINING LIBRARIES
===================

To compile jPDFtweak, you need the following libraries:

* iText, available from http://www.lowagie.com/iText
  Version 5.1.3 or newer
  -> itext.jar

* BouncyCastle (Provider and Mail), available from 
  http://www.bouncycastle.org/latest_releases.html
  Version 1.46 or newer
  the version for JDK 1.5 (bc*-jdk15-146.jar)
  -> bcprov.jar
  -> bcmail.jar
  -> bctsp.jar

* JGoodies Forms, available from http://www.jgoodies.com/freeware/forms/
  Version 1.4.2 or newer
  -> forms.jar

* JGoodies Common, available from http://www.jgoodies.com/downloads/libraries.html
  Version 1.2.1 or newer
  -> jgoodies-common.jar

* JMuPdf, available from http://sourceforge.net/projects/jmupdf/
  Version 0.2b released on 2011/06/02
  -> JMuPdf.jar


You can obtain the precompiled files from the "-shared" binary package,
if you do not want to collect/compile them yourself.

To compile the "-static jar", you will also need proguard, version 4.6,
available from http://proguard.sourceforge.net/
  -> proguard.jar
