all: p1 p2
CC = gcc
p1: hello_server.c
	$(CC) -o server hello_server.c
p2: hello_client.c
	$(CC) -o client hello_client.c
clean:
	-rm server client

