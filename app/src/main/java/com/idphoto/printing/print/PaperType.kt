package com.idphoto.printing.print

enum class PaperType(val label: String) {
    MATTE("Matte"), GLOSSY("Glossy");

    companion object {
        fun fromSaved(value: String?): PaperType = entries.firstOrNull { it.name == value } ?: MATTE
    }
}
