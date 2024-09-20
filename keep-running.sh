#!/bin/sh -l

echo "Hi $1"
time=$(date)
echo "time=$time" >> $GITHUB_OUTPUT
