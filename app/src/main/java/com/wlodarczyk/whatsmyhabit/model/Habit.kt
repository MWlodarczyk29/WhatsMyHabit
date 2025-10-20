package com.wlodarczyk.whatsmyhabit.model

//data class to klasa, ktorej android uzywa do przechowywania danych (automatycznie tworzy funkcje do porownania, kopiowania itp)
data class Habit (
    val id: Int,
    val name: String,
    val time: String
)