JFLAGS=-cp .
JAR=jar
JAVA=java
JAVAC=javac
JAVACC=javacc

PARSEDIR=Parse
MJ_PARSER=MiniJavaParser

SEMANTDIR=Semant
RA_PARSER=ReadAbsyn

MJ_JAVACCGEN = $(PARSEDIR)/SimpleCharStream.java \
	$(PARSEDIR)/ParseException.java \
	$(PARSEDIR)/Token.java \
	$(PARSEDIR)/TokenMgrError.java \
	$(PARSEDIR)/$(MJ_PARSER).java \
	$(PARSEDIR)/$(MJ_PARSER)Constants.java \
	$(PARSEDIR)/$(MJ_PARSER)TokenManager.java

RA_JAVACCGEN = $(SEMANTDIR)/SimpleCharStream.java \
	$(SEMANTDIR)/ParseException.java \
	$(SEMANTDIR)/Token.java \
	$(SEMANTDIR)/TokenMgrError.java \
	$(SEMANTDIR)/$(RA_PARSER).java \
	$(SEMANTDIR)/$(RA_PARSER)Constants.java \
	$(SEMANTDIR)/$(RA_PARSER)TokenManager.java


# Project 4 - Parser + Checker

ASTSRC    = $(wildcard Absyn/*.java)
TYPESRC   = $(wildcard Types/*.java)
SYMSRC    = $(wildcard Symbol/*.java)
SEMANTSRC = Semant/Main.java Semant/TypeChecker.java

PARSESRC = Parse/Main.java $(ASTSRC)
ALLSRC   = $(PARSESRC) $(TYPESRC) $(SYMSRC) $(SEMANTSRC)

all:	$(PARSEDIR)/$(MJ_PARSER).java $(SEMANTDIR)/$(RA_PARSER).java $(ALLSRC:.java=.class)

$(PARSEDIR)/$(MJ_PARSER).java: $(PARSEDIR)/$(MJ_PARSER).jj
	cd $(PARSEDIR); ${JAVACC} $(MJ_PARSER).jj;
	${JAVAC} ${JFLAGS} ${MJ_JAVACCGEN}

$(SEMANTDIR)/$(RA_PARSER).java: $(SEMANTDIR)/$(RA_PARSER).jj
	cd $(SEMANTDIR); ${JAVACC} $(RA_PARSER).jj;
	${JAVAC} ${JFLAGS} ${RA_JAVACCGEN}

%.class : %.java
	${JAVAC} ${JFLAGS} $<

clean:
	rm -f $(ALLSRC:.java=.class) \
		$(MJ_JAVACCGEN) $(MJ_JAVACCGEN:.java=.class) \
		$(RA_JAVACCGEN) $(RA_JAVACCGEN:.java=.class) \
                '$(PARSEDIR)/$(MJ_PARSER)$$JJCalls.class' \
                '$(PARSEDIR)/$(MJ_PARSER)$$LookaheadSuccess.class' \
                '$(SEMANTDIR)/$(RA_PARSER)$$JJCalls.class' \
                '$(SEMANTDIR)/$(RA_PARSER)$$LookaheadSuccess.class'

realclean: clean
	rm -f */*.class *~ */*~ *.jar
