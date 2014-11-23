#!/bin/sh
screen java -server -XX:-UseConcMarkSweepGC -XX:-UseParallelGC -Xms256m -Xmx256m  -jar start.jar
