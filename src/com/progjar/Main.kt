package com.progjar

import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


const val HTTP_VERSION = "HTTP/1.1"
const val REQUEST_TIMEOUT: Long = 2
const val DOWNLOAD_PATH = "/home/iwandepe/progjar/week-3/"

var responseHeader = mutableMapOf<String, String>()
var responseBody: String = ""

var url = "localhost"

fun main() {
    startApp()
}

/**
 * This app will create Http Request to the given url by user
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

        // TODO: Validate url input
        // TODO: if there is no protocol in the url input, give http as default protocol

        println("Anda mengakses url $url")

        var executor = Executors.newSingleThreadExecutor()
        var future = executor.submit(MakeHttpRequest())

        try {
            future[REQUEST_TIMEOUT, TimeUnit.SECONDS]
        } catch (e: TimeoutException) {
            future.cancel(true)
        }

        if (responseHeader.get("Status-Code")!!.startsWith("3")){
            responseBody = ""
            url = responseHeader.get("Location")!!
            println("Kamu diarahkan ke $url")
            executor = Executors.newSingleThreadExecutor()
            future = executor.submit(MakeHttpsRequest())

            try {
                future[REQUEST_TIMEOUT, TimeUnit.SECONDS]
            } catch (e: TimeoutException) {
                future.cancel(true)
            }
        }

        /**
         * Sometimes Http response for file request is correct
         * the Content-Type is other than text/html. Ex. "image/jpeg"
         *
         * But sometimes it returns Content-Type text/html and the Status-Code is 4xx
         */
        if (!responseHeader.get("Content-Type")!!.contains("html")) {
            GlobalScope.launch {
                downloadFile()
            }
            downloadFile()
            println("Program is downloading the file in the background")
            continue
        }

        if (responseHeader.get("Status-Code")!!.startsWith("4")) {
            GlobalScope.launch {
                downloadFile()
            }
            println("Program is downloading the file in the background")
            continue
        }

        println(responseHeader)
        println(responseBody)
        responseBody = ""
    }
}

fun downloadFile() {
    try {
        val urlObject = URL(url)
        val bis = BufferedInputStream(urlObject.openStream())
        val urlMap = parseUrl(url)
        val pathTo = DOWNLOAD_PATH + urlMap.get("path")
        val fis = FileOutputStream(pathTo)

        val buffer = ByteArray(1024)
        var count = 0
        while (bis.read(buffer, 0, 1024).also { count = it } != -1) {
            fis.write(buffer, 0, count)
        }

        fis.close()
        bis.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

internal class MakeHttpRequest : Callable<Boolean> {
    @Throws(java.lang.Exception::class)
    override fun call(): Boolean {
        try {
            val urlMap = parseUrl(url)
            val host = urlMap.get("host")
            val path = urlMap.get("path")

            var socket: Socket = Socket(host, 80)

            var bis = BufferedInputStream(socket.getInputStream())
            val br = BufferedReader(InputStreamReader(bis, StandardCharsets.UTF_8))
            var bos = BufferedOutputStream(socket.getOutputStream())

            bos.write( "GET $path $HTTP_VERSION\r\nHost: $host\r\n\r\n".toByteArray() )
            bos.flush()

            readBufferedReader(br)

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

internal class MakeHttpsRequest : Callable<Boolean> {
    @Throws(java.lang.Exception::class)
    override fun call(): Boolean {
        try {
            val urlMap = parseUrl(url)
            val host = urlMap.get("host")
            val path = urlMap.get("path")

            val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
            val socket = factory.createSocket(host, 443) as SSLSocket

            socket.startHandshake()
            val out = PrintWriter(BufferedWriter(OutputStreamWriter(socket.outputStream)))

            out.println("GET $path $HTTP_VERSION\r\nHost: $host\r\n\r\n")
            out.println()
            out.flush()

            if (out.checkError()) {
                println("SSLSocketClient:  java.io.PrintWriter error")
            }

            val br = BufferedReader(InputStreamReader(socket.inputStream))

            readBufferedReader(br)

            br.close()
            out.close()
            socket.close()

            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return false
        }
    }
}

/**
 * Read http response line by line in BufferedReader
 * Parsing the response to Http header and body
 *
 * The results are saved to global variable
 */
fun readBufferedReader(br: BufferedReader) {
    var line = br.readLine()
    var isParseHeader = true
    while (line != null) {
        if (isParseHeader && line == "") {
            isParseHeader = false
            line = br.readLine()
            continue
        }

        if (isParseHeader) {
            parseHeader(line)
        }
        else {
            responseBody = responseBody + "\n" + line
        }

        line = br.readLine()
    }
}

/**
 * Parse full url to host and path
 * The result is returned in Map<String, String>
 */
fun parseUrl(urlParam: String): Map<String, String> {
    var urlProp = urlParam
    if (urlProp.startsWith("http://") || urlProp.startsWith("https://")) {
        urlProp = urlProp.substring(urlProp.indexOf("/") + 2, urlProp.length)
    }

    var host = ""
    var path = "/"
    if (urlProp.contains("/")) {
        host = urlProp.substring(0, urlProp.indexOf("/"))
        path = urlProp.substring(host.length + 1, urlProp.length)
    }
    else {
        host = urlProp
    }

    return mapOf("host" to host, "path" to path)
}

/**
 * Parse per one line header
 * The result is saved to global variable
 */
fun parseHeader(line: String) {
    if (line.startsWith(HTTP_VERSION)) {
        var key = "Status-Code"
        var value = line.substring(HTTP_VERSION.length + 1, line.length)

        responseHeader.put(key, value)
    }
    else if (line.contains(":")) {
        var key = line.substring(0, line.indexOf(":"))
        var value = line.substring(key.length + 2, line.length)

        responseHeader.put(key, value)
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