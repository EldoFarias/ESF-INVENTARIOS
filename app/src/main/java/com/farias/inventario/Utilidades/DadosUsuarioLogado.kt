package com.farias.inventario.Utilidades

class DadosUsuarioLogado private constructor(){

    companion object {
        private var instance: DadosUsuarioLogado? = null

        fun getInstance(): DadosUsuarioLogado {
            if (instance == null) {
                instance = DadosUsuarioLogado()
            }
            return instance as DadosUsuarioLogado
        }
    }

    var codigoUsuario: String = ""
    var statusUsuario: String = "" // Exemplo Logado ou NãoLogado

}