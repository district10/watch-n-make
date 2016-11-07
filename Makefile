.PHONY: all clean watcher

all: watcher.jar
clean:
	rm -rf out
	rm md2html.jar
watcher: watcher.jar
	java -jar watcher.jar

dist := out/production/watch-n-make
pack := \
	$(dist)/META-INF/MANIFEST.MF \
	$(dist)/com/tangzhixiong/java/Main.class \

%.class:
	mkdir -p $(dist)
	javac src/com/tangzhixiong/java/*.java -d $(dist)
$(dist)/%: %
	@mkdir -p $(@D)
	cp $< $@
watcher.jar: $(pack)
	(cd $(dist) && zip -r ../../../watcher.jar *)
