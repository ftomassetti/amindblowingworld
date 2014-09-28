#!/bin/bash

nohup /home/cloudsigma/bin/lein ring server-headless 80 >> server.log 2>&1 &
