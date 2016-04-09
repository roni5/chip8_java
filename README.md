#A CHIP-8 Emulator, written in Java
-----------------------------------

The `CHIP-8` isn't an actual computer, but a language interpreter used on computers from the '70s and '80s, primarily the RCA TELMAC 1800 and the COSMAC VIP, both based on the CDP-1802 CPU. Hobbyist computers saw fairly large use, too. It was mostly used for making programming video games easier. Later, in the '90s, it saw widespread use on calculators as a programming tool for, again, developing video games on them.

This is an emulator for the CHIP-8, written in Java, using LWJGL 2.93 for graphics/input/sound.

#Usage
-------
\# ROM loading is menu-driven.

\# `ESC` button quits the emulator. Closing the LWJGL window does the same.

\# The hexadecimal (0-F) keypad of the original CHIP-8 is mapped on a modern keyboard as follows. Note that keys 2-4-6-8 are used for directional control (arrow keys) in most games.


###ORIGINAL:
--------

`1 2 3 C`

`4 5 6 D`

`7 8 9 E`

`A 0 B F`


###MAPPED:
--------

`1 2 3 4`

`Q W E R`

`A S D F`

`Z X C V`

--------

#Structure
----------
The main entry point for the emulator is at /src/emu/Main.java, which also contains the UI menu.

The "core" engine of the CHIP8 emulator is at /src/chip8/Chip8Core.java.


#Provided ROMs
--------------
Three demos and three games, in .ch8 format. Though, I'm not sure of their availability in the public domain, and will remove them if they are discovered to, in fact, not be as such.

#Compilation
------------
These are .java files, so compilation shouldn't be an issue. The project uses LWJGL 2.93 for openGL implementation in Java, so you'll need to add those if you want to compile the project for yourself. The \lib folder contains these external JAR files, and Windows natives needed for the openGL implementation.


