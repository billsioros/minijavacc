
.PHONY: compile
compile:
	java -jar ./jar/jtb132di.jar ./grammar/minijava.jj
	java -jar ./jar/javacc5.jar  ./grammar/minijava-jtb.jj
	javac Main.java

.PHONY: clean
clean:
	find . -maxdepth 5 -name "*.class" -delete -print
	rm -rfv ./syntaxtree ./visitor
	ls *.java | egrep -v "Main" | xargs rm -fv
