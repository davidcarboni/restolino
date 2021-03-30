.PHONY: all
all: audit test build

.PHONY: audit
audit:
	mvn ossindex:audit

.PHONY: build
build:
	mvn -Dmaven.test.skip -Dossindex.skip=true clean package

.PHONY: test
test:
	mvn -Dossindex.skip=true test

