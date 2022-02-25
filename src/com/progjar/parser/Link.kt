package com.progjar.parser

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