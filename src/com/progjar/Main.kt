package com.progjar

import com.progjar.parser.parseLink
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket

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
    connectToLink( "monta.if.its.ac.id" )
}

fun connectToLink( link: String ) {
    try {
        // 1. create a new socket object
        var socket:Socket = Socket(link, 80)

        // 2. obtain the input and output
        var bis = BufferedInputStream( socket.getInputStream() )
        var bos = BufferedOutputStream( socket.getOutputStream() )

        // 3. exchange messages
        bos.write( "hello world\r\n".toByteArray() )
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