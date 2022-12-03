# Another Signal Manager

version 2.0.0 , Dec 3 2022

Java version


# What is ASM?

ASM is a program for digital signal processing for educational purposes.

This is a port of the original version made in 1993 by Edwin Zoer and me on an
Acorn Archimedes computer running RISC OS.

# History

## AIM : Another Image Manager

also known as:
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
Ed Doppenberg  was successful in the port to RISC OS. 
AIM has been written in the C-language. 
AIM is limited in functionality as well as in flexibility.
The main  purpose of the program is to experiment with digital
image processing.

The latest version was 3.15 (1995).

On the Polytechnic of Enschede the Archimedes RISC OS version of AIM was used
in practical lessons in image processing.  Polytechnic of Enschede (Hogeschool
Enschede), the Netherlands, is called Saxion hogescholen (www.saxion.nl)
today.


## ASM : Another Signal Manager

In 1993 the idea came to make a program like AIM, but then for signal
processing: ASM for RISC OS.  The task of our final examination for the
Polytechnic of Enschede was to create ASM for RISC OS.

We made ASM at and with support of the Technical University of Delft, faculty
Applied Physics, Pattern Recognition group (Tom Hoeksma), and with support from
the Technisch Physische Dienst, Delft (Ed Doppenberg).  Our starting point was
a stripped down version of AIM made by Ed Doppenberg.  It was only one window
with a command line interpreter. 

In 1993 Edwin and I had only basic knowledge of ANSI C and no knowledge about
making user interfaces for RISC OS. Our goal was to put as much as possible
functionality in the program in only three months.

With the first RISC OS version we created it was possible to generate signals
and do some basic processing on them. The program was made for use during
practical lessons in digital signal processing at the polytechnic in Enschede.

The original intention was that other students would develop ASM further, but
this never happened. It was Ed Doppenberg who did a thorough revision of the
source code and added some professional functionality. That version of ASM (for
RISC OS) is not free available for the public domain.

In 1997 I ported the original version of ASM for RISC OS to DOS using DJGPP
2.01 (gcc 2.7.2). I used the Allegro 2.2 graphics library.  Allegro is a
library intended for making computer games. It was initially conceived on the
Atari ST.

In 1997 my primary goal was to port the program to a working version on DOS, for
the fun of programming and because I had no Archimedes computer.  That means I
only changed the graphical interface. I tried to keep the source code as much
as possible the same.

Allegro development went on, supporting more platforms.  In 2007 I build ASM
for DOS, Windows, and Linux using a newer version of Allegro.  Some minor
problems were fixed. For the rest it was the same version as in 1997.

For many years I had in the back of my mind the idea to make a windowed
version, like the original version on RISC OS. In 2021 I started porting ASM to
Java and JavaFX. In Dec 2022 the port to Java was ready. It is practically the
same as the original version of 1993 with the addition of menus and dialogs for
most of the commands and several bugs fixed.


# Copyright

ASM is Public Domain software.


# Contact/Download

ASM and a manual can be downloaded from:
https://waterlan.home.xs4all.nl/asm.html
GitHub: https://github.com/waterlan/anothersignalmanager

Erwin Waterlander
Eindhoven
The Netherlands
e-mail :   waterlan@xs4all.nl

Remarks are welcome.
