package com.progjar

import java.io.*
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

const val TEXT_RESET = "\u001B[0m"
const val TEXT_BLACK = "\u001B[30m"
const val TEXT_RED = "\u001B[31m"
const val TEXT_GREEN = "\u001B[32m"
const val TEXT_YELLOW = "\u001B[33m"
const val TEXT_BLUE = "\u001B[34m"
const val TEXT_PURPLE = "\u001B[35m"
const val TEXT_CYAN = "\u001B[36m"
const val TEXT_WHITE = "\u001B[37m"

fun main(args: Array<String>) {
//    println("Hello, World!")
//    println("<a href=\"www.google.com\">Google</a>")
//    println("Google(https://www.google.com)")
//    println(TEXT_RED + "This text is red!" + TEXT_RESET)

//    parseLink( "<a href=\"www.google.com\">google</a>")
//    connectToLink( "classroom.its.ac.id", "/auth/oidc/" )
    connectWithSSL()
}

fun connectToLink( domain: String, route: String ) {
    try {
        // 1. create a new socket object
        var socket:Socket = Socket(domain, 443)

        // 2. obtain the input and output
        var bis = BufferedInputStream( socket.getInputStream() )
        var bos = BufferedOutputStream( socket.getOutputStream() )

        // 3. exchange messages
        bos.write( "GET $route HTTP/1.1\r\nHost: $domain\r\n\r\n".toByteArray() )

        bos.flush()

        //
        var bResp = bis.readAllBytes()
//        var bResp = bis.readNBytes( 100 )
        var resp = String( bResp )
        print( resp )

//        var resp = ""
//        while( bResp.size < 100 ) {
//            resp += bResp.toString()
//            bResp = bis.readNBytes( 100 )
//        }

//        print( resp )

        // 4. close connection
        socket.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun connectWithSSL() {
    try {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        val socket = factory.createSocket("classroom.its.ac.id", 443) as SSLSocket

        /*
         * send http request
         *
         * Before any application data is sent or received, the
         * SSL socket will do SSL handshaking first to set up
         * the security attributes.
         *
         * SSL handshaking can be initiated by either flushing data
         * down the pipe, or by starting the handshaking by hand.
         *
         * Handshaking is started manually in this example because
         * PrintWriter catches all IOExceptions (including
         * SSLExceptions), sets an internal error flag, and then
         * returns without rethrowing the exception.
         *
         * Unfortunately, this means any error messages are lost,
         * which caused lots of confusion for others using this
         * code.  The only way to tell there was an error is to call
         * PrintWriter.checkError().
         */socket.startHandshake()
        val out = PrintWriter(
            BufferedWriter(
                OutputStreamWriter(
                    socket.outputStream
                )
            )
        )
        out.println("GET /auth/oidc/ HTTP/1.1\r\nHost: classroom.its.ac.id\r\n\r\n")
        out.println()
        out.flush()

        /*
         * Make sure there were no surprises
         */if (out.checkError()) println(
            "SSLSocketClient:  java.io.PrintWriter error"
        )

        /* read response */
        val `in` = BufferedReader(
            InputStreamReader(
                socket.inputStream
            )
        )
        var inputLine: String?
        while (`in`.readLine().also { inputLine = it } != null) println(inputLine)
        `in`.close()
        out.close()
        socket.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}