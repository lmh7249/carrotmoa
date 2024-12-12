#!/bin/bash

echo "--------------- 서버 배포 시작 -----------------"
cd /home/ubuntu/carrotmoa
sudo fuser -k -n tcp 8080 || true
# Nginx 재시작

# 명시적으로 환경변수 설정
export JASYPT_PASSWORD="carrotsS3"

# 디버깅을 위한 로그 추가
echo "JASYPT_PASSWORD: $JASYPT_PASSWORD"
nohup java -Djasypt.encryptor.password="$JASYPT_PASSWORD" -jar project.jar > ./output.log 2>&1 &

sleep 11
sudo systemctl restart nginx

echo "--------------- 서버 배포 끝 -----------------"
