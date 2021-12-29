package com.company.Server.models;

import com.company.Server.ClientHandler;

import java.io.IOException;
import java.io.OutputStream;

public class Response {
    private int status;
    private String response;

    public Response(int status, String response) {
        this.status = status;
        this.response = response;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void sendResponseHeaders(ClientHandler client) throws IOException {
        System.out.println("SEND RESPONSE");
        OutputStream clientOutput = client.getClient().getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        clientOutput.write(("Content-Length: -1").getBytes());
        clientOutput.flush();
        client.getClient().close();
    }

    public void sendResponse(ClientHandler client) throws IOException {
        System.out.println("RESPONSE: " + this.response.length() + ", " + this.response);
        OutputStream clientOutput = client.getClient().getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        clientOutput.write(("Content-Length: " + this.response.getBytes().length + "\r\n").getBytes());
        clientOutput.write(("Content-Type: application/json" + "\r\n\r\n").getBytes());
        clientOutput.write((this.response).getBytes());
        clientOutput.flush();
        client.getClient().close();
    }

    public void sendPlain(ClientHandler client) throws IOException {
        System.out.println("RESPONSE: " + this.response.length() + ", " + this.response);
        OutputStream clientOutput = client.getClient().getOutputStream();
        clientOutput.write(("HTTP/1.1 " + status + "\r\n").getBytes());
        clientOutput.write(("Content-Length: " + this.response.getBytes().length + "\r\n").getBytes());
        clientOutput.write(("Content-Type: text/plain" + "\r\n\r\n").getBytes());
        clientOutput.write((this.response).getBytes());
        clientOutput.flush();
        client.getClient().close();
    }
}
