OBTAINING LIBRARIES
===================

To compile jPDFtweak, you need the following libraries:

* iText, available from http://www.lowagie.com/iText
  Version 5.0.5 or newer
  -> itext.jar

* BouncyCastle (Provider and Mail), available from 
  http://www.bouncycastle.org/latest_releases.html
  Version 1.45 or newer
  the version for JDK 1.4 (bc*-jdk14-145.jar)
  -> bcprov.jar
  -> bcmail.jar
  -> bctsp.jar

* JGoodies Forms, available from http://www.jgoodies.com/freeware/forms/
  Version 1.3.0 or newer
  -> forms.jar
  
* JMuPdf, available from http://sourceforge.net/projects/jmupdf/
  Version published on 2011/03/20 or newer
  -> JMuPdf.jar
  

You can obtain the precompiled files from the "-shared" binary package,
if you do not want to collect/compile them yourself.

To compile the "-static jar", you will also need proguard, version 4.5,
available from http://proguard.sourceforge.net/
  -> proguard.jar