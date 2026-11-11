JFLAGS=-cp .
JAR=jar
JAVA=java
JAVAC=javac
JAVACC=javacc

PARSEDIR=Parse
MJ_PARSER=MiniJavaParser

SEMANTDIR=Semant
RA_PARSER=ReadAbsyn

TRANSDIR=Translate
RT_PARSER=ReadTypes

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

RT_JAVACCGEN = $(TRANSDIR)/SimpleCharStream.java \
        $(TRANSDIR)/ParseException.java \
        $(TRANSDIR)/Token.java \
        $(TRANSDIR)/TokenMgrError.java \
        $(TRANSDIR)/$(RT_PARSER).java \
        $(TRANSDIR)/$(RT_PARSER)Constants.java \
        $(TRANSDIR)/$(RT_PARSER)TokenManager.java

# Project 4 - Parser + Checker

ASTSRC    = $(wildcard Absyn/*.java)
TYPESRC   = $(wildcard Types/*.java)
SYMSRC    = $(wildcard Symbol/*.java)
SEMANTSRC = Semant/Main.java Semant/TypeChecker.java Semant/SubPrintVisitor.java

# Project 7 - Translate
TEMPSRC   = $(wildcard Temp/*.java)
TREESRC   = $(wildcard Tree/*.java)
FRAMESRC  = $(wildcard Frame/*.java)
RISCVSRC  = $(wildcard Riscv/*.java)
TRANSSRC  = $(filter-out $(RT_JAVACCGEN),$(wildcard Translate/*.java))

PARSESRC = Parse/Main.java $(ASTSRC)
ALLSRC   = $(PARSESRC) $(TYPESRC) $(SYMSRC) $(SEMANTSRC) \
        $(TEMPSRC) $(TREESRC) $(FRAMESRC) $(RISCVSRC) $(TRANSSRC)

all:        $(PARSEDIR)/$(MJ_PARSER).java $(SEMANTDIR)/$(RA_PARSER).java \
        $(TRANSDIR)/$(RT_PARSER).java $(ALLSRC:.java=.class)

$(PARSEDIR)/$(MJ_PARSER).java: $(PARSEDIR)/$(MJ_PARSER).jj
	cd $(PARSEDIR); ${JAVACC} $(MJ_PARSER).jj;
	${JAVAC} ${JFLAGS} ${MJ_JAVACCGEN}

$(SEMANTDIR)/$(RA_PARSER).java: $(SEMANTDIR)/$(RA_PARSER).jj
	cd $(SEMANTDIR); ${JAVACC} $(RA_PARSER).jj;
	${JAVAC} ${JFLAGS} ${RA_JAVACCGEN}

$(TRANSDIR)/$(RT_PARSER).java: $(TRANSDIR)/$(RT_PARSER).jj
		cd $(TRANSDIR);	${JAVACC} $(RT_PARSER).jj;
		${JAVAC} ${JFLAGS} ${RT_JAVACCGEN}

%.class : %.java
	${JAVAC} ${JFLAGS} $<

clean:
	rm -f $(ALLSRC:.java=.class) \
		$(MJ_JAVACCGEN) $(MJ_JAVACCGEN:.java=.class) \
		$(RA_JAVACCGEN) $(RA_JAVACCGEN:.java=.class) \
		$(RT_JAVACCGEN) $(RT_JAVACCGEN:.java=.class) \
                '$(PARSEDIR)/$(MJ_PARSER)$$JJCalls.class' \
                '$(PARSEDIR)/$(MJ_PARSER)$$LookaheadSuccess.class' \
                '$(SEMANTDIR)/$(RA_PARSER)$$JJCalls.class' \
                '$(SEMANTDIR)/$(RA_PARSER)$$LookaheadSuccess.class' \
                '$(TRANSDIR)/$(RT_PARSER)$$JJCalls.class' \
                '$(TRANSDIR)/$(RT_PARSER)$$LookaheadSuccess.class'

realclean: clean
	rm -f */*.class *~ */*~ *.jar
