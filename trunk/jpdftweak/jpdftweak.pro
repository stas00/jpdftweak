-injars       build/jpdftweak.jar
-injars       build/libs.jar
-outjars      jpdftweak-static.jar
-libraryjars  <java.home>/lib/rt.jar
-libraryjars  <java.home>/lib/jce.jar
-printmapping jPdfTweak.map
-overloadaggressively
-defaultpackage ''
-allowaccessmodification
-dontskipnonpubliclibraryclasses

-keep public class jpdftweak.Main {
    public static void main(java.lang.String[]);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}