#!/bin/sh
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="de.tech42.mensierbot.App" -Dexec.args="MensierBot" 2>&1 | tee -a ../../htdocs/bot.log
