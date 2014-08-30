#!/bin/sh
runTime=$(date +%Y:%m:%d-%H:%M:%S)
if screen -list | grep -q "NodeServer"; then
#   screen -S "NodeServer" -X quit
    echo $runTime " node server is running, exit with no action"
    exit 0
else
    echo $runTime " No previous screen, start a new node server "
fi
screen -dm -S "NodeServer" ./startNode.sh
