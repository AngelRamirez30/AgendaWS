package com.pmv.agendaws.data.models

import java.io.Serializable

data class Contacts(
    var _ID: Int = 0,
    var nombre: String? = null,
    var telefono1: String? = null,
    var telefono2: String? = null,
    var direccion: String? = null,
    var notas: String? = null,
    var favorite: Int = 0,
    var idMovil: String? = null
) : Serializable