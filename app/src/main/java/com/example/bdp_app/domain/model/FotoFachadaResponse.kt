package com.example.bdp_app.domain.model



// 1. La respuesta "envoltorio" (lo que engloba success y data)
data class FotosFachadaApiResponse(
    val success: Boolean,
    val data: List<FotoFachadaItem>
)

// 2. El item individual (la foto)
data class FotoFachadaItem(
    val idFotoFachada: Int,
    val foto: String  // ¡Aquí está la clave! Se llama 'foto', no 'url'
)