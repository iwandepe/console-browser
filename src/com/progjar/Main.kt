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
const val HTTP_VERSION_1 = "HTTP/1.0"
const val REQUEST_TIMEOUT: Long = 2
const val DOWNLOAD_PATH = "/home/iwandepe/Downloads"
const val REDIRECTION_LIMIT = 5

var responseHeader = mutableMapOf<String, String>()
var responseBody: String = ""

/* web body variables*/
var webTitle: String = ""

var url = "localhost"
var access_token = ""
var email: String? = ""
var password: String? = ""

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
        responseBody = ""
        responseHeader.clear()
        print(TEXT_GREEN + "Enter url: " + TEXT_RESET)
        var input = readLine()

//        print("Masukkan url: ")
//        var input = readLine()
//        var input = "http://127.0.0.1/api/user/login/"
        url = input!!

        if (!url.startsWith("http")) {
            url = "http://" + url
        }

        println(TEXT_GREEN + "You're accessing $url" + TEXT_RESET)

        print(TEXT_GREEN + "Enter METHOD: " + TEXT_RESET)
        var httpMethod = readLine()

        // try to connect
        if ( httpMethod. equals("post", true) ) {

            print(TEXT_GREEN + "Enter email: " + TEXT_RESET)
            email = readLine()
            print(TEXT_RED + "Enter password: " + TEXT_RESET)
            password = readLine()

            val executor = Executors.newSingleThreadExecutor()
            val future = executor.submit( MakeHttpPostRequest() )

            try {
                future[REQUEST_TIMEOUT, TimeUnit.SECONDS]
            } catch (e: TimeoutException) {
                future.cancel(false)
            }
        } else if ( httpMethod. equals("get", true) ) {
            executeThread()
        }


//        checkStatusCode()

        if( responseHeader.containsKey("Status-Code") ) {
            println(" (${ responseHeader.get("Status-Code") })")
        } else {
            println("\nSorry, we can't even get a RESPONSE HEADER")
        }

        /* handle redirectionS */
        var it = 0
        while( responseHeader.containsKey("Status-Code") && responseHeader.get("Status-Code")!!.startsWith("3") ){
            responseBody = ""
            if ( responseHeader.containsKey("Location") )
                url = responseHeader.get("Location")!!
            if ( responseHeader.containsKey("location") )
                url = responseHeader.get("location")!!
            println(TEXT_GREEN + "You're redirected to $url " + TEXT_RESET)

            // try to connect
            executeThread()

//            checkStatusCode()

            it++
            if (it >= REDIRECTION_LIMIT) {
                println( "You're redirected too many times :(" )
                break
            }
        }

        // println(responseHeader)

        /**
         * Sometimes Http response for file request is correct
         * the Content-Type is other than text/html. Ex. "image/jpeg"
         *
         * But sometimes it returns Content-Type text/html and the Status-Code is 4xx
         */
        if ( responseHeader.containsKey("Content-Type") && !responseHeader.get("Content-Type")!!.contains("html") ||
            responseHeader.containsKey("content-type") && !responseHeader.get("content-type")!!.contains("html")) {
            GlobalScope.launch {
                downloadFile()
            }
            println(TEXT_BLUE + "Program is downloading the file in the background" + TEXT_RESET)

            println()
            println("=======================================================")
            continue
        }

        if ( responseBody.length != 0 ){
            getWebTitle()
            getLink()
            println( responseBody )
//            getAccessToken()
        }

        println()
        println("=======================================================")
        responseBody = ""
    }
}

fun checkStatusCode() {
    if( responseHeader.containsKey("Status-Code") ) {
        println(TEXT_YELLOW + "Status Code = " + responseHeader.get("Status-Code") + TEXT_RESET)
    } else {
        println(TEXT_RED + "Sorry, we can't even get a RESPONSE HEADER. Check again your url!" + TEXT_RESET)
    }
}

fun executeThread() {
    if( url.startsWith("https", true) ) {
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit(MakeHttpsRequest())

        try {
            future[REQUEST_TIMEOUT, TimeUnit.SECONDS]
        } catch (e: TimeoutException) {
            future.cancel(true)
        }
    } else {
        val executor = Executors.newSingleThreadExecutor()
        val future = executor.submit( MakeHttpRequest() )

        try {
            future[REQUEST_TIMEOUT, TimeUnit.SECONDS]
        } catch (e: TimeoutException) {
            future.cancel(true)
        }
    }
}

