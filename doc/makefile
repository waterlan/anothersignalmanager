

all : asm.pdf

asm.pdf : asm.tex
	latex asm.tex
	latex asm.tex
	dvipdfm asm.dvi



clean :
	rm -f asm.toc asm.log asm.aux asm.dvi asm.pdf

distclean :
	rm -f asm.toc asm.log asm.aux asm.dvi
