package com.progjar

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

    parseLink( "<a href=\"www.google.com\">google</a>")
}

// TODO: add nested content parsing
fun parseLink(passedLink: String) {
    if ( !isLink( passedLink ) ) {
        return;
    }

    var it = 0
    var trueLink : String? = ""
    var content : String? = ""
    while( it < passedLink.length ) {
        if( passedLink.get(it).equals('h') &&
            passedLink.get(it+1).equals('r') &&
            passedLink.get(it+2).equals('e') &&
            passedLink.get(it+3).equals('f') &&
            passedLink.get(it+4).equals('=') &&
            passedLink.get(it+5).equals('"')) {
            val firstIndex = it + 6
            var it1 = firstIndex
            var lastIndex = firstIndex
            while( true ) {
                if ( passedLink.get(it1 + 1).equals('"') ) {
                    lastIndex = it1
                    break
                }
                it1++
            }
            trueLink = passedLink.substring( firstIndex, lastIndex+1 )
        }

        if( passedLink.get(it).equals('>') && it < passedLink.length - 1 ) {
            var it1 = it + 1
            val firstIndex = it1
            var lastIndex = firstIndex
            while( true ) {
                if( passedLink.get(it1).equals('<') &&
                    passedLink.get(it1 + 1).equals('/')) {
                    lastIndex = it1 - 1
                    break
                }
                it1++
            }
            content = passedLink.substring( firstIndex, lastIndex+1 )
        }

        it++
    }

    print( content )
    print( " (" )
    print( trueLink )
    println( ")" )
}

fun isLink(passedLink: String): Boolean {
    if ( passedLink.get(0).equals('<') && passedLink.get(1).equals('a') ) {
        return true
    }
    return false
}