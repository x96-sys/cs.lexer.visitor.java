BUILD_DIR     = build
MAIN_BUILD    = $(BUILD_DIR)/main
TEST_BUILD    = $(BUILD_DIR)/test

SRC_MAIN      = src/main
SRC_TEST      = src/test

TOOL_DIR      = tools
LIB_DIR       = lib

JUNIT_VERSION = 1.13.4
JUNIT_JAR     = $(TOOL_DIR)/junit-platform-console-standalone.jar
JUNIT_URL     = https://maven.org/maven2/org/junit/platform/junit-platform-console-standalone/$(JUNIT_VERSION)/junit-platform-console-standalone-$(JUNIT_VERSION).jar

JACOCO_VERSION = 0.8.13
JACOCO_BASE    = https://maven.org/maven2/org/jacoco

JACOCO_CLI_VERSION = $(JACOCO_VERSION)
JACOCO_CLI_JAR     = $(TOOL_DIR)/jacococli.jar
JACOCO_CLI_URL     = $(JACOCO_BASE)/org.jacoco.cli/$(JACOCO_CLI_VERSION)/org.jacoco.cli-$(JACOCO_CLI_VERSION)-nodeps.jar

JACOCO_AGENT_VERSION = $(JACOCO_VERSION)
JACOCO_AGENT_JAR     = $(TOOL_DIR)/jacocoagent-runtime.jar
JACOCO_AGENT_URL     = $(JACOCO_BASE)/org.jacoco.agent/$(JACOCO_AGENT_VERSION)/org.jacoco.agent-$(JACOCO_AGENT_VERSION)-runtime.jar

GJF_VERSION = 1.28.0
GJF_JAR     = $(TOOL_DIR)/google-java-format.jar
GJF_URL     = https://maven.org/maven2/com/google/googlejavaformat/google-java-format/$(GJF_VERSION)/google-java-format-$(GJF_VERSION)-all-deps.jar

CS_FLUX_VERSION = 1.0.1
CS_FLUX_JAR     = $(LIB_DIR)/org.x96.sys.foundation.io.jar
CS_FLUX_URL     = https://github.com/x96-sys/flux.java/releases/download/v$(CS_FLUX_VERSION)/org.x96.sys.foundation.io.jar

CS_KIND_VERSION = 0.1.3
CS_KIND_JAR     = $(LIB_DIR)/org.x96.sys.foundation.cs.lexer.token.kind.jar
CS_KIND_URL     = https://github.com/x96-sys/cs.lexer.token.kind.java/releases/download/0.1.3/org.x96.sys.foundation.cs.lexer.token.kind.jar

CS_TOKEN_VERSION = 0.1.3
CS_TOKEN_JAR     = $(LIB_DIR)/org.x96.sys.foundation.cs.lexer.token.jar
CS_TOKEN_URL     = https://github.com/x96-sys/cs.lexer.token.java/releases/download/v0.1.3/org.x96.sys.foundation.cs.lexer.token.jar

CS_TOKENIZER_VERSION = 0.1.6
CS_TOKENIZER_JAR     = $(LIB_DIR)/org.x96.sys.foundation.cs.lexer.tokenizer.jar
CS_TOKENIZER_URL     = https://github.com/x96-sys/cs.lexer.tokenizer.java/releases/download/v$(CS_TOKENIZER_VERSION)/org.x96.sys.foundation.cs.lexer.tokenizer.jar

CS_ROUTER_VERSION = 0.1.2
CS_ROUTER_JAR     = $(LIB_DIR)/org.x96.sys.foundation.cs.lexer.router.jar
CS_ROUTER_URL     = https://github.com/x96-sys/cs.lexer.router.java/releases/download/v$(CS_ROUTER_VERSION)/org.x96.sys.foundation.cs.lexer.router.jar

CS_AST_VERSION = 0.1.2
CS_AST_JAR = $(LIB_DIR)/org.x96.sys.foundation.cs.ast.jar
CS_AST_URL = https://github.com/x96-sys/cs.ast.java/releases/download/v0.1.2/org.x96.sys.foundation.cs.ast.jar

JAVA_SOURCES = $(shell find $(SRC_MAIN) -name "*.java")

DISTRO_JAR = org.x96.sys.foundation.cs.lexer.visitor.jar

CP = $(CS_AST_JAR):$(CS_FLUX_JAR):$(CS_TOKEN_JAR):$(CS_KIND_JAR):$(CS_TOKENIZER_JAR):$(CS_ROUTER_JAR)

gen-terminal-visitors:
	@echo ruby -v
	@echo "🔧 Gerando Terminal Visitors..."
	@ruby scripts/visitors.rb
	@echo "✅ Kind gerado com sucesso!"

