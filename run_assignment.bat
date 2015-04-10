cls
SET DIR_FOR_CLASSES="./src"
SET CLASSES="./src;./libs/jogl-2.0-windows-amd64/lib/gluegen-rt.jar;./libs/jogl-2.0-windows-amd64/lib/jogl.all.jar;./libs/jogl-2.0-windows-amd64/lib/newt.all.jar;./libs/jogl-2.0-windows-amd64/lib/nativewindow.all.jar"
SET NATIVE_LIBRARIES="./libs/jogl-2.0-windows-amd64/lib"
dir /s /B *.java > sources.txt
javac -Xlint:unchecked -classpath %CLASSES% -d %DIR_FOR_CLASSES% @sources.txt
del sources.txt
java -Djava.library.path=%NATIVE_LIBRARIES% -cp %CLASSES% assignment.House
pause