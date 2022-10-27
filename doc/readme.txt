

     Another Signal Manager

  (Java version)


     version 2.0.0 alpha1 , Oct 21 2022

     By Erwin Waterlander


Contents:

 1 What is ASM?
 2 History
 3 User interface
 4 Wishes/To do
 5 Copyright
 6 Contact/Download

======================================================
1 What is ASM?
======================================================

ASM is a program for digital signal processing for educational
purposes.

This is a port of the original version made in 1993 by Edwin Zoer and me on an
Acorn Archimedes computer running RISC-OS.

======================================================
2 History
======================================================

AIM : ANOTHER IMAGE MANAGER

     also known as

Atari Image Manager,
Archimedes Image Manager,
Amiga Image Manager. 

The image processing program AIM was originally developed for the 
ATARI-ST  by Frans Groen and  Robert de Vries.  Since  the  first 
version  of  AIM, the improvement of  this  public  domain  image 
processing  package  has  become a joint effort of  a  number  of 
people from the Delft University of Technology and the University 
of  Amsterdam.
AIM has been ported to the ARCHIMEDES (Arthur version) by  Robert
Ellens, Damir Sudar and Alle-Jan van der Veen.
Ed Doppenberg  was successful in the port to RISC-OS. 
AIM has been written in the C-language. 
AIM is limited in functionality as well as in flexibility.
The main  purpose of the program is to experiment with digital
image processing.

The latest version 3.15 (1995) for Archimedes RISC-OS can be downloaded
from http://wuerthner.dyndns.org/others.html

On the Polytechnic of Enschede the Archimedes RISC-OS version of AIM was used
in practical lessons in image processing.  Polytechnic of Enschede (Hogeschool
Enschede), the Netherlands, is called Saxion hogescholen (www.saxion.nl)
today.

ASM : ANOTHER SIGNAL MANAGER

In 1993 the idea came to make a program like AIM, but then for signal
processing: ASM for RISC-OS.  The task of our final examination for the
Polytechnic of Enschede was to create ASM for RISC-OS.

We made ASM at and with support of the Technical University of Delft, faculty
Applied Physics, Pattern Recognition group (Tom Hoeksma), and with support from
the Technisch Physische Dienst, Delft (Ed Doppenberg).  Our starting point was
a stripped down version of AIM made by Ed Doppenberg.  It was only one window
with a command line interpreter. 

In 1993 Edwin and I had only basic knowledge of ANSI C and no knowledge about
making user interfaces for RISC-OS. Our goal was to put as much as possible
functionality in the program in only three months.

With the first RISC-OS version we created it was possible to generate signals
and do some basic processing on them. The program was made for use during
practical lessons in digital signal processing at the polytechnic in Enschede.

The original intention was that other students would develop ASM further. But
it was Ed Doppenberg who did a thorough revision of the source code and added
some professional functionality. That version of ASM (for RISC-OS) is not free
available for the public domain.

In 1997 I ported the first version of ASM for RISC-OS to DOS using DJGPP 2.01
(gcc 2.7.2). See djgpp.txt. I used the Allegro 2.2 graphics library.
Allegro is a library intended for use in computer games. It was initially
conceived on the Atari ST. See allegro.txt.

In 1997 my primary goal was to port the program to a working version on DOS, for
the fun of programming and because I had no Archimedes computer.  That means I
only changed the graphical interface. I tried to keep the source code as much
as possible the same.  There are large changes in file plotutil.c. Files aim.c
and command.c have been replaced by asm.c.

Allegro development went on, supporting more platforms.  In 2007 I build ASM
also for Windows and Linux.  I replaced some deprecated Allegro api calls with
new ones, and build ASM for DOS, Windows and Linux. A few minor problems have
been fixed. For the rest it's the same version as in 1997.

For many years I had in the back of my mind the idea to make a windowed
version, like the original version on RiscOS. In 2021 I started porting ASM to
Java. In Oct 2022 the first alpha version was ready.

======================================================
3 User interface
======================================================

Graphical interface

ASM for RISC-OS has a windowed interface. One command line window and
each signal shown in a separate window with scroll bars.

Due to the limitations of DOS (no window environment) I had to change the
graphical interface. ASM for DOS can display only one signal at a time. Other
signals stay resident in memory.

The Java version has a windowed interface. It also adds menus for most of the
commands.


Command line interface

ASM has the same command line interface as AIM.

Commands can be abbreviated as long as they stay unique.

Parameters are separated by spaces.
If you don't give all the parameters that are possible on
a certain command ASM will take default values.


======================================================
4 Wishes/To do
======================================================

- Better and more support of wave sound fils
  (RIFF WAV format).
  Read, write, play.


======================================================
5 Copyright
======================================================

ASM is Public Domain software.

======================================================
6 Contact/Download
======================================================

ASM and a manual in PDF format
can be downloaded from:
https://waterlan.home.xs4all.nl/

Erwin Waterlander
Eindhoven
The Netherlands
e-mail :   waterlan@xs4all.nl

Remarks are welcome.