build: gen-build-info libs | $(MAIN_BUILD)
	@javac --version
	@javac -d $(MAIN_BUILD) -cp $(CP) $(JAVA_SOURCES)
	@echo "✅ Compilação concluída com sucesso!"

build-test:
	@javac -cp $(MAIN_BUILD):$(JUNIT_JAR):$(CP) -d $(TEST_BUILD) \
     $(shell find $(SRC_TEST) -name "*.java")
	@echo "✅ Compilação de testes concluída com sucesso!"

test: build-test
	@java -jar $(JUNIT_JAR) \
     execute \
     --class-path $(TEST_BUILD):$(MAIN_BUILD):$(CP) \
     --scan-class-path

coverage-run: build-test tools/jacoco
	@java -javaagent:$(JACOCO_AGENT_JAR)=destfile=$(BUILD_DIR)/jacoco.exec \
       -jar $(JUNIT_JAR) \
       execute \
       --class-path $(TEST_BUILD):$(MAIN_BUILD):$(CP) \
       --scan-class-path

coverage-report: tools/jacoco
	@java -jar $(JACOCO_CLI_JAR) report \
     $(BUILD_DIR)/jacoco.exec \
     --classfiles $(MAIN_BUILD) \
     --sourcefiles $(SRC_MAIN) \
     --html $(BUILD_DIR)/coverage \
     --name "Coverage Report"

coverage: coverage-run coverage-report
	@echo "✅ Relatório de cobertura disponível em: build/coverage/index.html"
	@echo "🌐 Abrir com: open build/coverage/index.html"

test-method: build-test ## Executa teste específico (METHOD="org.x96.sys.foundation.cs.lexer.visitor.entry.terminals.c4.LatinCapitalLetterITest#happy")
	@echo "🧪 Executando teste: $(METHOD)"
	@java -jar $(JUNIT_JAR) --class-path $(TEST_BUILD):$(MAIN_BUILD):$(CP) --select "method:$(METHOD)"

test-class: build-test ## Executa classe de teste (CLASS="nome.da.Classe")
	@echo "🧪 Executando classe: $(CLASS)"
	@java -jar $(JUNIT_JAR) --class-path $(TEST_BUILD):$(MAIN_BUILD):$(CP) --select "class:$(CLASS)"

distro:
	@jar cf $(DISTRO_JAR) -C $(MAIN_BUILD) .
	@echo "✅ Distribuição criada com sucesso! $(DISTRO_JAR)"

tools/jacoco: tools/jacoco_cli tools/jacoco_agent

# Gera automaticamente o arquivo BuildInfo.java
gen-build-info:
	@ruby -v
	@echo "🔧 Gerando BuildInfo..."
	@mkdir -p $(SRC_MAIN)/org/x96/sys/foundation/visitor/
	@ruby scripts/build_info.rb \
		           "org.x96.sys.foundation.visitor" > \
	  $(SRC_MAIN)/org/x96/sys/foundation/visitor/BuildInfo.java
	@echo "✅ BuildInfo gerado com sucesso!"

format: tools/gjf ## Formata todo o código fonte Java com google-java-format
	find src -name "*.java" -print0 | xargs -0 java -jar $(GJF_JAR) --aosp --replace

$(LIB_DIR) $(TOOL_DIR) $(MAIN_BUILD) $(TEST_BUILD):
	@mkdir -p $@

define deps
$1/$2: $1
	@if [ ! -f "$$($3_JAR)" ]; then \
		echo "[📦] [🚛] [$$($3_VERSION)] [$2]"; \
		curl -sSL -o $$($3_JAR) $$($3_URL); \
	else \
		echo "[📦] [📍] [$$($3_VERSION)] [$2]"; \
	fi
endef

libs: lib/flux lib/cs-token lib/cs-kind lib/cs-tokenizer lib/cs-router lib/cs-ast

$(eval $(call deps,lib,flux,CS_FLUX))
$(eval $(call deps,lib,cs-token,CS_TOKEN))
$(eval $(call deps,lib,cs-tokenizer,CS_TOKENIZER))
$(eval $(call deps,lib,cs-kind,CS_KIND))
$(eval $(call deps,lib,cs-router,CS_ROUTER))
$(eval $(call deps,lib,cs-ast,CS_AST))

kit: tools/gjf tools/junit tools/jacoco_cli tools/jacoco_agent

$(eval $(call deps,tools,gjf,GJF))
$(eval $(call deps,tools,junit,JUNIT))
$(eval $(call deps,tools,jacoco_cli,JACOCO_CLI))
$(eval $(call deps,tools,jacoco_agent,JACOCO_AGENT))

clean:
	@rm -rf $(BUILD_DIR) $(TOOL_DIR) $(LIB_DIR)
	@echo "[🧹] [clean] Build directory cleaned."
