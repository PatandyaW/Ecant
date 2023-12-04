package com.example.kelineyt.data

sealed class Category(val category: String) {

    object Makanan: Category("Special Product")
    object Cupboard: Category("Cupboard")
    object Table: Category("Table")
    object Accessory: Category("Accessory")
    object Furniture: Category("Furniture")
}