#!/bin/sh -l

echo "Hi $1"
time=$(date)
echo "Finished build at $time" >> $GITHUB_OUTPUT
