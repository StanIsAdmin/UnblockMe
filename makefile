JFLAGS = -g
JCOMP = javac
SOURCES = src
BINARIES = bin

CLASSES = \
        $(SOURCES)/Move.java \
        $(SOURCES)/Parking.java \
        $(SOURCES)/Escape.java

default: all

all: 
		mkdir -p bin
		$(JCOMP) $(JFLAGS) $(SOURCES)/*.java -d $(BINARIES)

clean:
		rm -r $(BINARIES)
