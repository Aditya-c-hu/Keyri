package com.example.keyri.keyboard

object KeyboardLayouts {
    val letterRow1 = "qwertyuiop".map { it.toString() }
    val letterRow2 = "asdfghjkl".map { it.toString() }
    val letterRow3 = "zxcvbnm".map { it.toString() }

    val symbolRow1 = "1234567890".map { it.toString() }
    val symbolRow2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
    val symbolRow3 = listOf("=", "*", "\"", "'", ":", ";", "!", "?")
}
