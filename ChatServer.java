import socket
import threading

class ChatServer:
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.clients = {}
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.bind((self.host, self.port))

    def start(self):
        self.server.listen()
        print(f"サーバーが{self.host}:{self.port}で起動しました")
        while True:
            client, address = self.server.accept()
            threading.Thread(target=self.handle_client, args=(client,)).start()

    def handle_client(self, client):
        username = client.recv(1024).decode('utf-8')
        self.clients[client] = username
        self.broadcast(f"{username}が参加しました")

        while True:
            try:
                message = client.recv(1024)
                if message:
                    self.broadcast(message.decode('utf-8'), username)
                else:
                    self.remove_client(client)
                    break
            except:
                self.remove_client(client)
                break

    def broadcast(self, message, sender=None):
        for client in self.clients:
            if sender:
                client.send(f"{sender}: {message}".encode('utf-8'))
            else:
                client.send(message.encode('utf-8'))

    def remove_client(self, client):
        if client in self.clients:
            username = self.clients[client]
            del self.clients[client]
            self.broadcast(f"{username}が退出しました")
            client.close()

class ChatClient:
    def __init__(self, host, port):
        self.host = host
        self.port = port
        self.client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def connect(self, username):
        self.client.connect((self.host, self.port))
        self.client.send(username.encode('utf-8'))
        threading.Thread(target=self.receive_messages).start()

    def send_message(self, message):
        self.client.send(message.encode('utf-8'))

    def receive_messages(self):
        while True:
            try:
                message = self.client.recv(1024).decode('utf-8')
                print(message)
            except:
                print("サーバーとの接続が切断されました")
                self.client.close()
                break

# サーバーの使用例
# server = ChatServer('localhost', 5555)
# server.start()

# クライアントの使用例
# client = ChatClient('localhost', 5555)
# client.connect('ユーザー名')
# client.send_message('こんにちは、世界！')
