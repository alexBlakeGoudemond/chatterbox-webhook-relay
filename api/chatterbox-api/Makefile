# NOTE: If Emojis arent showing in terminal, add these settings to .bashrc file in Users Directory:
# settings for emojis
#export LC_ALL=en_US.UTF-8
#export LANG=en_US.UTF-8
#export LESSCHARSET=utf-8

# Variables
APP_NAME = chatterbox
SRC_DIR = src
JAVA21=C:\myprograms\java\jdk\jdk21.0.7
NGROK_PORT=3002
NGROK_LOG=tmp/ngrok.log
NGROK_PID=tmp/ngrok.pid
NGROK_URL=unswaying-margeret-polymorphonuclear.ngrok-free.dev

# Default target if you just run "make"
all: help

help:
	@echo "'Make Help': Specify an argument: make <argument>"

# dockerissimo: custom script to compose containers
docker-up:
	dockerissimo -f docker/docker-compose.yml -p chatterbox-container-group

# dockerissimo: custom script to delete containers and images
docker-down:
	dockerissimo -f docker/docker-compose.yml -p chatterbox-container-group -c

docker-exec-nginx:
	docker exec -it chatterbox-nginx bash

localtunnel-up:
	lt --port 3002 --subdomain chatterbox

docker-redo:
	$(MAKE) docker-down
	$(MAKE) docker-up

mvn-clean-install:
	JAVA_HOME="$(JAVA21)" mvn clean install

ngrok-start-no-web-ui:
	@echo -e "\u2B06 Starting up Ngrok";
	nohup ngrok http $(NGROK_PORT) > $(NGROK_LOG) 2>&1 & \
	echo $$! > $(NGROK_PID)
	sleep 1
	$(MAKE) ngrok-status

ngrok-stop-no-web-ui:
	@echo -e "\u2B07 Stopping Ngrok";
	-kill `cat $(NGROK_PID)`
	rm -f $(NGROK_PID)
	$(MAKE) ngrok-status

ngrok-status:
	@if [ -f $(NGROK_PID) ] && ps -p $$(cat $(NGROK_PID)) > /dev/null 2>&1 ; then \
		echo -e "\U0001F45F  Ngrok is running (PID=$$(cat $(NGROK_PID)))"; \
	else \
		echo -e "\u270B  Ngrok is NOT running"; \
	fi

live-webhook-test:
	@echo -e "\U0001F310  Simulating Live payload being sent to public URL"
	curl -X POST https://$(NGROK_URL)/chatterbox/github -H "Content-Type: application/json" -H "X-GitHub-Event: push" -H "X-GitHub-Delivery: test123" -H "X-Hub-Signature-256: sha256=2677ad3e7c090b2fa2c0fb13020d66d5420879b8316eb356a2d60fb9073bc778" -d '{"hello":"world"}'

# Use `-s` to not echo before executing (silent)
spin-up:
	$(MAKE) docker-up
	$(MAKE) ngrok-start
	$(MAKE) live-webhook-test

# `\l` → list all databases
# `\c` <db_name> → connect to a database
# `\dt` → list all tables in the current database
# `\d` <table_name> → describe a table (columns, types, indexes)
# `\q` → quit psql
postgres-login:
	docker exec -it chatterbox-postgres psql -U user -d chatterbox

count-tests:
    grep -R "@\(Test\|ParameterizedTest\|RepeatedTest\|TestFactory\)" src/test/java | wc -l
