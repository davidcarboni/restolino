.PHONY: all
all: audit test build lint

.PHONY: audit
audit:
	mvn ossindex:audit

.PHONY: build
build:
	mvn clean package -Dmaven.test.skip -Dossindex.skip=true

.PHONY: test
test:
	mvn -Dossindex.skip=true test

.PHONY: lint
lint:
	exit