fun downloadFile() {
    try {
        val urlObject = URL(url)
        val bis = BufferedInputStream(urlObject.openStream())
        val urlMap = parseUrl(url)
        val path = urlMap.get("path")
        val pathTo = DOWNLOAD_PATH + path!!.substring(path.lastIndexOf("/") + 1)
        val fis = FileOutputStream(pathTo)

        val buffer = ByteArray(1024)
        var count = 0
        while (bis.read(buffer, 0, 1024).also { count = it } != -1) {
            fis.write(buffer, 0, count)
        }

        fis.close()
        bis.close()
    } catch (e: Exception) {
//        println(TEXT_RED + e.message + TEXT_RESET)
    }
}

internal class MakeHttpRequest : Callable<Boolean> {
    @Throws(java.lang.Exception::class)
    override fun call(): Boolean {
        try {
            val urlMap = parseUrl(url)
            val host = urlMap.get("host")
            var path = urlMap.get("path")

            var socket: Socket = Socket(host, 80)

            var bis = BufferedInputStream(socket.getInputStream())
            val br = BufferedReader(InputStreamReader(bis, StandardCharsets.UTF_8))
            var bos = BufferedOutputStream(socket.getOutputStream())

            var request = "GET $path $HTTP_VERSION\r\nHost: $host\r\n\r\n"

            if ( path.equals("") ) {
                path = "/"
                request = "GET $path $HTTP_VERSION\r\nHost: $host\r\n\r\n"
            }
            if ( url.contains( "api", true ) ) {
                request = "GET $path $HTTP_VERSION\r\nHost: $host\r\nAuthorization: Bearer $access_token\r\n\r\n"
            }
//            println( request )
            bos.write( request.toByteArray() )
            bos.flush()

            readBufferedReader(br)

            bis.close()
            bos.close()
            socket.close()

            return true
        } catch (e: Exception) {
            // e.printStackTrace()

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
            var path = urlMap.get("path")

            val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
            val socket = factory.createSocket(host, 443) as SSLSocket

            socket.startHandshake()
            val out = PrintWriter(BufferedWriter(OutputStreamWriter(socket.outputStream)))

            var request = "GET $path $HTTP_VERSION\r\nHost: $host\r\n\r\n"
//            println(TEXT_YELLOW + "Full request = " + request + TEXT_RESET)

//            if (path.equals("/") || path.equals("")) {
//                path = ""
//                request = "GET $HTTP_VERSION\r\nHost: $host\r\n\r\n"
//            }=
            out.println( request )
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
            // e.printStackTrace()
            return false
        }
    }
}

