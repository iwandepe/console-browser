package com.progjar

import java.io.*
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

var url = "localhost"

const val HTTP_VERSION = "HTTP/1.1"
const val REQUEST_TIMEOUT: Long = 2

var responseHeader = mutableMapOf<String, String>()
var responseBody: String = ""

fun main() {
    startApp()
}

/**
 * This app will create Http Request to the url given by user
 *
 * There is a problem when making the request
 * it takes so long to complete reading the responses
 * Even if the content is so small and everything is read so fast, it will not stop the loop right away
 * And I still cannot figured it out why is that
 *
 * So this program will give timeout when making request and receive the all the responses to just 2 seconds top
 * This is weird solution. Because if user has poor connection, and need more than 2 seconds to complete the request
 * The user will not get the complete content
 */
fun startApp() {
    while (true) {
        print("Masukkan url: ")
        var input = readLine()
        url = input!!

        println("Anda mengakses host $url")

        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit(MakeHttpRequest())

        try {
            future[REQUEST_TIMEOUT, TimeUnit.SECONDS]
        } catch (e: TimeoutException) {
            future.cancel(true)
        }

        println(responseBody)
        responseBody = ""
    }
}

internal class MakeHttpRequest : Callable<Boolean> {
    @Throws(java.lang.Exception::class)
    override fun call(): Boolean {
        try {
            var socket: Socket = Socket(url, 80)

            var bis = BufferedInputStream(socket.getInputStream())
            val bf = BufferedReader(
                InputStreamReader(bis, StandardCharsets.UTF_8)
            )
            var bos = BufferedOutputStream(socket.getOutputStream())

            bos.write( "GET / $HTTP_VERSION\r\nHost: $url\r\n\r\n".toByteArray() )
            bos.flush()

            var line = bf.readLine()
            var isParseHeader = true
            val startTime = System.currentTimeMillis()

            while (line != null) {
                if (isParseHeader && line == "") {
                    isParseHeader = false
                    line = bf.readLine()
                    continue
                }

                if (isParseHeader) {
                    parseHeader(line)
                }
                else {
                    responseBody = responseBody + "\n" + line
                }
                line = bf.readLine()
            }

            bis.close()
            bos.close()
            socket.close()

            return true
        } catch (e: Exception) {
            e.printStackTrace()

            return false
        }
    }
}

fun parseHeader(line: String) {
    if (line.startsWith(HTTP_VERSION)) {
        var key = "Status-Code"
        var value = line.substring(HTTP_VERSION.length + 1, line.length)

        responseHeader.put(key, value)
    }
    else if (line.contains(":")) {
        var key = line.substring(0, line.indexOf(":"))
        var value = line.substring(line.indexOf(":") + 1, line.length)

        responseHeader.put(key, value)
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
         */
        if (out.checkError()) println(
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


/** DO NOT DELETE **/
/** Code snippets for creating corouting **/
//fun main(args: Array<String>) = runBlocking {
//
//    val job = GlobalScope.launch(Dispatchers.IO) {
//        makeHttpRequest(host)
//    }
//
//    while (true) {
//        print("Masukkan host: ")
//        host = readLine()!!
//        print("Kamu mengakses host $host")
//
//        try {
//            withTimeout(1000) {
//                job.join()
//            }
//        } catch (ex: TimeoutCancellationException) {
//            println(responseBody)
//        }
//    }
//}

/** Code snippets for colorfull print to console **/
//const val TEXT_RESET = "\u001B[0m"
//const val TEXT_BLACK = "\u001B[30m"
//const val TEXT_RED = "\u001B[31m"
//const val TEXT_GREEN = "\u001B[32m"
//const val TEXT_YELLOW = "\u001B[33m"
//const val TEXT_BLUE = "\u001B[34m"
//const val TEXT_PURPLE = "\u001B[35m"
//const val TEXT_CYAN = "\u001B[36m"
//const val TEXT_WHITE = "\u001B[37m"
//
//println("Hello, World!")
//println("<a href=\"www.google.com\">Google</a>")
//println("Google(https://www.google.com)")
//println(TEXT_RED + "This text is red!" + TEXT_RESET)