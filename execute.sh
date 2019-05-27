#!/bin/bash

challengeId=$1
testPhase=$2
submissionId=$3

chmod +x /workdir/solution.sh

/workdir/solution.sh

command=`cat /workdir/command.txt`

rm $tempFile

java -jar /tester/target/ReviewTest-0.0.1-SNAPSHOT.jar $challengeId $testPhase $submissionId "$command"