internal class MakeHttpPostRequest : Callable<Boolean> {
    @Throws(java.lang.Exception::class)
    override fun call(): Boolean {
        try {
            val urlMap = parseUrl(url)
            val host = urlMap.get("host")
            var path = urlMap.get("path")

            var socket: Socket = Socket(host, 80)

            var bis = BufferedInputStream(socket.getInputStream())
            val br = BufferedReader(InputStreamReader(bis, StandardCharsets.UTF_8))
            var bos = BufferedOutputStream(socket.getOutputStream())

            var cookie =
                "Cookie: XSRF-TOKEN=eyJpdiI6IndvNWl3Zk0xRW4wc3VUMUVtYzNBaXc9PSIsInZhbHVlIjoiVHFNVkVpUmxYZFp0K2Y2Nkt4MFdGcFUxMjVpdk5wVWQ5Z3dJeTRocHFkVlk5VzBrYmtIcUE2ekt6dFB2RVRtZm8wc0thY1EyVFRNY1RKTjlFaWQrc2xuQ1J3TUlqKzBYRGk5WnEySXBxbFZVcURLSU1pYXFSOTFESjhNL3BFSDMiLCJtYWMiOiJmYTgyYTFjYmI2ZDI0NDI5YmQ1MTc3ZTQ3ZmI3MWE1OTA5ZGFiMjRlODYzNjBlOTBmMGY4ZjExZjgyMzQwZjUwIiwidGFnIjoiIn0%3D; " +
                "laravel_session=eyJpdiI6IkdYRFlkZ2ZycXZqYXZhck03dzBtMEE9PSIsInZhbHVlIjoidVNzL054RzJMOE5VNldEWTdYUXZncmdPMGl6dG9Ra0ZuZ3pJKzlEQmFYenk4b09SQ01nQXdKWWhyK3h0a2JTMXFFa3hCelY3eURrMGRGaTI2bGJuSERYY1J2NzlBRGV2TVhQMkxCQkNJcmxmYUxzLzNJYi9JUzlIQnlGd3lQQmsiLCJtYWMiOiJiNjA2NmMzMWUzYTBjYmQ5ZmUwYjVkMDhkOTM0MjFkY2Q3ZjZhN2JkODM1NWE3NjdlNDkwZGQyM2E2OGMzM2VlIiwidGFnIjoiIn0%3D"

            var _token = "k5zJYMGZh7RLVJc0yhdAqYmdW1lZofia5jX9iLDL"
//            var email = "ei@gmail.com"
//            var password = "password"



            if ( path.equals("") ) {
                path = "/"
            }
//            var request = "POST $path $HTTP_VERSION\r\nHost: $host\r\n$cookie\r\nContent-Type: application/x-www-form-urlencoded\r\n_token: $_token\r\nemail: ei@gmail.com\r\npassword: password\r\n\r\n"
            var request = "POST $path $HTTP_VERSION\r\n" +
                    "Host: $host\r\n" +
//                    "Connection: keep-alive\r\n" +
                    "Content-Length: ${ ("email=$email" + "&" + "password=$password").length }\r\n" +
//                    "Cache-Control: max-age=0\r\n" +
//                    "Origin: http://instapp-ets.herokuapp.com\r\n" +
//                    "Upgrade-Insecure-Requests: 1\r\n" +
//                    "DNT: 1\r\n" +
                    "Content-Type: application/x-www-form-urlencoded\r\n" +
//                    "Accept: */*\r\n" +
//                    "Referer: http://instapp-ets.herokuapp.com/login\r\n" +
//                    "Accept-Language: id-ID,id;q=0.9,en-US;q=0.8,en;q=0.7\r\n" +
//                    cookie +
                    "\r\n" +

//                    "_token=$_token" + "&" +
                    "email=$email" + "&" +
                    "password=$password" +
                    "\r\n"

//            println( request )
            bos.write( request.toByteArray() )
            bos.flush()

            readBufferedReader(br)

            bis.close()
            bos.close()
            socket.close()

            return true
        } catch (e: Exception) {
            // e.printStackTrace()

            return false
        }
    }
}

internal class MakeHttpsPostRequest : Callable<Boolean> {
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

            out.println("POST $path $HTTP_VERSION\r\nHost: $host\r\n\r\nemail=ei@gmail.com&password=password&_token=bxKyOQUKFXU5K3kBUSF6hbxlXBpH1ctg59pvEezM\r\n")
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
//        println( line )
        line = br.readLine()
    }
//    println( responseHeader )
    println( responseBody )
    if ( responseHeader.containsKey("Content-Type") && responseHeader.get("Content-Type")!!.contains("application/json") ||
        responseHeader.containsKey("content-type") && responseHeader.get("content-type")!!.contains("application/json") ) {
        getAccessToken()
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
        path = urlProp.substring(host.length, urlProp.length)
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
    if (line.startsWith(HTTP_VERSION) || line.startsWith(HTTP_VERSION_1)) {
        var key = "Status-Code"
        var value = line.substring(line.indexOf(" ") + 1, line.length)

        responseHeader.put(key, value)
    }
    else if (line.contains(":")) {
        var key = line.substring(0, line.indexOf(":"))
        var value = line.substring(key.length + 2, line.length)

        responseHeader.put(key, value)
    }
}

fun getWebTitle() {
    var it = 0
    var webTitleStartIndex : Int = -1
    var webTitleEndIndex : Int = -1
    while( it < responseBody.length && webTitle == "" ) {
        // <title>{web_title}</title>
        if( responseBody.substring(it, it + 7).equals("<title>", true) ) {
            webTitleStartIndex = it + 7
        }
        if( responseBody.substring(it, it + 8).equals("</title>", true) ) {
            webTitleEndIndex = it
        }
        if ( webTitleStartIndex != -1 && webTitleEndIndex != -1 ) {
            webTitle = responseBody.substring( webTitleStartIndex, webTitleEndIndex )
        }
        it++
    }
    println( webTitle )
}

