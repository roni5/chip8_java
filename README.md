#A CHIP-8 Emulator, written in Java
-----------------------------------

The `CHIP-8` isn't an actual computer, but a language interpreter used on computers from the '70s and '80s, primarily the RCA TELMAC 1800 and the COSMAC VIP, both based on the CDP-1802 CPU. Hobbyist computers saw fairly large use, too. It was mostly used for making programming video games easier. Later, in the '90s, it saw widespread use on calculators as a programming tool for, again, developing video games on them.

This is an emulator for the CHIP-8, written in Java, using LWJGL 2.93 for graphics/input/sound.

#Usage
-------
\# ROM loading is menu-driven.

\# `ESC` button quits the emulator. Closing the LWJGL window does the same.

#Provided ROMs
--------------
Three demos and three games, though I'm not sure of their availability in the public domain, and will remove if they are discovered to, in fact, not be in the public domain.

#Compilation
------------
These are .java files, so compilation shouldn't be an issue. The project uses LWJGL 2.93 for openGL implementation in Java, so you'll need to add those if you want to compile this. The \lib folder contains these external JAR files, and Windows natives needed for the openGL implementation.


