# NetChat AI

A lightweight LAN chat application built with Java TCP Socket,
with AI-assisted message improvement.

## Features

- LAN chat using TCP Socket
- Client-Server architecture
- Multiple users
- Online user display
- Group messaging
- AI-assisted message improvement
- Swing graphical user interface
- Automatic reconnection and heartbeat

## Technologies

- Java 17
- Java Swing
- TCP Socket
- JSON-simple
- DeepSeek API
- Multithreading

## Architecture

Client → Chat Server → Other Clients

The client communicates with the server using TCP sockets.
The server receives messages and broadcasts them to connected clients.

For AI Improve, the client sends the selected message to the
DeepSeek API and places the improved result back into the input box.

## How to Run

1. Start `ChatServer`
2. Start one or more `ChatClient` instances
3. Enter a username
4. Connect to the server
5. Send messages
6. Use `AI Improve` to improve a message

## Project Structure

NetChat AI
├── src
│   ├── client
│   │   └── ChatClient.java
│   └── server
│       └── ChatServer.java
├── lib
│   └── json-simple-1.1.1.jar
└── image
    └── background.png

## AI Integration

The AI Improve feature uses the DeepSeek API to rewrite
user messages while preserving their original meaning.

## Future Improvements

- Private messaging
- Better user interface
- Message history
- File sharing
- End-to-end encryption


YouTube video demonstration link: https://www.youtube.com/watch?v=495O5J7SbNY&list=PLPyWB8OX_I30

You can also view the detailed content of the project I submitted on the devpost official website, which can be found at: https://devpost.com/software/netchat-ai
