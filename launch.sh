POOL_IP=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1') 

POOL_IP=$POOL_IP docker-compose up -d
echo "StudyBits is lifing off @" $POOL_IP 
