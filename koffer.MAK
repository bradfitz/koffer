ORIGIN		= Symantec Café
ORIGIN_VER	= Version 7.22
VERSION		= RELEASE

!IFDEF SUB_DEBUG
DEBUG		= $(SUB_DEBUG)
NDEBUG		= !$(SUB_DEBUG)
!ELSE
DEBUG		= 0
NDEBUG		= 1
!ENDIF

PROJ		= koffer
APPTYPE		= JAVA APPLET
PROJTYPE	= class

JAVAC		= sj
MAKE		= SMAKE
LNK		= LINK


HEADERS		= 
!IF $(DEBUG)
OUTPUTDIR	= .
CREATEOUTPUTDIR	=
TARGETDIR	= .
CREATETARGETDIR	=

CFLAGS		=  -g 
DEFINES		= 
!ELSE
OUTPUTDIR	= .
CREATEOUTPUTDIR	=
TARGETDIR	= .
CREATETARGETDIR	=

CFLAGS		=  -O 
DEFINES		= 
!ENDIF

MFLAGS		= MASTERPROJ=$(PROJ) 
DEBUGGERFLAGS	=  
PAR		= PROJS BATS SRCS

INCLUDES	= 



SRCS		= koffer.java klevel.java suitcase.java  \
		darts.java intro.java plane.java train.java  \
		drive.java bcLabel.java bcManager.java socks.java  \
		dimFilter.java clearFilter.java whack.java end.java 

BATS		= 

.SUFFIXES: .JAVA .CLASS 

.JAVA.CLASS:
	$(JAVAC) $(CFLAGS) $(INCLUDES) $*.java



all:	noteout createdir $(PRECOMPILE) compall $(POSTCOMPILE) _done


all2:	createdir $(PRECOMPILE) compall $(POSTCOMPILE) _done


all3:	createdir $(PRECOMPILE) compall2 $(POSTCOMPILE) _done


compall:	$(OBJS)
	$(JAVAC) $(CFLAGS) $(INCLUDES) -make -cdb $(PROJ).cdb @<<
	$(SRCS)
<<


compall2:	$(OBJS)
	$(JAVAC) $(CFLAGS) $(INCLUDES) -cdb $(PROJ).cdb @<<
	$(SRCS)
<<

noteout:
	REM Output to $(OUTPUTDIR)

createdir:
	$(CREATEOUTPUTDIR)
	$(CREATETARGETDIR)
	
_done:
		REM  Project is up to date

buildall:	clean	all3


clean:
		-del $(TARGETDIR)\$$SCW$$.$(PROJTYPE)
		-del $(TARGETDIR)\$(PROJ).CLE
		-del koffer.dpd
		-del $(OBJS)