fun getLink() {
    var it = 0
    var startIndex = -1
    var endIndex = -1
    println("clickable links:")
    while( it < responseBody.length ) {
        // <a href="">{web_title}</a>
        if( (it < responseBody.length - 2) && responseBody.substring(it, it + 2).equals("<a", true) ) {
            startIndex = it
            var it1 = it
            while ( it1 < responseBody.length ) {
                if ( responseBody.substring(it1, it1 + 4).equals("</a>", true) ) {
                    endIndex = it1 + 4
                    break
                }
                it1++
            }
            if (startIndex != -1 && endIndex != -1) {
                if ( !handleHref(startIndex, endIndex).contains("http") ){
                    it++
                    continue
                }
                print( "- " )
                print( handleText(startIndex, endIndex) )
                print( " (" )
                print( handleHref(startIndex, endIndex) )
                println( ")" )
            }
        }

        it++
    }
}

fun handleText(startIndex: Int, endIndex: Int): String {
    var it = startIndex
    var contentStartIndex = -1
    var contentEndIndex = -1
    while (it < endIndex) {
        if( responseBody.get( it ).equals( '>' ) && (it != endIndex - 1) ) {
            contentStartIndex = it + 1
            var it1 = contentStartIndex
            while( it1 < endIndex ) {
                if( responseBody.substring( it1, it1 + 4 ).equals("</a>", true) ){
                    contentEndIndex = it1
                    break
                }
                it1++
            }
            /* this break is, if u shouldn't include a raw text inside the anchor tag, such icon or <i>*/
//            break
        }
        it++
    }
    if ( contentStartIndex == -1 || contentEndIndex == -1 || (contentStartIndex >= contentEndIndex) ) return ""
    return responseBody.substring( contentStartIndex, contentEndIndex ).replace("\t", "").replace("\n", " ").replace(" ", "")
}

fun handleHref(startIndex: Int, endIndex: Int): String {
    var it = startIndex
    var linkStartIndex = -1
    var linkEndIndex = -1
    while (it < endIndex) {
        if( responseBody.substring( it, it + 6).equals("href=\"", true) ) {
            linkStartIndex = it + 6
            var it1 = linkStartIndex
            while( it1 < endIndex ) {
                if( responseBody.get( it1 ).equals('\"', true) ){
                    linkEndIndex = it1
                    break
                }
                it1++
            }
        }
        it++
    }
    if ( linkStartIndex == -1 || linkEndIndex == -1 || (linkStartIndex >= linkEndIndex) ) return ""
    return responseBody.substring( linkStartIndex, linkEndIndex ).replace(" ", "").replace("\n", "")
}

fun getAccessToken() {
    var it = 0
    var startIndex = -1
    var endIndex = -1
    while ( it < responseBody.length ) {
        if ( responseBody.substring(it, it + "\"access_token\":\"".length ).equals( "\"access_token\":\"") ) {
            it += "\"access_token\":\"".length
            startIndex = it

            while ( it < responseBody.length ) {
                if ( responseBody.get( it ).equals('\"') ) {

                    endIndex = it
                    it++
                    break
                }
                it++
            }
        }
        it++
    }
    if ( startIndex == -1 || endIndex == -1 || (startIndex >= endIndex) ) access_token = ""
    access_token = responseBody.substring( startIndex, endIndex )
//    println( access_token )
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
const val TEXT_RESET = "\u001B[0m"
const val TEXT_BLACK = "\u001B[30m"
const val TEXT_RED = "\u001B[31m"
const val TEXT_GREEN = "\u001B[32m"
const val TEXT_YELLOW = "\u001B[33m"
const val TEXT_BLUE = "\u001B[34m"
const val TEXT_PURPLE = "\u001B[35m"
const val TEXT_CYAN = "\u001B[36m"
const val TEXT_WHITE = "\u001B[37m"
//
//println("Hello, World!")
//println("<a href=\"www.google.com\">Google</a>")
//println("Google(https://www.google.com)")
//println(TEXT_RED + "This text is red!" + TEXT_RESET)