#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
cd /home/ubuntu/carrotmoa
sudo fuser -k -n tcp 8080 || true
# Nginx 재시작
nohup java -Djasypt.encryptor.password=$JASYPT_PASSWORD -jar project.jar > ./output.log 2>&1 &

sleep 10
sudo systemctl restart nginx

echo "--------------- 서버 배포 끝 -----------------"
