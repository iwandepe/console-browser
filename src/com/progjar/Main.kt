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
    println("Hello, World!")
    println("<a href=\"www.google.com\">Google</a>")
    println("Google(https://www.google.com)")
    println(TEXT_RED + "This text is red!" + TEXT_RESET)
}