JFLAGS = -g
JC = javac

.SUFFIXES: .java .class

.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
        Move.java \
        Parking.java \
        Escape.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
