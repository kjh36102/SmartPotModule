import socket

# UDP IP and port
UDP_IP = "192.168.0.255"  # or you could use your specific broadcast IP
UDP_PORT = 12345

# create a socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM) 

# this is a broadcast socket
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

# message to be sent
message = "Hello, world!"

import time

while True:
    # send message
    sock.sendto(message.encode(), (UDP_IP, UDP_PORT))
    time.sleep(1)
    
